package kr.bi.greenmate.term.service;

import kr.bi.greenmate.common.annotation.CacheableWithTTL;
import kr.bi.greenmate.term.domain.Term;
import kr.bi.greenmate.term.dto.TermResponse;
import kr.bi.greenmate.term.repository.TermRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class TermService {
    private final TermRepository termRepository;

    public List<Term> getAllTerms() {
        return termRepository.findAll();
    }

    @CacheableWithTTL(cacheName = "all-terms", ttl = 12, unit = TimeUnit.HOURS)
    public List<TermResponse> getAllTermsAsDto() {
        return getAllTerms().stream()
                .map(TermResponse::from)
                .collect(Collectors.toList());
    }
}
