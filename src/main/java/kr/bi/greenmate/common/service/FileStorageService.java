package kr.bi.greenmate.common.service;

import kr.bi.greenmate.common.domain.ImageFileExtension;
import kr.bi.greenmate.common.exception.ApplicationException;
import kr.bi.greenmate.common.repository.ObjectStorageRepository;
import lombok.RequiredArgsConstructor;
import org.apache.tika.Tika;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

import static kr.bi.greenmate.common.exception.CommonErrorCode.FILE_DELETE_FAILED;
import static kr.bi.greenmate.common.exception.CommonErrorCode.FILE_READ_FAILED;
import static kr.bi.greenmate.common.exception.CommonErrorCode.FILE_UPLOAD_FAILED;
import static kr.bi.greenmate.common.exception.CommonErrorCode.INVALID_FILE_NAME;
import static kr.bi.greenmate.common.exception.CommonErrorCode.MIME_TYPE_MISMATCH;
import static kr.bi.greenmate.common.exception.CommonErrorCode.MISSING_FILE_EXTENSION;
import static kr.bi.greenmate.common.exception.CommonErrorCode.UNREADABLE_FILE_MIME_TYPE;
import static kr.bi.greenmate.common.exception.CommonErrorCode.UNSUPPORTED_IMAGE_FILE_EXTENSION;

@Service
@RequiredArgsConstructor
public class FileStorageService {

    private static final long MAX_FILE_SIZE = 1_000_000L;

    private final ObjectStorageRepository objectStorageRepository;
    private final Tika tika;

    public String uploadFile(MultipartFile file, String subPath) throws IOException {
        if (file == null || file.isEmpty()) {
            return null;
        }

        // 파일 크기 검증
        validateFileSize(file);

        // 확장자/MIME 검증
        ImageFileExtension fileExtension = getFileExtension(file);

        String uniqueFileName = UUID.randomUUID() + "." + fileExtension.name().toLowerCase();

        try {
            String uploadedFileKey = objectStorageRepository.upload(
                    subPath,
                    uniqueFileName,
                    file.getInputStream()
            );
            return objectStorageRepository.getDownloadUrl(uploadedFileKey);
        } catch (IOException e){
            throw new ApplicationException(FILE_READ_FAILED);
        } catch (Exception e){
            throw new ApplicationException(FILE_UPLOAD_FAILED);
        }
    }

    public void deleteFile(String fileUrl) {
        try {
            objectStorageRepository.delete(fileUrl);
        } catch (Exception e){
            throw new ApplicationException(FILE_DELETE_FAILED);
        }
    }

    private void validateFileSize(MultipartFile file) {
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("이미지 파일 크기는 1MB를 초과할 수 없습니다.");
        }
    }

    private ImageFileExtension getFileExtension(MultipartFile file) throws IOException {
        String originalFileName = file.getOriginalFilename();
        if (originalFileName == null || originalFileName.isBlank()) {
            throw new ApplicationException(INVALID_FILE_NAME);
        }

        String fileExtensionStr = StringUtils.getFilenameExtension(originalFileName);
        if (fileExtensionStr == null) {
            throw new ApplicationException(MISSING_FILE_EXTENSION);
        }

        ImageFileExtension fileExtension = getImageFileExtension(fileExtensionStr.toUpperCase());
        validateMimeType(file, fileExtension);

        return fileExtension;
    }

    private void validateMimeType(MultipartFile file, ImageFileExtension extension) throws IOException {
        try (InputStream inputStream = file.getInputStream()) {
            String detectedMimeType = tika.detect(inputStream);
            if (detectedMimeType == null) {
                throw new ApplicationException(UNREADABLE_FILE_MIME_TYPE);
            }
            if (!extension.getMimeType().equals(detectedMimeType.toLowerCase())) {
                throw new ApplicationException(MIME_TYPE_MISMATCH);
            }
        } catch (IOException e) {
            throw new ApplicationException(FILE_READ_FAILED);
        }
    }

    private ImageFileExtension getImageFileExtension(String fileExtensionStr) {
        return ImageFileExtension.fromExtension(fileExtensionStr)
                .orElseThrow(() -> new ApplicationException(UNSUPPORTED_IMAGE_FILE_EXTENSION));
    }
}
