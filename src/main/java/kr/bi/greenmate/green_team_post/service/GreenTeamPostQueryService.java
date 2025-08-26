package kr.bi.greenmate.green_team_post.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import lombok.RequiredArgsConstructor;

import kr.bi.greenmate.common.repository.ObjectStorageRepository;
import kr.bi.greenmate.green_team_post.exception.GreenTeamPostErrorCode;
import kr.bi.greenmate.green_team_post.domain.GreenTeamPost;
import kr.bi.greenmate.green_team_post.dto.GreenTeamPostDetailResponse;
import kr.bi.greenmate.green_team_post.dto.GreenTeamPostSummaryResponse;
import kr.bi.greenmate.green_team_post.repository.GreenTeamPostImageRepository;
import kr.bi.greenmate.green_team_post.repository.GreenTeamPostRepository;
import kr.bi.greenmate.common.util.StringUtils;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GreenTeamPostQueryService {

  private static final int PREVIEW_LEN = 20;

  private final GreenTeamPostRepository postRepository;
  private final GreenTeamPostImageRepository imageRepository;
  private final ObjectStorageRepository objectStorageRepository;

  public GreenTeamPostDetailResponse getPostDetail(Long id) {
    GreenTeamPost post = postRepository.findByIdWithUser(id)
        .orElseThrow(() -> new ResponseStatusException(
            GreenTeamPostErrorCode.GTP_40401.status(),
            GreenTeamPostErrorCode.GTP_40401.code()
        ));

    List<String> imageUrls = imageRepository.findByPostId(id).stream()
        .map(img -> objectStorageRepository.getDownloadUrl(img.getImageUrl()))
        .toList();

    return GreenTeamPostDetailResponse.from(post, imageUrls);
  }

  public List<GreenTeamPostSummaryResponse> getPostList() {
    List<GreenTeamPost> posts = postRepository.findAllByOrderByCreatedAtDesc();

    return posts.stream()
        .map(post -> GreenTeamPostSummaryResponse.builder()
            .id(post.getId())
            .userId(post.getUser().getId())
            .nickname(post.getUser().getNickname())
            .title(post.getTitle())
            .content(StringUtils.truncateWithEllipsis(post.getContent(), PREVIEW_LEN))
            .participantCount(post.getParticipantCount())
            .maxParticipants(post.getMaxParticipants())
            .eventDate(post.getEventDate())
            .deadlineAt(post.getDeadlineAt())
            .build())
        .toList();
  }
}
