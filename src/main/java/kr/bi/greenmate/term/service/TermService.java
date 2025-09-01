package kr.bi.greenmate.term.service;

import kr.bi.greenmate.term.domain.Term;
import kr.bi.greenmate.term.dto.TermResponse;
import kr.bi.greenmate.term.repository.TermRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class TermService {
    private final TermRepository termRepository;

    @Cacheable(value = "terms")
    public List<Term> getAllTerms() {
        return termRepository.findAll();
    }

    public List<TermResponse> getAllTermsAsDto() {
        return getAllTerms().stream()
                .map(TermResponse::from)
                .collect(Collectors.toList());
    }
}
