package az.aladdin.emaildelivery.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "admin.email")
public class AdminEmailProperties {

    private int maxRecipientsPerRequest = 500;
    private int maxAttachmentsPerEmail = 5;
    private long maxAttachmentBytes = 10 * 1024 * 1024L;
    private int maxSubjectLength = 200;
    private int maxHtmlBodyLength = 500_000;
    private String publicBaseUrl = "http://localhost:8090";
}
