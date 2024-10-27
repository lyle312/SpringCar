package springcar.RentalCar.dto.car;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ModelImageChangeReq {
    @NotNull
    private Long id;
    private String name;
    @NotNull
    private MultipartFile image;
}
