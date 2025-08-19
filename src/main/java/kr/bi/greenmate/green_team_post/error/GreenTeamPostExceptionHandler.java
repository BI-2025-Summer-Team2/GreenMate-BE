package kr.bi.greenmate.green_team_post.error;

import java.util.EnumSet;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import static kr.bi.greenmate.green_team_post.error.GreenTeamPostErrorCode.GENERIC_40001;
import static kr.bi.greenmate.green_team_post.error.GreenTeamPostErrorCode.GENERIC_50001;

@RestControllerAdvice
public class GreenTeamPostExceptionHandler {

  private static final Map<String, GreenTeamPostErrorCode> CODE_MAP =
      EnumSet.allOf(GreenTeamPostErrorCode.class).stream()
          .collect(Collectors.toMap(GreenTeamPostErrorCode::code, e -> e));

  @ExceptionHandler(ResponseStatusException.class)
  public ResponseEntity<?> handleResponseStatusException(ResponseStatusException ex) {
    String reason = ex.getReason();
    GreenTeamPostErrorCode code = CODE_MAP.get(reason);

    if (code == null) {
      code = (ex.getStatusCode().is5xxServerError()) ? GENERIC_50001 : GENERIC_40001;
    }

    ProblemDetail pd = ProblemDetail.forStatus(code.status());
    pd.setTitle(reason != null ? reason : code.code());
    pd.setDetail(code.message());
    pd.setProperty("code", reason != null ? reason : code.code());

    return ResponseEntity.status(code.status()).body(pd);
  }
}
