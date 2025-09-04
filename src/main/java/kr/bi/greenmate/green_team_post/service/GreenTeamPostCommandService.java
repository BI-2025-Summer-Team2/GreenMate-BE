package kr.bi.greenmate.green_team_post.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
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

  private final GreenTeamPostRepository postRepository;
  private final GreenTeamPostImageRepository imageRepository;
  private final UserRepository userRepository;
  private final ImageService imageService;

  @Autowired
  private ObjectMapper objectMapper;

  /**
   * 환경 활동 모집글 생성
   *
   * @param userId  작성자 ID
   * @param request 게시글 생성 요청 DTO(JSON)
   * @return 생성된 게시글 ID
   */
  @Transactional
  public Long create(Long userId, GreenTeamPostCreateRequest request, List<MultipartFile> images) {
    User writer = findWriter(userId);
    GreenTeamPost post = createPost(writer, request);

    // 이미지가 없을 경우 조기종료
    if (images == null || images.isEmpty()) {
      return post.getId();
    }
    // 이미지 업로드 및 GreenTeamPostImage 저장
    saveImages(post, images);
    return post.getId();
  }

  private User findWriter(Long userId) {
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

    GreenTeamPost post = GreenTeamPost.builder()
        .user(writer)
        .title(request.getTitle())
        .content(request.getContent())
        .locationType(request.getLocationType())
        .locationGeojson(geojsonStr) // 문자열로 저장
        .maxParticipants(request.getMaxParticipants())
        .eventDate(request.getEventDate())
        .deadlineAt(request.getDeadlineAt())
        .build();

    return postRepository.save(post);
  }

  private void saveImages(GreenTeamPost post, List<MultipartFile> imageFiles) {
    if (imageFiles == null || imageFiles.isEmpty()) {
      return;
    }

    List<String> keys = Collections.emptyList();
    try {
      keys = imageService.uploadMany(IMAGE_DIR, imageFiles);
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
