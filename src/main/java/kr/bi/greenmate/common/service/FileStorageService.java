package kr.bi.greenmate.common.service;

import kr.bi.greenmate.common.domain.ImageFileExtension;
import kr.bi.greenmate.common.repository.ObjectStorageRepository;
import lombok.RequiredArgsConstructor;
import org.apache.tika.Tika;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FileStorageService {

    private final ObjectStorageRepository objectStorageRepository;
    private final Tika tika;

    public String uploadFile(MultipartFile file, String subPath) throws IOException {
        if (file == null || file.isEmpty()) {
            return null;
        }

        ImageFileExtension fileExtension = getFileExtension(file);

        String uniqueFileName = UUID.randomUUID() + "." + fileExtension.name().toLowerCase();

        String uploadedFileKey = objectStorageRepository.upload(
                subPath,
                uniqueFileName,
                file.getInputStream()
        );

        return objectStorageRepository.getDownloadUrl(uploadedFileKey);
    }

    public void deleteFile(String fileUrl) {
        objectStorageRepository.delete(fileUrl);
    }

    private ImageFileExtension getFileExtension(MultipartFile file) throws IOException {
        String originalFileName = file.getOriginalFilename();
        if (originalFileName == null || originalFileName.isBlank()) {
            throw new IllegalArgumentException("원본 파일명이 유효하지 않습니다.");
        }

        String fileExtensionStr = StringUtils.getFilenameExtension(originalFileName);
        if (fileExtensionStr == null) {
            throw new IllegalArgumentException("파일 확장자가 존재하지 않습니다.");
        }

        ImageFileExtension fileExtension = getImageFileExtension(fileExtensionStr.toUpperCase());
        validateMimeType(file, fileExtension);

        return fileExtension;
    }

    private void validateMimeType(MultipartFile file, ImageFileExtension extension) throws IOException {
        try (InputStream inputStream = file.getInputStream()) {
            String detectedMimeType = tika.detect(inputStream);
            if (detectedMimeType == null) {
                throw new IllegalArgumentException("파일의 MimeType을 확인할 수 없습니다.");
            }
            if (!extension.getMimeType().equals(detectedMimeType.toLowerCase())) {
                throw new IllegalArgumentException("파일의 MimeType이 확장자와 일치하지 않습니다. " + extension.getMimeType() + "이어야 합니다. : " + detectedMimeType);
            }
        } catch (IOException e) {
            throw new IOException("파일 읽기 실패", e);
        }
    }

    private ImageFileExtension getImageFileExtension(String fileExtensionStr) {
        return ImageFileExtension.fromExtension(fileExtensionStr)
                .orElseThrow(() -> new IllegalArgumentException("이미지 파일에 허용되지 않는 확장자입니다: " + fileExtensionStr));
    }
}
