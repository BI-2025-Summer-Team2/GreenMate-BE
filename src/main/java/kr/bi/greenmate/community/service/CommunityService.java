package kr.bi.greenmate.community.service;

import kr.bi.greenmate.common.annotation.DistributedLock;
import kr.bi.greenmate.common.event.FileRollbackEvent;
import kr.bi.greenmate.common.exception.ApplicationException;
import kr.bi.greenmate.common.repository.ObjectStorageRepository;
import kr.bi.greenmate.common.service.FileStorageService;
import kr.bi.greenmate.community.domain.Community;
import kr.bi.greenmate.community.domain.CommunityComment;
import kr.bi.greenmate.community.domain.CommunityImage;
import kr.bi.greenmate.community.domain.CommunityLike;
import kr.bi.greenmate.community.dto.CommunityLikeResponse;
import kr.bi.greenmate.community.dto.CommunityPostDetailResponse;
import kr.bi.greenmate.community.dto.CreateCommunityCommentRequest;
import kr.bi.greenmate.community.dto.CreateCommunityPostRequest;
import kr.bi.greenmate.community.repository.CommunityCommentRepository;
import kr.bi.greenmate.community.repository.CommunityLikeRepository;
import kr.bi.greenmate.community.repository.CommunityRepository;
import kr.bi.greenmate.user.domain.User;
import kr.bi.greenmate.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

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
    private final ObjectStorageRepository objectStorageRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final UserRepository userRepository;
    private final CommunityRepository communityRepository;
    private final CommunityLikeRepository communityLikeRepository;
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

    @DistributedLock(prefix = "COMMUNITY", keys = {"#postId"})
    public void createComment(Long postId, Long userId, CreateCommunityCommentRequest request, MultipartFile imageFile) {
        Community post = communityRepository.findById(postId).orElseThrow(() -> new ApplicationException(POST_NOT_FOUND));
        post.increaseCommentCount();

        String imageUri = null;
        if (imageFile != null && !imageFile.isEmpty()) {
            String imageUrl = fileStorageService.uploadFile(imageFile, COMMUNITY_COMMENT_IMAGE_DIR);
            eventPublisher.publishEvent(new FileRollbackEvent(this, imageUrl));
            imageUri = getUriPath(imageUrl);
        }

        CommunityComment comment = createCommunityComment(userId, post, request.getContent(), imageUri);
        commentRepository.save(comment);
    }

    public CommunityPostDetailResponse getPostDetail(Long userId, Long postId) {
        Community communityPost = communityRepository.findWithDetailsById(postId)
                .orElseThrow(() -> new ApplicationException(POST_NOT_FOUND));

        List<String> imageUrls = getImageUrls(communityPost.getImages());

        boolean isLiked = communityLikeRepository.existsByCommunity_IdAndUser_Id(postId, userId);

        return CommunityPostDetailResponse.from(communityPost, imageUrls, isLiked);
    }

    @DistributedLock(keys = {"#postId"}, prefix = "COMMUNITY")
    public CommunityLikeResponse toggleLike(Long userId, Long postId) {
        Community post = communityRepository.findById(postId).orElseThrow(() -> new ApplicationException(POST_NOT_FOUND));

        Optional<CommunityLike> likeOptional = communityLikeRepository.findByCommunity_IdAndUser_Id(postId, userId);
        if (likeOptional.isPresent()) {
            communityLikeRepository.delete(likeOptional.get());
            post.decreaseLikeCount();
        } else {
            User user = userRepository.findById(userId).orElseThrow(() -> new ApplicationException(USER_NOT_FOUND));
            addLike(post, user);
            post.increaseLikeCount();
        }

        return CommunityLikeResponse.builder()
                .likeCount(post.getLikeCount())
                .isLiked(likeOptional.isEmpty())
                .build();
    }

    private Community createCommunity(Long userId, CreateCommunityPostRequest request) {
        return Community.builder()
                .user(userRepository.findById(userId).orElseThrow(() -> new ApplicationException(USER_NOT_FOUND)))
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

    private List<String> getImageUrls(List<CommunityImage> images) {
        return images.stream()
                .map(image -> objectStorageRepository.getDownloadUrl(image.getImageUrl()))
                .toList();
    }

    private void addLike(Community community, User user) {
        CommunityLike newLike = CommunityLike.builder()
                .community(community)
                .user(user)
                .build();
        communityLikeRepository.save(newLike);
    }
}
