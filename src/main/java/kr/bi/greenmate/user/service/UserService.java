package kr.bi.greenmate.user.service;

import jakarta.transaction.Transactional;
import kr.bi.greenmate.term.domain.Term;
import kr.bi.greenmate.term.repository.TermRepository;
import kr.bi.greenmate.user.domain.User;
import kr.bi.greenmate.user.domain.UserAgreement;
import kr.bi.greenmate.user.dto.Agreement;
import kr.bi.greenmate.user.dto.SignUpRequest;
import kr.bi.greenmate.user.exception.DuplicateEmailException;
import kr.bi.greenmate.user.exception.RequiredTermNotAgreedException;
import kr.bi.greenmate.user.exception.TermAgreementValidationException;
import kr.bi.greenmate.user.repository.UserAgreementRepository;
import kr.bi.greenmate.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class UserService {

    private final UserRepository userRepository;
    private final TermRepository termRepository;
    private final UserAgreementRepository userAgreementRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void signUp(SignUpRequest request) {

        String email = request.getEmail();
        // email 중복 검증
        if (userRepository.existsByEmail(email)) {
            throw new DuplicateEmailException(email);
        }


        List<Term> terms = termRepository.findAll();
        Map<Long, Term> termMap = terms.stream()
                .collect(Collectors.toMap(Term::getId, term -> term));

        // 요청된 약관 중복
        List<Agreement> reqAgreements = request.getAgreements();
        Set<Long> reqTermIds = reqAgreements.stream()
                .map(Agreement::getTermId)
                .collect(Collectors.toSet());

        if (reqTermIds.size() != reqAgreements.size()) {
            throw new TermAgreementValidationException();
        }

        // 등록된 약관과 요청된 약관 일치
        if (!termMap.keySet().equals(reqTermIds)) {
            throw new TermAgreementValidationException();
        }

        // 필수 약관 동의
        if (!agreedAllRequiredTerms(reqAgreements, terms)) {
            throw new RequiredTermNotAgreedException();
        }

        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .nickname(request.getNickname())
                .profileImageUrl(request.getProfileImageUrl())
                .selfIntroduction(request.getSelfIntroduction())
                .build();
        userRepository.save(user);

        saveAgreements(user, reqAgreements, termMap);
    }

    private boolean agreedAllRequiredTerms(List<Agreement> agreements, List<Term> terms) {

        List<Long> agreedRequiredTermIds = agreements.stream()
                .filter(Agreement::getAgreed)
                .map(Agreement::getTermId)
                .toList();

        return terms.stream()
                .filter(Term::isMandatory)
                .allMatch(term ->
                        agreedRequiredTermIds.contains(term.getId()));
    }

    private void saveAgreements(User user, List<Agreement> agreements, Map<Long, Term> termMap) {

        List<UserAgreement> entities = agreements.stream()
                .map(agreement ->
                        UserAgreement.builder()
                                .user(user)
                                .term(termMap.get(agreement.getTermId()))
                                .agreed(agreement.getAgreed())
                                .build())
                .collect(Collectors.toList());
        userAgreementRepository.saveAll(entities);
    }
}
