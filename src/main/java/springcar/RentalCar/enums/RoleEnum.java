package springcar.RentalCar.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum RoleEnum {
    ROLE_ADMIN("ADMIN"),
    ROLE_USER("USER"),
    ROLE_ADVANCED_USER("ADVANCED_USER"),
    ROLE_CAR_RENTAL("CAR_RENTAL");

    private final String value;
}
