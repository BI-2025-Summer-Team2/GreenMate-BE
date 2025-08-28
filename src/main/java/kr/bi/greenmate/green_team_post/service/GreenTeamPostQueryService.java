package kr.bi.greenmate.green_team_post.service;

import java.util.List;

import kr.bi.greenmate.common.pagination.CursorPaginator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import lombok.RequiredArgsConstructor;

import kr.bi.greenmate.common.pagination.dto.PageResponse;
import kr.bi.greenmate.common.repository.ObjectStorageRepository;
import kr.bi.greenmate.green_team_post.exception.GreenTeamPostErrorCode;
import kr.bi.greenmate.green_team_post.domain.GreenTeamPost;
import kr.bi.greenmate.green_team_post.dto.GreenTeamPostDetailResponse;
import kr.bi.greenmate.green_team_post.dto.GreenTeamPostSummaryResponse;
import kr.bi.greenmate.green_team_post.repository.GreenTeamPostImageRepository;
import kr.bi.greenmate.green_team_post.repository.GreenTeamPostRepository;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GreenTeamPostQueryService {

  private static final int DEFAULT_SIZE = 20;
  private static final int MAX_SIZE = 50;

  private final GreenTeamPostRepository postRepository;
  private final GreenTeamPostImageRepository imageRepository;
  private final ObjectStorageRepository objectStorageRepository;

  private final CursorPaginator paginator;

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

  public PageResponse<GreenTeamPostSummaryResponse> getPostList(Integer size, String cursor) {
    return paginator.paginate(
        DEFAULT_SIZE, MAX_SIZE, size, cursor,

        // 첫 페이지, 다음/이전 페이지 조회
        take -> postRepository.fetchFirst(org.springframework.data.domain.Pageable.ofSize(take)),
        (cAt, cId, take) -> postRepository.fetchNext(cAt, cId,
            org.springframework.data.domain.Pageable.ofSize(take)),
        (cAt, cId, take) -> postRepository.fetchPrevAsc(cAt, cId,
            org.springframework.data.domain.Pageable.ofSize(take)),

        // 커서 기준값 (createdAt, id)
        GreenTeamPost::getCreatedAt,
        GreenTeamPost::getId,

        GreenTeamPostSummaryResponse::from
    );
  }
}
