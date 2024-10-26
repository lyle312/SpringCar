package springcar.RentalCar.config;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import springcar.RentalCar.entity.*;
import springcar.RentalCar.repository.*;
import springcar.RentalCar.entity.*;
import springcar.RentalCar.enums.PrivilegeEnum;
import springcar.RentalCar.enums.RoleEnum;
import springcar.RentalCar.repository.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class SetupDummyData {
    private final CarRepository carRepository;
    private final CarModelRepository carModelRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserGroupRepository userGroupRepository;
    private final PrivilegeRepository privilegeRepository;

    private final PasswordEncoder passwordEncoder;
    @PostConstruct
    @Transactional
    public void setupCarDummy() {  // 차량 더미 데이터 생성
        { // 아반떼 10대
            CarModel carModel = CarModel.builder()
                    .name("아반떼")
                    .image("/image/car/1.jpeg")
                    .build();
            List<Car> cars = new ArrayList<>();
            for (int i = 0; i < 10; i++) {
                cars.add(Car.builder()
                        .number("11가_111" + i)
                        .status("정상")
                        .carModel(carModel)
                        .comment("코멘트" + (i + 1))
                        .build());
            }
            carModel.setCars(cars);
            carModelRepository.save(carModel);
        }

        { // 쏘나타 10대
            CarModel carModel = CarModel.builder()
                    .name("쏘나타")
                    .image("/image/car/2.jpeg")
                    .build();
            List<Car> cars = new ArrayList<>();
            for (int i = 0; i < 10; i++) {
                cars.add(Car.builder()
                        .number("11가_222" + i)
                        .status("정상")
                        .carModel(carModel)
                        .comment("코멘트" + (i + 1))
                        .build());
            }
            carModel.setCars(cars);
            carModelRepository.save(carModel);
        }

        { // 스포티지 10대
            CarModel carModel = CarModel.builder()
                    .name("스포티지")
                    .image("/image/car/3.jpeg")
                    .build();
            List<Car> cars = new ArrayList<>();
            for (int i = 0; i < 10; i++) {
                cars.add(Car.builder()
                        .number("11가_333" + i)
                        .status("정상")
                        .carModel(carModel)
                        .comment("코멘트" + (i + 1))
                        .build());
            }
            carModel.setCars(cars);
            carModelRepository.save(carModel);
        }

        { // 그랜저 10대
            CarModel carModel = CarModel.builder()
                    .name("그랜저")
                    .image("/image/car/4.jpeg")
                    .build();
            List<Car> cars = new ArrayList<>();
            for (int i = 0; i < 10; i++) {
                cars.add(Car.builder()
                        .number("22가_222" + i)
                        .status("정상")
                        .carModel(carModel)
                        .comment("코멘트" + (i + 1))
                        .build());
            }
            carModel.setCars(cars);
            carModelRepository.save(carModel);
        }

        { // 투싼 10대
            CarModel carModel = CarModel.builder()
                    .name("투싼")
                    .image("/image/car/5.jpeg")
                    .build();
            List<Car> cars = new ArrayList<>();
            for (int i = 0; i < 10; i++) {
                cars.add(Car.builder()
                        .number("33가_111" + i)
                        .status("정상")
                        .carModel(carModel)
                        .comment("코멘트" + (i + 1))
                        .build());
            }
            carModel.setCars(cars);
            carModelRepository.save(carModel);
        }


        // 유저 더미 데이터 생성
        // 기본 그룹 생성
        UserGroup userGroup = UserGroup.builder()
                .name("기본")
                .build();
        userGroupRepository.save(userGroup);

        // 기본 권한 생성
        Privilege readPrivilege = Privilege.builder()
                .name(PrivilegeEnum.READ_PRIVILEGE.getValue())
                .build();
        privilegeRepository.save(readPrivilege);

        // 유저 역할 생성
        Role role = Role.builder()
                .name(RoleEnum.ROLE_USER.getValue())
                .privileges(Set.of(readPrivilege))
                .build();
        roleRepository.save(role);

        for (int i = 0; i < 10; i++) {
            User user = User.builder()
                    .username("user" + (i + 1))
                    .password(passwordEncoder.encode("1234"))
                    .name("참가자" + (i + 1))
                    .userGroup(userGroup)
                    .roles(Set.of(role))
                    .build();
            userGroup.setUsers(List.of(user));
            userRepository.save(user);
        }
    }
}
