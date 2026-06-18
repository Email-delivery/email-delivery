package az.aladdin.emaildelivery.model.request.email;

import jakarta.validation.constraints.Email;
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
public class UpsertEmailAddressBookContactRequest {

    @NotBlank
    @Email
    @Size(max = 160)
    private String email;

    @Size(max = 120)
    private String name;
}
