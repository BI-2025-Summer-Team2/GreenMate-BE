package kr.bi.greenmate.config.properties;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;

@RequiredArgsConstructor
@ConfigurationProperties(prefix = "spring.cloud.aws.s3")
public class ObjectStorageProperties {
    public final String bucket;
    public final String cdnUrl;
}
