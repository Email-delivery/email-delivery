package az.aladdin.emaildelivery.model.response.email;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EmailCampaignResponse {

    private Long id;
    private String name;
    private String description;
    private String defaultSubject;
    private String defaultHtmlBody;
    private Long templateId;
    private int contactCount;
    private List<EmailCampaignContactResponse> contacts;
    private Instant createdAt;
    private Instant updatedAt;
}
