package springcar.RentalCar.dto.car;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CarDto {
    private Long id;
    @NotBlank
    private String number;
    @NotBlank
    private String model;
    private String status;
    private String comment;
    private String image;
}

