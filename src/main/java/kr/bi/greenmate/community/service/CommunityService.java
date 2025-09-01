package kr.bi.greenmate.community.service;

import kr.bi.greenmate.common.domain.FilePath;
import kr.bi.greenmate.common.event.FileRollbackEvent;
import kr.bi.greenmate.common.service.FileStorageService;
import kr.bi.greenmate.community.domain.Community;
import kr.bi.greenmate.community.domain.CommunityImage;
import kr.bi.greenmate.community.dto.CreateCommunityPostRequest;
import kr.bi.greenmate.community.repository.CommunityRepository;
import kr.bi.greenmate.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

import static kr.bi.greenmate.common.util.UriPathExtractor.getUriPath;

@Slf4j
@RequiredArgsConstructor
@Service
public class CommunityService {
    private final FileStorageService fileStorageService;
    private final ApplicationEventPublisher eventPublisher;
    private final UserRepository userRepository;
    private final CommunityRepository communityRepository;

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

    private Community createCommunity(Long userId, CreateCommunityPostRequest request) {
        return Community.builder()
                .user(userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("등록되지 않은 회원입니다.")))
                .title(request.getTitle())
                .content(request.getContent())
                .build();
    }

    private CommunityImage createImageEntity(MultipartFile imageFile, Community post) {
        try {
            String imageUrl = fileStorageService.uploadFile(imageFile, FilePath.COMMUNITY_POST.getPath());
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
