package az.aladdin.emaildelivery.model.response.email;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EmailTemplateResponse {

    private Long id;
    private String name;
    private String description;
    private String subject;
    private String htmlBody;
    private Instant createdAt;
    private Instant updatedAt;
}
