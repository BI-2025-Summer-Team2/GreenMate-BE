package kr.bi.greenmate.common.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class FileRollbackEvent extends ApplicationEvent {

    private final String fileUrl;

    public FileRollbackEvent(Object source, String fileUrl) {
        super(source);
        this.fileUrl = fileUrl;
    }
}
