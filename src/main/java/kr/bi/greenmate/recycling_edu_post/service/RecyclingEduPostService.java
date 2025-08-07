package kr.bi.greenmate.recycling_edu_post.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kr.bi.greenmate.recycling_edu_post.domain.RecyclingEduPost;
import kr.bi.greenmate.recycling_edu_post.dto.RecyclingEduPostResponse;
import kr.bi.greenmate.recycling_edu_post.repository.RecyclingEduPostRepository;

import lombok.RequiredArgsConstructor;


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RecyclingEduPostService {

  private final RecyclingEduPostRepository repository;

  /**
   * 전체 목록 조회
   */
  public List<RecyclingEduPostResponse> getAllPosts() {
    return repository.findAll().stream()
        .map(RecyclingEduPostResponse::from)
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

    return RecyclingEduPostResponse.from(post);
  }
}
