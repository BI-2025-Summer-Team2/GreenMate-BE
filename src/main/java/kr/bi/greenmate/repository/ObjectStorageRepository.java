package kr.bi.greenmate.repository;

import io.awspring.cloud.s3.S3Resource;
import io.awspring.cloud.s3.S3Template;
import kr.bi.greenmate.config.properties.ObjectStorageProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.io.InputStream;

// 외부 저장소에 파일 업로드, 다운로드, 삭제를 위한 리포지토리
@RequiredArgsConstructor
@Repository
public class ObjectStorageRepository {
    private final ObjectStorageProperties objectStorageProperties;
    private final S3Template s3Template;

    public String upload(String path, String key, InputStream stream) {
        String fullKey = path.endsWith("/") ? path + key : path + "/" + key;
        S3Resource result = s3Template.upload(objectStorageProperties.getBucket(), fullKey, stream);
        return result.getFilename();
    }

    public String getDownloadUrl(String key) {
        String baseUrl = objectStorageProperties.getCdnUrl();
        if (baseUrl.endsWith("/")) {
            baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
        }
        return baseUrl + "/" + key;
    }

    public void delete(String key) { // 참고로 delete는 실시간 반영되지 않음
        s3Template.deleteObject(objectStorageProperties.getBucket(), key);
    }
}
