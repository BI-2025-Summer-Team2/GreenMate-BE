package kr.bi.greenmate.community.service;

import kr.bi.greenmate.common.annotation.DistributedLock;
import kr.bi.greenmate.common.event.FileRollbackEvent;
import kr.bi.greenmate.common.exception.ApplicationException;
import kr.bi.greenmate.common.service.FileStorageService;
import kr.bi.greenmate.community.domain.Community;
import kr.bi.greenmate.community.domain.CommunityComment;
import kr.bi.greenmate.community.domain.CommunityImage;
import kr.bi.greenmate.community.dto.CreateCommunityCommentRequest;
import kr.bi.greenmate.community.dto.CreateCommunityPostRequest;
import kr.bi.greenmate.community.repository.CommunityCommentRepository;
import kr.bi.greenmate.community.repository.CommunityRepository;
import kr.bi.greenmate.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import static kr.bi.greenmate.common.util.UriPathExtractor.getUriPath;
import static kr.bi.greenmate.community.exception.CommunityErrorCode.POST_NOT_FOUND;
import static kr.bi.greenmate.user.exception.UserErrorCode.USER_NOT_FOUND;

@Slf4j
@RequiredArgsConstructor
@Service
public class CommunityService {

    private static final String COMMUNITY_IMAGE_DIR = "/community/post";
    private static final String COMMUNITY_COMMENT_IMAGE_DIR = "/community/comment";

    private final FileStorageService fileStorageService;
    private final ApplicationEventPublisher eventPublisher;
    private final UserRepository userRepository;
    private final CommunityRepository communityRepository;
    private final CommunityCommentRepository commentRepository;

    @Transactional
    public void createPost(Long userId, CreateCommunityPostRequest request, List<MultipartFile> imageFiles) {
        Community communityPost = createCommunity(userId, request);

        if (imageFiles != null && !imageFiles.isEmpty()) {
            for (MultipartFile imageFile : imageFiles) {
                if (imageFile == null || imageFile.isEmpty()) {
                    continue;
                }
                communityPost.getImages().add(createImageEntity(imageFile, communityPost));
            }
        }
        communityRepository.save(communityPost);
    }

    @DistributedLock(keys = {"'COMMUNITY:' + #request.postId"})
    public void createComment(Long postId, Long userId, CreateCommunityCommentRequest request, MultipartFile imageFile) {
        Community post = communityRepository.findById(postId).orElseThrow(() -> new ApplicationException(POST_NOT_FOUND));
        post.increaseCommentCount();

        String imageUrl = fileStorageService.uploadFile(imageFile, COMMUNITY_COMMENT_IMAGE_DIR);
        eventPublisher.publishEvent(new FileRollbackEvent(this, imageUrl));

        CommunityComment comment = createCommunityComment(userId, post, request.getContent(), getUriPath(imageUrl));
        commentRepository.save(comment);
    }

    private Community createCommunity(Long userId, CreateCommunityPostRequest request) {
        return Community.builder()
                .user(userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("등록되지 않은 회원입니다.")))
                .title(request.getTitle())
                .content(request.getContent())
                .build();
    }

    private CommunityComment createCommunityComment(Long userId, Community post, String content, String imageUri) {
        return CommunityComment.builder()
                .user(userRepository.findById(userId).orElseThrow(() -> new ApplicationException(USER_NOT_FOUND)))
                .community(post)
                .imageUrl(imageUri)
                .content(content)
                .build();
    }

    private CommunityImage createImageEntity(MultipartFile imageFile, Community post) {
        try {
            String imageUrl = fileStorageService.uploadFile(imageFile, COMMUNITY_IMAGE_DIR);
            log.debug("imageUrl: {}", imageUrl);
            // 롤백 대비
            eventPublisher.publishEvent(new FileRollbackEvent(this, imageUrl));

            return CommunityImage.builder()
                    .community(post)
                    .imageUrl(getUriPath(imageUrl))
                    .build();
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("커뮤니티 게시글의 이미지 파일이 유효하지 않습니다.");
        }
    }
}
