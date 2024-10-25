package springcar.RentalCar.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoleChangeReq {
    @NotNull
    private Long userId;
    @NotNull
    private Long roleId;
}
