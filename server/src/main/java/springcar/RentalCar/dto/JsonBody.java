package springcar.RentalCar.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JsonBody {
    @NotBlank
    private String message;
    private Object data;
}
