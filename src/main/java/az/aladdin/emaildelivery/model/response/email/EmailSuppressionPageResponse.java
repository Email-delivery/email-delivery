package az.aladdin.emaildelivery.model.response.email;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EmailSuppressionPageResponse {

    private List<EmailSuppressionResponse> items;
    private long total;
    private int page;
    private int limit;
}
