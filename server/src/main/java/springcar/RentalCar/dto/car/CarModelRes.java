package springcar.RentalCar.dto.car;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CarModelRes {
    @NotNull
    private Long id;
    @NotBlank
    private String name;
    @NotBlank
    private String image;
}
