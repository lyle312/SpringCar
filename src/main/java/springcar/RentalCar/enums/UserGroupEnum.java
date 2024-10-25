package springcar.RentalCar.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum UserGroupEnum {
    GROUP_ADMIN("ADMIN"),
    GROUP_ILSAN("일산렌터카");

    private final String value;
}
