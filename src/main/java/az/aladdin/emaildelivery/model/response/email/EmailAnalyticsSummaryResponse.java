package az.aladdin.emaildelivery.model.response.email;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EmailAnalyticsSummaryResponse {

    private long emailsSent;
    private long recipientsTotal;
    private long delivered;
    private long opened;
    private long failed;
    private double deliveryRate;
    private double openRate;
}
