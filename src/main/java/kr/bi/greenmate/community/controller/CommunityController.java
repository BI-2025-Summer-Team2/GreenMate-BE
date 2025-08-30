package kr.bi.greenmate.community.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kr.bi.greenmate.community.dto.CreateCommunityPostRequest;
import kr.bi.greenmate.community.service.CommunityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Tag(name = "커뮤니티", description = "커뮤니티 글 관련 API")
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/v1/community")
@RestController
public class CommunityController {
    private final CommunityService communityService;

    @Operation(summary = "커뮤니티 글 생성", description = "작성된 글(JSON)과 이미지(파일)를 DB에 저장합니다.")
    @PostMapping(value = "/posts", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> posts(@RequestPart("writing") @Valid CreateCommunityPostRequest request,
                                           @RequestPart(value = "imageFiles", required = false) @Parameter(description = "이미지 파일") List<MultipartFile> imageFiles) {
        communityService.createPost(request, imageFiles);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
