package kr.bi.greenmate.user.service;

import kr.bi.greenmate.common.annotation.DistributedLock;
import kr.bi.greenmate.common.domain.FilePath;
import kr.bi.greenmate.common.event.FileRollbackEvent;
import kr.bi.greenmate.common.service.FileStorageService;
import kr.bi.greenmate.term.domain.Term;
import kr.bi.greenmate.term.service.TermService;
import kr.bi.greenmate.user.domain.User;
import kr.bi.greenmate.user.domain.UserAgreement;
import kr.bi.greenmate.user.domain.UserAgreementId;
import kr.bi.greenmate.user.dto.SignUpRequest;
import kr.bi.greenmate.user.dto.SignUpTermAgreement;
import kr.bi.greenmate.user.exception.DuplicateEmailException;
import kr.bi.greenmate.user.exception.DuplicateNicknameException;
import kr.bi.greenmate.user.exception.RequiredTermNotAgreedException;
import kr.bi.greenmate.user.exception.TermAgreementValidationException;
import kr.bi.greenmate.user.repository.UserAgreementRepository;
import kr.bi.greenmate.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static kr.bi.greenmate.common.util.UriPathExtractor.getUriPath;

@Slf4j
@RequiredArgsConstructor
@Service
public class UserService {

    private final UserRepository userRepository;
    private final TermService termService;
    private final UserAgreementRepository userAgreementRepository;
    private final PasswordEncoder passwordEncoder;
    private final FileStorageService fileStorageService;
    private final ApplicationEventPublisher eventPublisher;

    @DistributedLock(keys = {"#request.email", "#request.nickname"})
    public void signUp(SignUpRequest request, MultipartFile profileImage) {

        // unique 필드 1차 검증
        String email = request.getEmail();
        String nickname = request.getNickname();

        validateEmailIsUnique(email);
        validateNicknameIsUnique(nickname);

        List<Term> terms = termService.getAllTerms();
        Map<Long, Term> termMap = terms.stream()
                .collect(Collectors.toMap(Term::getId, term -> term));

        // 요청된 약관 유효성 검증
        List<SignUpTermAgreement> reqSignUpTermAgreements = request.getSignUpTermAgreements();
        validateRequestedAgreements(reqSignUpTermAgreements, termMap);

        // 필수 약관 동의 검증
        validateAllRequiredTermsAgreed(reqSignUpTermAgreements, terms);

        String profileImageUrl = saveProfileImage(profileImage);

        User user = createUser(request, profileImageUrl);
        userRepository.save(user);
        saveUserAgreements(user, reqSignUpTermAgreements, termMap);
    }

    private void validateEmailIsUnique(String email) {

        if (userRepository.existsByEmail(email)) {
            throw new DuplicateEmailException(email);
        }
    }

    public void validateNicknameIsUnique(String nickname) {

        if (userRepository.existsByNickname(nickname)) {
            throw new DuplicateNicknameException(nickname);
        }
    }

    private void validateRequestedAgreements(List<SignUpTermAgreement> reqSignUpTermAgreements, Map<Long, Term> termMap) {

        Set<Long> reqTermIds = getReqTermIds(reqSignUpTermAgreements);
        validateNoDuplicateRequestTerms(reqTermIds, reqSignUpTermAgreements);
        validateRequestedTermsMatchRegistered(reqTermIds, termMap);
    }

    private Set<Long> getReqTermIds(List<SignUpTermAgreement> reqSignUpTermAgreements) {

        return reqSignUpTermAgreements.stream()
                .map(SignUpTermAgreement::getTermId)
                .collect(Collectors.toSet());
    }

    private void validateNoDuplicateRequestTerms(Set<Long> reqTermIds, List<SignUpTermAgreement> reqSignUpTermAgreements) {

        if (reqTermIds.size() != reqSignUpTermAgreements.size()) {
            throw new TermAgreementValidationException();
        }
    }

    private void validateRequestedTermsMatchRegistered(Set<Long> reqTermIds, Map<Long, Term> termMap) {

        if (!termMap.keySet().equals(reqTermIds)) {
            throw new TermAgreementValidationException();
        }
    }

    private void validateAllRequiredTermsAgreed(List<SignUpTermAgreement> signUpTermAgreements, List<Term> terms) {

        Set<Long> agreedRequiredTermIds = signUpTermAgreements.stream()
                .filter(SignUpTermAgreement::getAgreed)
                .map(SignUpTermAgreement::getTermId)
                .collect(Collectors.toSet());

        if (!terms.stream()
                .filter(Term::isMandatory)
                .allMatch(term ->
                        agreedRequiredTermIds.contains(term.getId()))) {

            throw new RequiredTermNotAgreedException();
        }
    }

    private String saveProfileImage(MultipartFile profileImageFile) {
        if (profileImageFile == null || profileImageFile.isEmpty()) {
            return null;
        }
        try {
            String profileImageUrl = fileStorageService.uploadFile(profileImageFile, FilePath.USER_PROFILE.getPath());
            log.info("imageURI: {}", profileImageUrl);
            // 롤백될 경우 대비 이벤트 발행
            eventPublisher.publishEvent(new FileRollbackEvent(this, profileImageUrl));

            return getUriPath(profileImageUrl);

        } catch (IOException e) {
            throw new RuntimeException("프로필 이미지 파일 업로드 중 오류가 발생했습니다.", e);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("프로필 이미지 파일이 유효하지 않습니다.", e);
        }
    }

    private User createUser(SignUpRequest request, String profileImageUrl) {
        return User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .nickname(request.getNickname())
                .profileImageUrl(profileImageUrl)
                .selfIntroduction(request.getSelfIntroduction())
                .build();
    }

    private void saveUserAgreements(User user, List<SignUpTermAgreement> signUpTermAgreements, Map<Long, Term> termMap) {

        List<UserAgreement> entities = signUpTermAgreements.stream()
                .map(signUpTermAgreement -> {
                    Term term = termMap.get(signUpTermAgreement.getTermId());
                    UserAgreementId userAgreementId = new UserAgreementId(user.getId(), term.getId());
                    return UserAgreement.builder()
                            .id(userAgreementId)
                            .user(user)
                            .term(term)
                            .agreed(signUpTermAgreement.getAgreed())
                            .build();
                })
                .collect(Collectors.toList());
        userAgreementRepository.saveAll(entities);
    }
}
