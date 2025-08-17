package kr.bi.greenmate.common.service;

import kr.bi.greenmate.common.domain.ImageFileExtension;
import kr.bi.greenmate.repository.ObjectStorageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FileStorageService {

    private final ObjectStorageRepository objectStorageRepository;

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

    private ImageFileExtension getFileExtension(MultipartFile file) throws IOException {
        String originalFileName = file.getOriginalFilename();
        validateOriginalFileName(originalFileName);

        String fileExtensionStr = StringUtils.getFilenameExtension(originalFileName);
        ImageFileExtension fileExtension = getImageFileExtension(fileExtensionStr);

        validateMimeType(file);

        return fileExtension;
    }

    private void validateOriginalFileName(String originalFileName) {
        if (originalFileName == null || originalFileName.isBlank()) {
            throw new IllegalArgumentException("원본 파일명이 유효하지 않습니다.");
        }
    }

    private void validateMimeType(MultipartFile file) throws IOException {
        try (InputStream inputStream = file.getInputStream()) {
            String detectedMimeType = URLConnection.guessContentTypeFromStream(inputStream);

            if (!ImageFileExtension.isAllowedMimeType(detectedMimeType)) {
                throw new IllegalArgumentException("허용되지 않는 MimeType입니다: " + detectedMimeType);
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
