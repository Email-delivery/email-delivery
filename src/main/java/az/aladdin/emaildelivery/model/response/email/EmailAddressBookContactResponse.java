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
public class EmailAddressBookContactResponse {

    private Long id;
    private String email;
    private String name;
    private Instant createdAt;
    private Instant updatedAt;
}
