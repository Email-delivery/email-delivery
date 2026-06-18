package az.aladdin.emaildelivery.model.response.email;

import az.aladdin.emaildelivery.model.enums.EmailSuppressionSource;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EmailSuppressionResponse {

    private Long id;
    private String email;
    private Instant unsubscribedAt;
    private EmailSuppressionSource source;
    private Long campaignId;
    private Long sentEmailId;
}
