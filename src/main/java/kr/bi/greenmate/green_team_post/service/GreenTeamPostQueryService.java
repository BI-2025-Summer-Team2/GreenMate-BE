package kr.bi.greenmate.green_team_post.service;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import lombok.RequiredArgsConstructor;

import kr.bi.greenmate.common.dto.CursorSliceResponse;
import kr.bi.greenmate.common.repository.ObjectStorageRepository;
import kr.bi.greenmate.green_team_post.domain.GreenTeamPost;
import kr.bi.greenmate.green_team_post.dto.GreenTeamPostDetailResponse;
import kr.bi.greenmate.green_team_post.dto.GreenTeamPostSummaryResponse;
import kr.bi.greenmate.green_team_post.exception.GreenTeamPostErrorCode;
import kr.bi.greenmate.green_team_post.repository.GreenTeamParticipantRepository;
import kr.bi.greenmate.green_team_post.repository.GreenTeamPostImageRepository;
import kr.bi.greenmate.green_team_post.repository.GreenTeamPostRepository;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GreenTeamPostQueryService {

  private final GreenTeamPostRepository postRepository;
  private final GreenTeamPostImageRepository imageRepository;
  private final GreenTeamParticipantRepository participantRepository;
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

  public CursorSliceResponse<GreenTeamPostSummaryResponse> getPostList(Long cursorId, int size) {
    Pageable pageable = Pageable.ofSize(size);

    Slice<GreenTeamPost> slice = (cursorId == null)
        ? postRepository.findAllByOrderByIdDesc(pageable)
        : postRepository.findByIdLessThanOrderByIdDesc(cursorId, pageable);

    Slice<GreenTeamPostSummaryResponse> mapped = slice.map(GreenTeamPostSummaryResponse::from);

    return CursorSliceResponse.of(mapped, size, GreenTeamPostSummaryResponse::getId);
  }

  @Transactional(readOnly = true)
  public CursorSliceResponse<GreenTeamPostSummaryResponse> getParticipatedPostList(Long userId,
      Long cursorId, int size) {
    Pageable pageable = Pageable.ofSize(size);

    Slice<GreenTeamPost> slice = participantRepository.findParticipatedPosts(userId, cursorId,
        pageable);

    Slice<GreenTeamPostSummaryResponse> mapped = slice.map(GreenTeamPostSummaryResponse::from);

    return CursorSliceResponse.of(mapped, size, GreenTeamPostSummaryResponse::getId);
  }

}
