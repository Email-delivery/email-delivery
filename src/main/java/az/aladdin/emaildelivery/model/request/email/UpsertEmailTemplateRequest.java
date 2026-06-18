package az.aladdin.emaildelivery.model.request.email;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpsertEmailTemplateRequest {

    @NotBlank
    @Size(max = 160)
    private String name;

    @Size(max = 500)
    private String description;

    @Size(max = 200)
    private String subject;

    @Size(max = 500_000)
    private String htmlBody;
}
