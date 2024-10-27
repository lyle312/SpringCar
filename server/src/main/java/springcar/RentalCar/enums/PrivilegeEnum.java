package springcar.RentalCar.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum PrivilegeEnum {
    READ_PRIVILEGE("READ"),
    WRITE_PRIVILEGE("WRITE"),
    DELETE_PRIVILEGE("DELETE"),

    CAR_RENTAL_PRIVILEGE("CAR_RENTAL"),

    ADMIN_PRIVILEGE("ADMIN");

    private final String value;
}
