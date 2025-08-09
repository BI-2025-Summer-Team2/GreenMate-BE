package kr.bi.greenmate.recycling_edu_post.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kr.bi.greenmate.common.repository.ObjectStorageRepository;
import kr.bi.greenmate.recycling_edu_post.domain.RecyclingEduPost;
import kr.bi.greenmate.recycling_edu_post.dto.RecyclingEduPostResponse;
import kr.bi.greenmate.recycling_edu_post.repository.RecyclingEduPostRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RecyclingEduPostService {

  private final RecyclingEduPostRepository repository;
  private final ObjectStorageRepository objectStorageRepository;

  /**
   * 전체 목록 조회
   */
  public List<RecyclingEduPostResponse> getAllPosts() {
    List<RecyclingEduPost> posts = repository.findAll();
    return posts.stream()
        .map(post -> RecyclingEduPostResponse.from(
            post,
            objectStorageRepository.getDownloadUrl(post.getImageUrl())
        ))
        .toList();
  }

  /**
   * 단일 조회
   */
  public RecyclingEduPostResponse getPostById(Long id) {
    if (id == null) {
      throw new IllegalArgumentException("ID는 null일 수 없습니다.");
    }
    RecyclingEduPost post = repository.findById(id)
        .orElseThrow(() -> new IllegalArgumentException("해당 분리수거 학습글이 존재하지 않습니다."));
    String imageUrl = objectStorageRepository.getDownloadUrl(post.getImageUrl());
    return RecyclingEduPostResponse.from(post, imageUrl);
  }
}
