package kr.bi.greenmate.green_team_post.exception;

import org.springframework.http.HttpStatus;

public enum GreenTeamPostErrorCode {
  GTP_40001("GTP-40001", HttpStatus.BAD_REQUEST, "활동일은 현재 시점 이후여야 합니다."),
  GTP_40002("GTP-40002", HttpStatus.BAD_REQUEST, "모집 종료일은 현재 시점 이후여야 합니다."),
  GTP_40003("GTP-40003", HttpStatus.BAD_REQUEST, "모집 종료일은 활동일 이전이어야 합니다."),
  GTP_40004("GTP-40004", HttpStatus.BAD_REQUEST, "최대 참가 인원은 1명 이상이어야 합니다."),
  GTP_40005("GTP-40005", HttpStatus.BAD_REQUEST, "이미지는 최대 3장까지 업로드 가능합니다."),

  GENERIC_40001("GENERIC-40001", HttpStatus.BAD_REQUEST, "잘못된 요청입니다."),
  AUTH_40101("AUTH-40101", HttpStatus.UNAUTHORIZED, "로그인이 필요합니다."),

  GENERIC_50001("GENERIC-50001", HttpStatus.INTERNAL_SERVER_ERROR, "서버 처리 중 오류가 발생했습니다."),
  IMG_50001("IMG-50001", HttpStatus.INTERNAL_SERVER_ERROR, "이미지 처리 중 오류가 발생했습니다.");

  private final String code;
  private final HttpStatus status;
  private final String message;

  GreenTeamPostErrorCode(String code, HttpStatus status, String message) {
    this.code = code;
    this.status = status;
    this.message = message;
  }

  public String code() {
    return code;
  }

  public HttpStatus status() {
    return status;
  }

  public String message() {
    return message;
  }
}
