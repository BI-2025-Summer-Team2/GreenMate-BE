package kr.bi.greenmate.recycling_edu_post.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kr.bi.greenmate.common.exception.ResourceNotFoundException;
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
    return repository.findAll().stream()
        .map(post -> RecyclingEduPostResponse.from(
            post,
            objectStorageRepository.getDownloadUrl(post.getImageUrl())))
        .toList();
  }

  /**
   * 단일 조회
   */
  public RecyclingEduPostResponse getPostDetail(Long id) {
    RecyclingEduPost post = repository.findById(id)
        .orElseThrow(() ->
            new ResourceNotFoundException("해당 분리수거 학습글이 존재하지 않습니다."));

    String imageUrl = objectStorageRepository.getDownloadUrl(post.getImageUrl());
    return RecyclingEduPostResponse.from(post, imageUrl);
  }
}
