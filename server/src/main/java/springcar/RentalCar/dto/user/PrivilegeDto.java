package springcar.RentalCar.dto.user;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PrivilegeDto {
    private Long id;
    @NotBlank
    private String name;
}
