package kr.bi.greenmate.term.service;

import kr.bi.greenmate.common.annotation.CacheableWithTTL;
import kr.bi.greenmate.term.domain.Term;
import kr.bi.greenmate.term.dto.TermResponse;
import kr.bi.greenmate.term.dto.TermTitleResponse;
import kr.bi.greenmate.term.repository.TermRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class TermService {
    private final TermRepository termRepository;

    @CacheableWithTTL(cacheName = "terms-summary", ttl = 12, unit = TimeUnit.HOURS)
    public Set<TermTitleResponse> getTermSummary() {
        return getAllTerms().stream()
                .map(TermTitleResponse::from)
                .collect(Collectors.toSet());
    }

    @CacheableWithTTL(cacheName = "term", ttl = 12, unit = TimeUnit.HOURS)
    public TermResponse getTermById(long id) {
        return TermResponse.from(termRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("존재하지 않는 약관 id입니다.")));
    }

    public List<Term> getAllTerms() {
        return termRepository.findAll();
    }
}
