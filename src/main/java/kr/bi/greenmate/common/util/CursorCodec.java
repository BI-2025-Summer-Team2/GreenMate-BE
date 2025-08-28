package kr.bi.greenmate.common.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Base64;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

/**
 * 커서 기반 페이지네이션 인코딩/디코딩 유틸리티.
 *
 * 커서는 (createdAt, id, direction) 정보를 JSON → Base64 URL-safe 문자열로 변환하여 전달
 * - createdAt: 정렬 기준 시간
 * - id: 타이브레이커 (createdAt 동일 시 순서를 안정적으로 보장)
 * - direction: "next" | "prev" (페이지 이동 방향)
 */
public final class CursorCodec {

  private static final ObjectMapper MAPPER = new ObjectMapper()
      .registerModule(new JavaTimeModule())
      .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

  private CursorCodec() {
  }

  /**
   * 커서를 Base64 URL-safe 문자열로 인코딩
   */
  public static String encode(LocalDateTime createdAt, Long id, String direction) {
    try {
      Payload payload = new Payload(direction, createdAt, id);
      String json = MAPPER.writeValueAsString(payload);
      return Base64.getUrlEncoder().withoutPadding()
          .encodeToString(json.getBytes(StandardCharsets.UTF_8));
    } catch (JsonProcessingException e) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "커서 인코딩에 실패했습니다.", e);
    }
  }

  /**
   * 커서 문자열을 디코딩하여 Payload 객체로 반환
   */
  public static Payload decode(String cursor) {
    if (cursor == null || cursor.isBlank()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "커서 값이 비어 있습니다.");
    }
    try {
      byte[] bytes = Base64.getUrlDecoder().decode(cursor);
      String json = new String(bytes, StandardCharsets.UTF_8);
      Payload payload = MAPPER.readValue(json, Payload.class);

      if (payload.direction == null || payload.createdAt == null || payload.id == null) {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "커서 정보가 올바르지 않습니다.");
      }
      if (!"next".equals(payload.direction) && !"prev".equals(payload.direction)) {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "커서 방향 값이 잘못되었습니다.");
      }
      return payload;
    } catch (IllegalArgumentException | JsonProcessingException e) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "커서 형식이 올바르지 않습니다.", e);
    }
  }

  /**
   * 커서 데이터 구조
   * - direction: 페이지 방향 ("next" | "prev")
   * - createdAt: 정렬 기준 시간
   * - id: 타이브레이커 (createdAt이 같은 레코드 간 순서 보장)
   */
  public static final class Payload {

    public String direction;
    public LocalDateTime createdAt;
    public Long id;

    public Payload() {
    }

    public Payload(String direction, LocalDateTime createdAt, Long id) {
      this.direction = direction;
      this.createdAt = createdAt;
      this.id = id;
    }

    public String getDirection() {
      return direction;
    }

    public LocalDateTime getCreatedAt() {
      return createdAt;
    }

    public Long getId() {
      return id;
    }
  }
}
