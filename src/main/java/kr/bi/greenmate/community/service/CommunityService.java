package kr.bi.greenmate.community.service;

import kr.bi.greenmate.common.dto.CursorSliceResponse;
import kr.bi.greenmate.common.event.FileRollbackEvent;
import kr.bi.greenmate.common.exception.ApplicationException;
import kr.bi.greenmate.common.service.FileStorageService;
import kr.bi.greenmate.community.domain.Community;
import kr.bi.greenmate.community.domain.CommunityComment;
import kr.bi.greenmate.community.domain.CommunityImage;
import kr.bi.greenmate.community.dto.CommunityCommentResponse;
import kr.bi.greenmate.community.dto.CreateCommunityPostRequest;
import kr.bi.greenmate.community.repository.CommunityCommentRepository;
import kr.bi.greenmate.community.repository.CommunityRepository;
import kr.bi.greenmate.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

import static kr.bi.greenmate.common.util.UriPathExtractor.getUriPath;
import static kr.bi.greenmate.community.exception.CommunityErrorCode.POST_NOT_FOUND;

@Slf4j
@RequiredArgsConstructor
@Service
public class CommunityService {

    private static final String IMAGE_DIR = "/community";

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

    public CursorSliceResponse<CommunityCommentResponse> getCommentList(Long postId, Long cursor, int size) {
        if (!communityRepository.existsById(postId)) {
            throw new ApplicationException(POST_NOT_FOUND);
        }
        PageRequest pageRequest = PageRequest.of(0, size, Sort.by("id").descending());

        Slice<CommunityComment> commentSlice = (cursor == null)
                ? commentRepository.findAllByCommunity_Id(postId, pageRequest)
                : commentRepository.findAllByCommunity_IdAndIdLessThan(postId, cursor, pageRequest);

        Slice<CommunityCommentResponse> responseSlice = commentSlice.map(CommunityCommentResponse::from);

        return CursorSliceResponse.of(responseSlice, size, CommunityCommentResponse::getCommentId);
    }

    private Community createCommunity(Long userId, CreateCommunityPostRequest request) {
        return Community.builder()
                .user(userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("등록되지 않은 회원입니다.")))
                .title(request.getTitle())
                .content(request.getContent())
                .build();
    }

    private CommunityImage createImageEntity(MultipartFile imageFile, Community post) {
        try {
            String imageUrl = fileStorageService.uploadFile(imageFile, IMAGE_DIR);
            log.info("imageUrl: {}", imageUrl);
            // 롤백 대비
            eventPublisher.publishEvent(new FileRollbackEvent(this, imageUrl));

            return CommunityImage.builder()
                    .community(post)
                    .imageUrl(getUriPath(imageUrl))
                    .build();
        } catch (IOException e) {
            throw new RuntimeException("커뮤니티 게시글의 이미지 파일 업로드 중 오류가 발생했습니다.");
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("커뮤니티 게시글의 이미지 파일이 유효하지 않습니다.");
        }
    }
}
