package az.aladdin.emaildelivery.model.response.email;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EmailRecipientStatusResponse {

    private String email;
    private String name;
    private String status;
    private String deliveredAt;
    private String openedAt;
    private String errorMessage;
}
