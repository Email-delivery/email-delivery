package az.aladdin.emaildelivery.model.response.account;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminAccountPageResponse {

    private List<AdminAccountResponse> items;
    private long total;
    private int page;
    private int limit;
}
