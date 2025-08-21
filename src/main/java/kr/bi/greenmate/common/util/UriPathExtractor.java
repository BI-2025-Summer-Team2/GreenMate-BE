package kr.bi.greenmate.common.util;

import java.net.URI;
import java.net.URISyntaxException;

public class UriPathExtractor {
    private UriPathExtractor(){}

    public static String getUriPath(String url){
        try{
            URI uri = new URI(url);
            String path = uri.normalize().getPath();

            if(path != null && path.startsWith("/")){
                path = path.substring(1);
            }
            return path;
        } catch (URISyntaxException e){
            throw new RuntimeException("Failed to extract uri");
        }
    }
}
