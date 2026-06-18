package az.aladdin.emaildelivery.model.request.email;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdateEmailScheduleRequest {

    @NotNull
    private Instant scheduledAt;
}
