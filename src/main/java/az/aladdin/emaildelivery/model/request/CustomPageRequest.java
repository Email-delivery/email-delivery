package az.aladdin.emaildelivery.model.request;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor(staticName = "of")
public class CustomPageRequest {
    private int page = 0;
    private int size = 10;
}
