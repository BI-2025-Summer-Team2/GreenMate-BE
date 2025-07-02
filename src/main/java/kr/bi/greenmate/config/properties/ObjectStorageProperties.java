package kr.bi.greenmate.config.properties;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@RequiredArgsConstructor
@ConfigurationProperties(prefix = "spring.cloud.aws.s3")
public class ObjectStorageProperties {
    private final String bucket;
    private final String cdnUrl;
}
