package springcar.RentalCar.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PrivilegeChangeReq {
    @NotNull
    private Long roleId;
    @NotNull
    private Long privilegeId;
}
