package kr.bi.greenmate.term.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotNull;
import kr.bi.greenmate.term.dto.TermResponse;
import kr.bi.greenmate.term.dto.TermTitleResponse;
import kr.bi.greenmate.term.service.TermService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;

@Tag(name = "약관 관리", description = "약관 관련 API")
@RequestMapping("/api/v1/term")
@RequiredArgsConstructor
@RestController
public class TermController {
    private final TermService termService;

    @Operation(summary = "전체 약관 제목 조회", description = "등록된 전체 약관의 title과 id를 조회합니다.")
    @GetMapping("/summary")
    public ResponseEntity<Set<TermTitleResponse>> getTermSummary() {
        Set<TermTitleResponse> titles = termService.getTermSummary();
        return ResponseEntity.ok(titles);
    }

    @Operation(summary = "약관 상세 id로 조회", description = "id에 해당되는 단일 약관의 상세 정보를 조회합니다.")
    @GetMapping("/{id}")
    public ResponseEntity<TermResponse> getTermById(
            @Parameter(
                    description = "조회할 약관 ID",
                    required = true,
                    example = "1"
            )
            @PathVariable @NotNull Long id) {
        return ResponseEntity.ok(termService.getTermById(id));
    }
}
