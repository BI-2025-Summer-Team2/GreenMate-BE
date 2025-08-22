package kr.bi.greenmate.common.service;

import kr.bi.greenmate.common.repository.ObjectStorageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ImageService {

  private static final long MAX_IMAGE_BYTES = 1_000_000L; // 1MB (정책대로 10진수 1MB)

  /**
   * 허용 MIME 타입과 확장자 매핑
   */
  private static final Map<String, String> EXTENSIONS = Map.of(
      "image/jpeg", ".jpg",
      "image/jpg", ".jpg",
      "image/png", ".png",
      "image/webp", ".webp",
      "image/avif", ".avif"
  );

  private final ObjectStorageRepository storage;

  /**
   * 단일 이미지 업로드: 도메인 제외 key 반환 (예: green-team-post/UUID32.jpg)
   */
  public String uploadOne(String path, MultipartFile file) {
    String pathPrefix = normalize(path);
    validate(file);

    String ext = EXTENSIONS.get(file.getContentType().toLowerCase(Locale.ROOT));
    String uuid32 = UUID.randomUUID().toString().replace("-", "");
    String key = uuid32 + ext;

    try (InputStream is = file.getInputStream()) {
      // 저장소가 반환하는 최종 key를 그대로 반환 (경로 조합 중복 제거)
      return storage.upload(pathPrefix, key, is);
    } catch (IOException e) {
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "이미지 업로드 실패", e);
    }
  }

  /**
   * 다중 이미지 업로드 - 파일 목록이 비어있으면 빈 리스트를 반환한다. - 각 파일이 비어있으면 예외를 발생시킨다.
   */
  public List<String> uploadMany(String pathPrefix, List<MultipartFile> files) {
    if (files == null || files.isEmpty()) {
      return Collections.emptyList();
    }

    List<String> uploaded = new ArrayList<>(files.size());
    try {
      for (MultipartFile f : files) {
        if (f == null || f.isEmpty()) {
          throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "비어있는 파일이 포함되어 있습니다.");
        }
        String key = uploadOne(pathPrefix, f); // 성공 시 full key 반환
        uploaded.add(key);
      }
      return Collections.unmodifiableList(uploaded);

    } catch (ResponseStatusException e) {
      safeCompensate(uploaded);
      throw e;

    } catch (Exception e) {
      safeCompensate(uploaded);
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "이미지 업로드 중 오류가 발생했습니다.",
          e);
    }
  }

  /**
   * 단일 이미지 삭제 - 키 삭제를 시도하고, 실패는 WARN 로그로만 남김 - 예외는 전파하지 않음
   */
  public void deleteOne(String key) {
    if (key == null || key.isBlank()) {
      return;
    }
    try {
      storage.delete(key);
    } catch (Exception e) {
      log.warn("이미지 삭제 실패: key={}, 오류={}", key, e.getMessage(), e);
    }
  }

  /**
   * 다중 이미지 삭제 - 각 키별 삭제를 시도하고, 실패는 WARN 로그로만 남김 - 예외는 전파하지 않음
   */
  public void deleteMany(List<String> keys) {
    if (keys == null || keys.isEmpty()) {
      return;
    }

    List<String> failures = new ArrayList<>();
    for (String key : keys) {
      if (key == null || key.isBlank()) {
        continue;
      }
      try {
        storage.delete(key);
      } catch (Exception e) {
        failures.add(key);
      }
    }

    if (!failures.isEmpty()) {
      log.warn("이미지 삭제 실패 발생 (총 {}건). 실패 키 목록: {}",
          failures.size(), failures);
    }
  }

  /**
   * 이미지 부분 업로드 시 보상 삭제 (삭제 실패는 로그만 남기고 삼킴)
   */
  private void safeCompensate(List<String> uploadedKeys) {
    if (uploadedKeys == null || uploadedKeys.isEmpty()) {
      return;
    }
    try {
      deleteMany(uploadedKeys);
    } catch (Exception ex) {
      log.warn("보상 삭제 과정에서 추가 오류 발생", ex.getMessage(), ex);
    }
  }

  /**
   * 업로드할 이미지 파일 존재 여부, 크기, MIME 타입을 검증
   */
  private void validate(MultipartFile file) {
    if (file == null || file.isEmpty()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "파일이 비어있습니다.");
    }
    if (file.getSize() > MAX_IMAGE_BYTES) {
      throw new ResponseStatusException(HttpStatus.PAYLOAD_TOO_LARGE, "이미지는 최대 1MB까지 업로드 가능합니다.");
    }
    String ct = Optional.ofNullable(file.getContentType()).orElse("").toLowerCase(Locale.ROOT);
    if (!EXTENSIONS.containsKey(ct)) {
      throw new ResponseStatusException(
          HttpStatus.UNSUPPORTED_MEDIA_TYPE,
          "지원하지 않는 이미지 형식입니다. 지원 가능: " + String.join(", ", EXTENSIONS.keySet())
      );
    }
  }

  /**
   * 디렉토리 경로의 앞뒤 공백과 마지막 슬래시를 제거
   */
  private String normalize(String pathPrefix) {
    if (pathPrefix == null || pathPrefix.isBlank()) {
      return "";
    }
    String trimmed = pathPrefix.trim();
    return trimmed.endsWith("/") ? trimmed.substring(0, trimmed.length() - 1) : trimmed;
  }
}
