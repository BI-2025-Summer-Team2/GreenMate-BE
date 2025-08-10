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
import org.springframework.dao.DataIntegrityViolationException;
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
        validateEmailIsUnique(email);

        List<Term> terms = termRepository.findAll();
        Map<Long, Term> termMap = terms.stream()
                .collect(Collectors.toMap(Term::getId, term -> term));

        // 요청된 약관 유효성 검증
        List<Agreement> reqAgreements = request.getAgreements();
        validateRequestedAgreements(reqAgreements, termMap);

        // 필수 약관 동의 검증
        validateAllRequiredTermsAgreed(reqAgreements, terms);

        User user = createUser(request);
        saveUser(user, email);
        saveUserAgreements(user, reqAgreements, termMap);
    }

    private void validateEmailIsUnique(String email) {

        if (userRepository.existsByEmail(email)) {
            throw new DuplicateEmailException(email);
        }
    }

    private void validateRequestedAgreements(List<Agreement> reqAgreements, Map<Long, Term> termMap) {

        Set<Long> reqTermIds = getReqTermIds(reqAgreements);
        validateNoDuplicateRequestTerms(reqTermIds, reqAgreements);
        validateRequestedTermsMatchRegistered(reqTermIds, termMap);
    }

    private Set<Long> getReqTermIds(List<Agreement> reqAgreements) {

        return reqAgreements.stream()
                .map(Agreement::getTermId)
                .collect(Collectors.toSet());
    }

    private void validateNoDuplicateRequestTerms(Set<Long> reqTermIds, List<Agreement> reqAgreements) {

        if (reqTermIds.size() != reqAgreements.size()) {
            throw new TermAgreementValidationException();
        }
    }

    private void validateRequestedTermsMatchRegistered(Set<Long> reqTermIds, Map<Long, Term> termMap) {

        if (!termMap.keySet().equals(reqTermIds)) {
            throw new TermAgreementValidationException();
        }
    }

    private void validateAllRequiredTermsAgreed(List<Agreement> agreements, List<Term> terms) {

        Set<Long> agreedRequiredTermIds = agreements.stream()
                .filter(Agreement::getAgreed)
                .map(Agreement::getTermId)
                .collect(Collectors.toSet());

        if (!terms.stream()
                .filter(Term::isMandatory)
                .allMatch(term ->
                        agreedRequiredTermIds.contains(term.getId()))) {

            throw new RequiredTermNotAgreedException();
        }
    }

    private User createUser(SignUpRequest request) {
        return User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .nickname(request.getNickname())
                .profileImageUrl(request.getProfileImageUrl())
                .selfIntroduction(request.getSelfIntroduction())
                .build();
    }

    private void saveUserAgreements(User user, List<Agreement> agreements, Map<Long, Term> termMap) {

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

    private void saveUser(User user, String email){
        try {
            userRepository.save(user);
        } catch (DataIntegrityViolationException e) {
            // 이메일 중복이 원인인지 확인
            if (userRepository.existsByEmail(email)) {
                throw new DuplicateEmailException(email);
            }

            throw e;
        }
    }
}
