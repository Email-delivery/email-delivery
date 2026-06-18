package az.aladdin.emaildelivery.model.request.email;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpsertEmailCampaignRequest {

    @NotBlank
    @Size(max = 160)
    private String name;

    @Size(max = 500)
    private String description;

    @Size(max = 200)
    private String defaultSubject;

    @Size(max = 500_000)
    private String defaultHtmlBody;

    private Long templateId;

    @Builder.Default
    @Valid
    private List<EmailCampaignContactRequest> contacts = new ArrayList<>();
}
