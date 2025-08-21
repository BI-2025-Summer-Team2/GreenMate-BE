package kr.bi.greenmate.common.service;

import kr.bi.greenmate.common.repository.ObjectStorageRepository;
import lombok.RequiredArgsConstructor;
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
  public String uploadOne(String directory, MultipartFile file) {
    String dir = normalize(directory);
    validate(dir, file);

    String ext = EXTENSIONS.get(file.getContentType().toLowerCase(Locale.ROOT));
    String uuid32 = UUID.randomUUID().toString().replace("-", "");
    String key = uuid32 + ext;

    try (InputStream is = file.getInputStream()) {
      // 저장소가 반환하는 최종 key를 그대로 반환 (경로 조합 중복 제거)
      return storage.upload(dir, key, is);
    } catch (IOException e) {
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "이미지 업로드 실패", e);
    }
  }

  /**
   * 다중 이미지 업로드
   * - 파일 목록이 비어있으면 빈 리스트를 반환한다.
   * - 각 파일이 비어있으면 예외를 발생시킨다.
   */
  public List<String> uploadMany(String directory, List<MultipartFile> files) {
    if (files == null || files.isEmpty()) {
      return Collections.emptyList();
    }
    List<String> results = new ArrayList<>(files.size());
    for (MultipartFile f : files) {
      if (f == null || f.isEmpty()) {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "비어있는 파일이 포함되어 있습니다.");
      }
      results.add(uploadOne(directory, f));
    }
    return results;
  }

  /**
   * 업로드할 이미지의 디렉토리, 파일 존재 여부, 크기, MIME 타입을 검증
   */
  private void validate(String directory, MultipartFile file) {
    if (directory == null || directory.isBlank()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "저장 디렉토리가 비어있습니다.");
    }
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
  private String normalize(String dir) {
    if (dir == null || dir.isBlank()) {
      return "";
    }
    String trimmed = dir.trim();
    return trimmed.endsWith("/") ? trimmed.substring(0, trimmed.length() - 1) : trimmed;
  }
}
