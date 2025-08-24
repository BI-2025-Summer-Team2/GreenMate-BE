package kr.bi.greenmate.green_team_post.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import lombok.RequiredArgsConstructor;

import kr.bi.greenmate.common.repository.ObjectStorageRepository;
import kr.bi.greenmate.green_team_post.exception.GreenTeamPostErrorCode;
import kr.bi.greenmate.green_team_post.domain.GreenTeamPost;
import kr.bi.greenmate.green_team_post.domain.GreenTeamPostImage;
import kr.bi.greenmate.green_team_post.dto.GreenTeamPostDetailResponse;
import kr.bi.greenmate.green_team_post.repository.GreenTeamPostImageRepository;
import kr.bi.greenmate.green_team_post.repository.GreenTeamPostRepository;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GreenTeamPostQueryService {

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
        .map(GreenTeamPostImage::getImageUrl)
        .map(objectStorageRepository::getDownloadUrl)
        .toList();
    return GreenTeamPostDetailResponse.from(post, imageUrls);
  }
}
