package kr.bi.greenmate.common.util;

import kr.bi.greenmate.common.exception.ApplicationException;

import java.net.URI;
import java.net.URISyntaxException;

import static kr.bi.greenmate.common.exception.CommonErrorCode.FAILED_TO_EXTRACT_PATH;

public class UriPathExtractor {
    private UriPathExtractor() {
    }

    public static String getUriPath(String url) {
        try {
            URI uri = new URI(url);
            String path = uri.normalize().getPath();

            if (path != null && path.startsWith("/")) {
                path = path.substring(1);
            }
            return path;
        } catch (URISyntaxException e) {
            throw new ApplicationException(FAILED_TO_EXTRACT_PATH);
        }
    }
}
