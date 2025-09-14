package kr.bi.greenmate.green_team_post.service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import kr.bi.greenmate.common.annotation.DistributedLock;
import kr.bi.greenmate.common.event.FileRollbackEvent;
import kr.bi.greenmate.common.service.FileStorageService;
import kr.bi.greenmate.common.util.UriPathExtractor;
import kr.bi.greenmate.green_team_post.domain.GreenTeamPost;
import kr.bi.greenmate.green_team_post.domain.GreenTeamPostImage;
import kr.bi.greenmate.green_team_post.domain.GreenTeamParticipant;
import kr.bi.greenmate.green_team_post.domain.GreenTeamPostLike;
import kr.bi.greenmate.green_team_post.dto.GreenTeamPostCreateRequest;
import kr.bi.greenmate.green_team_post.dto.GreenTeamPostParticipantResponse;
import kr.bi.greenmate.green_team_post.dto.GreenTeamPostLikeResponse;
import kr.bi.greenmate.green_team_post.exception.GreenTeamPostErrorCode;
import kr.bi.greenmate.green_team_post.repository.GreenTeamParticipantRepository;
import kr.bi.greenmate.green_team_post.repository.GreenTeamPostLikeRepository;
import kr.bi.greenmate.green_team_post.repository.GreenTeamPostRepository;
import kr.bi.greenmate.user.domain.User;
import kr.bi.greenmate.user.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class GreenTeamPostCommandService {

  private static final String IMAGE_DIR = "green-team-posts";
  private static final int MAX_IMAGE_COUNT = 3;

  private final GreenTeamPostRepository postRepository;
  private final GreenTeamParticipantRepository participantRepository;
  private final GreenTeamPostLikeRepository likeRepository;
  private final UserRepository userRepository;
  private final FileStorageService fileStorageService;
  private final ApplicationEventPublisher eventPublisher;
  private final ObjectMapper objectMapper;

  /**
   * 환경 활동 모집글 생성
   *
   * @param userId  작성자 ID
   * @param request 게시글 생성 요청 DTO(JSON)
   * @return 생성된 게시글 ID
   */
  @Transactional
  public Long create(Long userId, GreenTeamPostCreateRequest request, List<MultipartFile> images) {

    if (images != null && images.size() > MAX_IMAGE_COUNT) {
      throw new ResponseStatusException(
          GreenTeamPostErrorCode.GTP_40005.status(),
          GreenTeamPostErrorCode.GTP_40005.code()
      );
    }

    User writer = findUserById(userId);
    GreenTeamPost post = createPost(writer, request);

    // 이미지가 없을 경우 조기종료
    if (images == null || images.isEmpty()) {
      postRepository.save(post);
      return post.getId();
    }

    for (MultipartFile imageFile : images) {
      if (imageFile == null || imageFile.isEmpty()) {
        continue;
      }
      post.getImages().add(createImageEntity(imageFile, post));
    }
    postRepository.save(post);
    return post.getId();
  }

  /**
   * 참가 신청
   * - 참가가 없으면 생성·카운트 증가, 있으면 무시(멱등성 보장)
   * - 모집 정원 및 마감일 체크
   *
   * @param postId 게시글 ID
   * @param userId 사용자 ID
   */
  @DistributedLock(prefix = "post:participant", keys = {"#postId"})
  @Transactional
  public GreenTeamPostParticipantResponse applyParticipation(Long postId, Long userId) {
    GreenTeamPost post = findPostById(postId);
    User user = findUserById(userId);

    if (participantRepository.existsByPostIdAndUserId(postId, userId)) {
      return GreenTeamPostParticipantResponse.from(true, post.getParticipantCount());
    }

    if (LocalDateTime.now().isAfter(post.getDeadlineAt())) {
      throw new ResponseStatusException(
          GreenTeamPostErrorCode.GTP_40007.status(),
          GreenTeamPostErrorCode.GTP_40007.code()
      );
    }

    if (post.getParticipantCount() >= post.getMaxParticipants()) {
      throw new ResponseStatusException(
          GreenTeamPostErrorCode.GTP_40008.status(),
          GreenTeamPostErrorCode.GTP_40008.code()
      );
    }

    // 참가자 저장
    participantRepository.save(
        GreenTeamParticipant.builder()
            .post(post)
            .user(user)
            .build()
    );
    post.increaseParticipantCount();

    return GreenTeamPostParticipantResponse.from(true, post.getParticipantCount());
  }

  /**
   * 참가 취소
   * - 참가가 있으면 삭제·카운트 감소, 없으면 무시(멱등성 보장)
   *
   * @param postId 게시글 ID
   * @param userId 사용자 ID
   */
  @DistributedLock(prefix = "post:participant", keys = {"#postId"})
  @Transactional
  public GreenTeamPostParticipantResponse cancelParticipation(Long postId, Long userId) {
    GreenTeamPost post = findPostById(postId);

    participantRepository.findByPostIdAndUserId(postId, userId).ifPresent(participant -> {
      participantRepository.delete(participant);
      post.decreaseParticipantCount();
    });

    return GreenTeamPostParticipantResponse.from(false, post.getParticipantCount());
  }

  /**
   * 좋아요 추가
   * - 좋아요가 없으면 생성·카운트 증가, 있으면 무시(멱등성 보장)
   *
   * @param postId 게시글 ID
   * @param userId 사용자 ID
   */
  @DistributedLock(prefix = "post:like", keys = {"#postId"})
  @Transactional
  public GreenTeamPostLikeResponse addLike(Long postId, Long userId) {
    GreenTeamPost post = findPostById(postId);
    User user = findUserById(userId);

    if (likeRepository.existsByPostIdAndUserId(postId, userId)) {
      return GreenTeamPostLikeResponse.from(true, post.getLikeCount());
    }

    likeRepository.save(
        GreenTeamPostLike.builder()
            .post(post)
            .user(user)
            .build()
    );
    post.increaseLikeCount();

    return GreenTeamPostLikeResponse.from(true, post.getLikeCount());
  }

  /**
   * 좋아요 취소
   * - 좋아요가 있으면 삭제·카운트 감소, 없으면 무시(멱등성 보장)
   *
   * @param postId 게시글 ID
   * @param userId 사용자 ID
   */
  @DistributedLock(prefix = "post:like", keys = {"#postId"})
  @Transactional
  public GreenTeamPostLikeResponse removeLike(Long postId, Long userId) {
    GreenTeamPost post = findPostById(postId);

    likeRepository.findByPostIdAndUserId(postId, userId)
        .ifPresent(like -> {
          likeRepository.delete(like);
          post.decreaseLikeCount();
        });

    return GreenTeamPostLikeResponse.from(false, post.getLikeCount());
  }

  private GreenTeamPost findPostById(Long postId) {
    return postRepository.findById(postId)
        .orElseThrow(() -> new ResponseStatusException(
            GreenTeamPostErrorCode.GTP_40401.status(),
            GreenTeamPostErrorCode.GTP_40401.code()
        ));
  }

  private User findUserById(Long userId) {
    return userRepository.findById(userId)
        .orElseThrow(() -> new ResponseStatusException(
            GreenTeamPostErrorCode.AUTH_40401.status(),
            GreenTeamPostErrorCode.AUTH_40401.code()
        ));
  }

  private GreenTeamPost createPost(User writer, GreenTeamPostCreateRequest request) {
    String geojsonStr;
    try {
      geojsonStr = objectMapper.writeValueAsString(request.getLocationGeojson());
    } catch (JsonProcessingException e) {
      throw new ResponseStatusException(
          GreenTeamPostErrorCode.GTP_50001.status(),
          GreenTeamPostErrorCode.GTP_50001.code(),
          e
      );
    }

    return GreenTeamPost.builder()
        .user(writer)
        .title(request.getTitle())
        .content(request.getContent())
        .locationType(request.getLocationType())
        .locationGeojson(geojsonStr)
        .maxParticipants(request.getMaxParticipants())
        .eventDate(request.getEventDate())
        .deadlineAt(request.getDeadlineAt())
        .build();
  }

  private GreenTeamPostImage createImageEntity(MultipartFile imageFile, GreenTeamPost post) {
    try {
      String uploadedUrl = fileStorageService.uploadFile(imageFile, IMAGE_DIR);

      // 롤백 처리
      eventPublisher.publishEvent(new FileRollbackEvent(this, uploadedUrl));

      String uriPath = UriPathExtractor.getUriPath(uploadedUrl);
      return GreenTeamPostImage.builder()
          .post(post)
          .imageUrl(uriPath)
          .build();
    } catch (IOException e) {
      throw new ResponseStatusException(
          GreenTeamPostErrorCode.IMG_50001.status(),
          GreenTeamPostErrorCode.IMG_50001.code(),
          e
      );
    } catch (IllegalArgumentException e) {
      throw new ResponseStatusException(
          GreenTeamPostErrorCode.GTP_40006.status(),
          GreenTeamPostErrorCode.GTP_40006.code(),
          e
      );
    }
  }
}
