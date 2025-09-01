package kr.bi.greenmate.term.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import kr.bi.greenmate.term.dto.TermResponse;
import kr.bi.greenmate.term.service.TermService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "약관 관리", description = "약관 관련 API")
@RequestMapping("/api/v1/term")
@RequiredArgsConstructor
@RestController
public class TermController {
    private final TermService termService;

    @GetMapping("/")
    public ResponseEntity<List<TermResponse>> getAllTerms(){
        List<TermResponse> terms = termService.getAllTerms();
        return ResponseEntity.ok(terms);
    }
}
