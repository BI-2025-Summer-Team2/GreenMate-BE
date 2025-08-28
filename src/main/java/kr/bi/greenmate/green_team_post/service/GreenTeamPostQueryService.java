package kr.bi.greenmate.green_team_post.service;

import java.util.List;
import java.util.Collections;

import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import lombok.RequiredArgsConstructor;

import kr.bi.greenmate.common.pagination.dto.PageInfo;
import kr.bi.greenmate.common.pagination.dto.PageResponse;
import kr.bi.greenmate.common.pagination.CursorCodec;
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

  public PageResponse<GreenTeamPostSummaryResponse> getPostList(Integer size,
      String cursor) {
    int limit = clamp(size);
    boolean firstPage = (cursor == null || cursor.isBlank());

    List<GreenTeamPost> rows = firstPage
        ? postRepository.fetchFirst(Pageable.ofSize(limit + 1))
        : fetchByCursor(cursor, limit + 1);

    boolean hasMore = rows.size() > limit;
    List<GreenTeamPost> page = hasMore ? rows.subList(0, limit) : rows;

    PageInfo pageInfo = buildPageInfo(firstPage, hasMore, page);
    List<GreenTeamPostSummaryResponse> items = toSummaries(page);
    return PageResponse.of(items, pageInfo);
  }

  private List<GreenTeamPost> fetchByCursor(String cursor, int take) {
    CursorCodec.Payload p = CursorCodec.decode(cursor);
    List<GreenTeamPost> list;
    if ("next".equals(p.getDirection())) {
      list = postRepository.fetchNext(p.getCreatedAt(), p.getId(), Pageable.ofSize(take));
    } else if ("prev".equals(p.getDirection())) {
      list = postRepository.fetchPrevAsc(p.getCreatedAt(), p.getId(), Pageable.ofSize(take));
      Collections.reverse(list);
    } else {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid cursor direction");
    }
    return list;
  }

  private PageInfo buildPageInfo(boolean firstPage, boolean hasMore, List<GreenTeamPost> page) {
    if (page.isEmpty()) {
      return PageInfo.builder().nextCursor(null).prevCursor(null).build();
    }
    GreenTeamPost first = page.get(0);
    GreenTeamPost last = page.get(page.size() - 1);

    String prev = firstPage ? null
        : CursorCodec.encode(first.getCreatedAt(), first.getId(), "prev");

    String next = hasMore
        ? CursorCodec.encode(last.getCreatedAt(), last.getId(), "next")
        : null;

    return PageInfo.builder().nextCursor(next).prevCursor(prev).build();
  }

  private List<GreenTeamPostSummaryResponse> toSummaries(List<GreenTeamPost> page) {
    return page.stream().map(GreenTeamPostSummaryResponse::from).toList();
  }

  private int clamp(Integer size) {
    if (size == null) {
      return DEFAULT_SIZE;
    }
    if (size < 1) {
      return 1;
    }
    return Math.min(size, MAX_SIZE);
  }
}
