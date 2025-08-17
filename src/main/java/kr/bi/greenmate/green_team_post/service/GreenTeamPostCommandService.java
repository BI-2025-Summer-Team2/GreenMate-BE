package kr.bi.greenmate.green_team_post.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.http.HttpStatus;
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
   * @param req    게시글 생성 요청 DTO
   * @return 생성된 게시글 ID
   */
  @Transactional
  public Long create(Long userId, GreenTeamPostCreateRequest req) {
    validateRequest(userId, req);

    User writer = findWriter(userId);
    GreenTeamPost post = createPost(writer, req);
    saveImages(post, req.getImages());

    return post.getId();
  }

  private void validateRequest(Long userId, GreenTeamPostCreateRequest req) {
    if (userId == null) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다.");
    }

    LocalDateTime now = LocalDateTime.now();
    if (req.getEventDate().isBefore(now)) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "활동일은 현재 시점 이후여야 합니다.");
    }

    if (req.getDeadlineAt().isBefore(now)) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "모집 종료일은 현재 시점 이후여야 합니다.");
    }

    if (req.getDeadlineAt().isAfter(req.getEventDate())) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "모집 종료일은 활동일 이전이어야 합니다.");
    }

    if (req.getMaxParticipants() == null || req.getMaxParticipants() < 1) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "최대 참가 인원은 1명 이상이어야 합니다.");
    }

    if (req.getImages().size() > MAX_IMAGE_COUNT) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "이미지는 최대 " + MAX_IMAGE_COUNT + "장까지 업로드 가능합니다."
      );
    }
  }

  private User findWriter(Long userId) {
    return userRepository.findById(userId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "유효하지 않은 사용자입니다."));
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

    try {
      List<String> keys = imageService.uploadMany(
          IMAGE_DIR,
          images,
          MAX_IMAGE_COUNT
      );

      List<GreenTeamPostImage> postImages = keys.stream()
          .map(key -> GreenTeamPostImage.builder()
              .post(post)
              .imageUrl(key)
              .build())
          .toList();

      imageRepository.saveAll(postImages);

    } catch (ResponseStatusException e) {
      throw e; // ImageService에서 예외처리
    } catch (Exception e) {
      throw new ResponseStatusException(
          HttpStatus.INTERNAL_SERVER_ERROR,
          "게시글 이미지 처리 중 오류가 발생했습니다.",
          e
      );
    }
  }
}
