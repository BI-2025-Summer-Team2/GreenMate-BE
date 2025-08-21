package kr.bi.greenmate.green_team_post.service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import kr.bi.greenmate.common.service.ImageService;
import kr.bi.greenmate.green_team_post.domain.GreenTeamPost;
import kr.bi.greenmate.green_team_post.domain.GreenTeamPostImage;
import kr.bi.greenmate.green_team_post.dto.GreenTeamPostCreateRequest;
import kr.bi.greenmate.green_team_post.repository.GreenTeamPostImageRepository;
import kr.bi.greenmate.green_team_post.repository.GreenTeamPostRepository;
import kr.bi.greenmate.green_team_post.exception.GreenTeamPostErrorCode;
import kr.bi.greenmate.user.domain.User;
import kr.bi.greenmate.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GreenTeamPostCommandService {

  private static final String IMAGE_DIR = "green-team-posts";
  private static final int MAX_IMAGE_COUNT = 3;

  private final GreenTeamPostRepository postRepository;
  private final GreenTeamPostImageRepository imageRepository;
  private final UserRepository userRepository;
  private final ImageService imageService;

  /**
   * 환경 활동 모집글 생성
   *
   * @param userId 작성자 ID
   * @param req    게시글 생성 요청 DTO(JSON)
   * @param images 첨부 이미지 파일들(0~3장)
   * @return 생성된 게시글 ID
   */
  @Transactional
  public Long create(Long userId, GreenTeamPostCreateRequest req, List<MultipartFile> images) {
    validateRequest(userId, req);
    validateImagesCount(images);

    User writer = findWriter(userId);
    GreenTeamPost post = createPost(writer, req);

    // 이미지가 없을 경우 조기종료
    if (images == null || images.isEmpty()) {
      return post.getId();
    }
    // 이미지 업로드 및 GreenTeamPostImage 저장
    saveImages(post, images);
    return post.getId();
  }

  private void validateRequest(Long userId, GreenTeamPostCreateRequest req) {
    if (userId == null) {
      throw new ResponseStatusException(
          GreenTeamPostErrorCode.AUTH_40101.status(),
          GreenTeamPostErrorCode.AUTH_40101.code()
      );
    }

    LocalDateTime now = LocalDateTime.now();

    if (req.getEventDate().isBefore(now)) {
      throw new ResponseStatusException(
          GreenTeamPostErrorCode.GTP_40001.status(),
          GreenTeamPostErrorCode.GTP_40001.code()
      );
    }

    if (req.getDeadlineAt().isBefore(now)) {
      throw new ResponseStatusException(
          GreenTeamPostErrorCode.GTP_40002.status(),
          GreenTeamPostErrorCode.GTP_40002.code()
      );
    }

    if (req.getDeadlineAt().isAfter(req.getEventDate())) {
      throw new ResponseStatusException(
          GreenTeamPostErrorCode.GTP_40003.status(),
          GreenTeamPostErrorCode.GTP_40003.code()
      );
    }

    if (req.getMaxParticipants() == null || req.getMaxParticipants() < 1) {
      throw new ResponseStatusException(
          GreenTeamPostErrorCode.GTP_40004.status(),
          GreenTeamPostErrorCode.GTP_40004.code()
      );
    }
  }

  private void validateImagesCount(List<MultipartFile> images) {
    if (images == null || images.isEmpty()) {
      return;
    }

    if (images.size() > MAX_IMAGE_COUNT) {
      throw new ResponseStatusException(
          GreenTeamPostErrorCode.GTP_40005.status(),
          GreenTeamPostErrorCode.GTP_40005.code()
      );
    }
  }

  private User findWriter(Long userId) {
    return userRepository.findById(userId)
        .orElseThrow(() -> new ResponseStatusException(
            GreenTeamPostErrorCode.AUTH_40101.status(),
            GreenTeamPostErrorCode.AUTH_40101.code()
        ));
  }

  private GreenTeamPost createPost(User writer, GreenTeamPostCreateRequest req) {
    GreenTeamPost post = GreenTeamPost.builder()
        .user(writer)
        .title(req.getTitle())
        .content(req.getContent())
        .locationType(req.getLocationType())
        .locationGeojson(req.getLocationGeojson())
        .maxParticipants(req.getMaxParticipants())
        .eventDate(req.getEventDate())
        .deadlineAt(req.getDeadlineAt())
        .build();

    return postRepository.save(post);
  }

  private void saveImages(GreenTeamPost post, List<MultipartFile> images) {
    if (images == null || images.isEmpty()) {
      return;
    }

    List<String> keys = Collections.emptyList();
    try {
      keys = imageService.uploadMany(IMAGE_DIR, images);
      List<GreenTeamPostImage> postImages = keys.stream()
          .map(key -> GreenTeamPostImage.builder()
              .post(post)
              .imageUrl(key)
              .build())
          .toList();

      imageRepository.saveAll(postImages);

    } catch (ResponseStatusException e) {
      if (keys != null && !keys.isEmpty()) {
        imageService.deleteMany(keys);
      }
      throw e; // ImageService에서 예외처리
    } catch (Exception e) {
      if (keys != null && !keys.isEmpty()) {
        imageService.deleteMany(keys);
      }
      throw new ResponseStatusException(
          GreenTeamPostErrorCode.IMG_50001.status(),
          GreenTeamPostErrorCode.IMG_50001.code(),
          e
      );
    }
  }
}
