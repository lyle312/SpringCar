package springcar.RentalCar.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;
import springcar.RentalCar.dto.car.CarDto;
import springcar.RentalCar.dto.car.RentCarDto;
import springcar.RentalCar.entity.*;
import springcar.RentalCar.entity.Car;
import springcar.RentalCar.entity.RentCar;
import springcar.RentalCar.entity.User;
import springcar.RentalCar.repository.RentCarRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.catchThrowable;
import static org.mockito.BDDMockito.given;
import static springcar.RentalCar.service.LoginServiceTest.makeUser;

@ExtendWith(MockitoExtension.class)
@DisplayName("차량 대여 서비스 테스트")
public class CarRentalServiceTest {
    @InjectMocks
    private CarRentalService carRentalService;
    @Mock
    private RentCarRepository rentCarRepository;

    public static RentCarDto makeRentCarDto(Car car) {
        return RentCarDto.builder()
                .id(1L)
                .carId(car.getId())
                .pickupDate(LocalDate.now())
                .returnDate(LocalDate.now().plusDays(7))
                .build();
    }

    public static RentCarDto makeRentCarDto() {
        return RentCarDto.builder()
                .id(1L)
                .carId(1L)
                .pickupDate(LocalDate.now())
                .returnDate(LocalDate.now().plusDays(7))
                .build();
    }

    public static RentCar makeRentalHistory(User user, Car car) {
        return RentCar.builder()
                .id(1L)
                .user(user)
                .car(car)
                .pickupDate(LocalDate.now())
                .returnDate(LocalDate.now().plusDays(7))
                .build();
    }

    public static RentCar makeRentalHistory() {
        return RentCar.builder()
                .id(1L)
                .user(LoginServiceTest.makeUser(LoginServiceTest.makeUserGroup()))
                .car(CarManageServiceTest.makeCar(CarManageServiceTest.makeCarModel()))
                .pickupDate(LocalDate.now())
                .returnDate(LocalDate.now().plusDays(7))
                .build();
    }


    @Test
    @DisplayName("차량 대여 성공")
    public void rentCar() {
        // given
        User user = LoginServiceTest.makeUser();
        Car car = CarManageServiceTest.makeCar();
        RentCarDto rentCarDto = makeRentCarDto(car);

        given(rentCarRepository.findByCar_IdAndReturnDateGreaterThanEqualAndPickupDateLessThanEqual
                (makeRentCarDto().getCarId(), rentCarDto.getPickupDate(), rentCarDto.getReturnDate())).willReturn(List.of()); // 대여 기록이 없는 상태
        given(rentCarRepository.save(Mockito.any(RentCar.class))).willReturn(makeRentalHistory()); // 대여 기록 저장 성공

        // when
        RentCarDto res = carRentalService.rentCar(rentCarDto, user, car);

        // then
        assertThat(res).isNotNull();
        assertThat(res.getId()).isEqualTo(rentCarDto.getId());
        assertThat(res.getCarId()).isEqualTo(rentCarDto.getCarId());
        assertThat(res.getPickupDate()).isEqualTo(rentCarDto.getPickupDate());
        assertThat(res.getReturnDate()).isEqualTo(rentCarDto.getReturnDate());
    }

    @Test
    @DisplayName("차량 대여 실패 - 이미 대여된 차량")
    public void rentCarFail() {
        // given
        User user = LoginServiceTest.makeUser();
        Car car = CarManageServiceTest.makeCar();
        RentCarDto rentCarDto = makeRentCarDto(car);
        RentCar rentCar = makeRentalHistory(user, car);

        given(rentCarRepository.findByCar_IdAndReturnDateGreaterThanEqualAndPickupDateLessThanEqual
                (rentCarDto.getCarId(), rentCarDto.getPickupDate(), rentCarDto.getReturnDate())).willReturn(List.of(rentCar)); // 대여 기록이 있는 상태

        // when
        Throwable thrown = catchThrowable(() -> carRentalService.rentCar(rentCarDto, user, car));

        // then
        assertThat(thrown).isInstanceOf(ResponseStatusException.class).hasMessage("400 BAD_REQUEST \"이미 대여된 차량입니다.\"");
    }

    @Test
    @DisplayName("차량 대여 실패 - 대여일이 오늘 이전")
    public void rentCarFail2() {
        // given
        User user = LoginServiceTest.makeUser();
        Car car = CarManageServiceTest.makeCar();
        RentCarDto rentCarDto = makeRentCarDto(car);
        rentCarDto.setPickupDate(LocalDate.now().minusDays(1));

        // when
        Throwable thrown = catchThrowable(() -> carRentalService.rentCar(rentCarDto, user, car));

        // then
        assertThat(thrown).isInstanceOf(ResponseStatusException.class).hasMessage("400 BAD_REQUEST \"대여는 오늘부터 가능합니다.\"");
    }

    @Test
    @DisplayName("차량 대여 실패 - 반납일이 대여일보다 빠르거나 같음")
    public void rentCarFail3() {
        // given
        User user = LoginServiceTest.makeUser();
        Car car = CarManageServiceTest.makeCar();
        RentCarDto rentCarDto = makeRentCarDto(car);
        rentCarDto.setReturnDate(rentCarDto.getPickupDate());

        // when
        Throwable thrown = catchThrowable(() -> carRentalService.rentCar(rentCarDto, user, car));

        // then
        assertThat(thrown).isInstanceOf(ResponseStatusException.class).hasMessage("400 BAD_REQUEST \"반납일이 대여일보다 빠르거나 같을 수 없습니다.\"");
    }

    @Test
    @DisplayName("차량 대여 실패 - 대여일로부터 7일 이후 반납")
    public void rentCarFail4() {
        // given
        User user = LoginServiceTest.makeUser();
        Car car = CarManageServiceTest.makeCar();
        RentCarDto rentCarDto = makeRentCarDto(car);
        rentCarDto.setReturnDate(rentCarDto.getPickupDate().plusDays(9));

        // when
        Throwable thrown = catchThrowable(() -> carRentalService.rentCar(rentCarDto, user, car));

        // then
        assertThat(thrown).isInstanceOf(ResponseStatusException.class).hasMessage("400 BAD_REQUEST \"7일 이내로만 대여할 수 있습니다.\"");
    }

    @Test
    @DisplayName("차량 반납 성공")
    public void returnCar() {
        // given
        User user = LoginServiceTest.makeUser();
        Car car = CarManageServiceTest.makeCar();
        RentCarDto rentCarDto = makeRentCarDto(car);
        RentCar rentCar = makeRentalHistory();

        given(rentCarRepository.findByUser_IdAndCarIdAndPickupDateAndReturnDate
                (user.getId(), car.getId(), rentCarDto.getPickupDate(), rentCarDto.getReturnDate())).willReturn(Optional.of(rentCar)); // 대여 기록이 있는 상태

        // when
        RentCarDto res = carRentalService.returnCar(rentCarDto, user, car);

        // then
        assertThat(res).isNotNull();
        assertThat(res.getId()).isEqualTo(rentCarDto.getId());
        assertThat(res.getCarId()).isEqualTo(car.getId());
        assertThat(res.getPickupDate()).isEqualTo(rentCarDto.getPickupDate());
        assertThat(res.getReturnDate()).isEqualTo(LocalDate.now());  // 반납 완료시 오늘 날짜로 설정
    }

    @Test
    @DisplayName("차량 반납 실패 - 대여 기록이 없음")
    public void returnCarFail() {
        // given
        User user = LoginServiceTest.makeUser();
        Car car = CarManageServiceTest.makeCar();
        RentCarDto rentCarDto = makeRentCarDto(car);

        given(rentCarRepository.findByUser_IdAndCarIdAndPickupDateAndReturnDate
                (user.getId(), car.getId(), rentCarDto.getPickupDate(), rentCarDto.getReturnDate())).willReturn(Optional.empty()); // 대여 기록이 없는 상태

        // when
        Throwable thrown = catchThrowable(() -> carRentalService.returnCar(rentCarDto, user, car));

        // then
        assertThat(thrown).isInstanceOf(ResponseStatusException.class).hasMessage("400 BAD_REQUEST \"예약되지 않은 차량입니다.\"");
    }

    @Test
    @DisplayName("차량 반납 실패 - 이미 반납된 차량")
    public void returnCarFail2() {
        // given
        User user = LoginServiceTest.makeUser();
        Car car = CarManageServiceTest.makeCar();
        RentCarDto rentCarDto = makeRentCarDto(car);
        RentCar rentCar = makeRentalHistory();
        rentCar.setReturnDate(LocalDate.now().minusDays(1));

        given(rentCarRepository.findByUser_IdAndCarIdAndPickupDateAndReturnDate
                (user.getId(), car.getId(), rentCarDto.getPickupDate(), rentCarDto.getReturnDate())).willReturn(Optional.of(rentCar)); // 대여 기록이 있는 상태

        // when
        Throwable thrown = catchThrowable(() -> carRentalService.returnCar(rentCarDto, user, CarManageServiceTest.makeCar()));

        // then
        assertThat(thrown).isInstanceOf(ResponseStatusException.class).hasMessage("400 BAD_REQUEST \"이미 반납된 차량입니다.\"");
    }

    @Test
    @DisplayName("차량 반납 실패 - 아직 대여일이 되지 않음")
    public void returnCarFail3() {
        // given
        User user = LoginServiceTest.makeUser();
        Car car = CarManageServiceTest.makeCar();
        RentCarDto rentCarDto = makeRentCarDto();
        RentCar rentCar = makeRentalHistory();
        rentCar.setPickupDate(LocalDate.now().plusDays(1));

        given(rentCarRepository.findByUser_IdAndCarIdAndPickupDateAndReturnDate
                (user.getId(), car.getId(), rentCarDto.getPickupDate(), rentCarDto.getReturnDate())).willReturn(Optional.of(rentCar)); // 대여 기록이 있는 상태

        // when
        Throwable thrown = catchThrowable(() -> carRentalService.returnCar(rentCarDto, user, CarManageServiceTest.makeCar()));

        // then
        assertThat(thrown).isInstanceOf(ResponseStatusException.class).hasMessage("400 BAD_REQUEST \"아직 대여일이 되지 않은 차량입니다.\"");
    }
    @Test
    @DisplayName("차량 대여 내역 조회 - 오늘 이후로만 조회")
    public void getRentalHistory() {
        // given
        User user = LoginServiceTest.makeUser();
        Car car = CarManageServiceTest.makeCar();
        Long carId = car.getId();
        RentCar rentCar = makeRentalHistory();
        List<CarDto> carDtoList = List.of(CarManageServiceTest.makeCarDto());

        given(rentCarRepository.findByCar_IdAndReturnDateGreaterThanEqual
                (carId, LocalDate.now())).willReturn(List.of(rentCar)); // 대여 기록이 있는 상태
        // when
        Map<Long, List<RentCar>> res = carRentalService.getRentalHistory(carDtoList);

        // then
        assertThat(res).isNotNull();
        assertThat(res.get(carId)).isNotNull();
        assertThat(res.get(carId).get(0).getId()).isEqualTo(rentCar.getId());
        assertThat(res.get(carId).get(0).getUser().getId()).isEqualTo(user.getId());
        assertThat(res.get(carId).get(0).getCar().getId()).isEqualTo(car.getId());
        assertThat(res.get(carId).get(0).getPickupDate()).isEqualTo(rentCar.getPickupDate());
        assertThat(res.get(carId).get(0).getReturnDate()).isEqualTo(rentCar.getReturnDate());
    }

    @Test
    @DisplayName("차량 대여 취소 성공")
    public void deleteRental() {
        // given
        User user = LoginServiceTest.makeUser();
        Car car = CarManageServiceTest.makeCar();
        RentCarDto rentCarDto = makeRentCarDto(car);
        RentCar rentCar = makeRentalHistory();

        given(rentCarRepository.findByUser_IdAndCarIdAndPickupDateAndReturnDate
                (user.getId(), car.getId(), rentCarDto.getPickupDate(), rentCarDto.getReturnDate())).willReturn(Optional.of(rentCar)); // 대여 기록이 있는 상태

        // when
        RentCarDto res = carRentalService.deleteRental(rentCarDto, user, car);

        // then
        assertThat(res).isNotNull();
        assertThat(res.getId()).isEqualTo(rentCarDto.getId());
        assertThat(res.getCarId()).isEqualTo(car.getId());
        assertThat(res.getPickupDate()).isEqualTo(rentCarDto.getPickupDate());
        assertThat(res.getReturnDate()).isEqualTo(rentCarDto.getReturnDate());
    }

    @Test
    @DisplayName("차량 대여 취소 실패 - 대여 기록이 없음")
    public void deleteRentalFail() {
        // given
        User user = LoginServiceTest.makeUser();
        Car car = CarManageServiceTest.makeCar();
        RentCarDto rentCarDto = makeRentCarDto(car);

        given(rentCarRepository.findByUser_IdAndCarIdAndPickupDateAndReturnDate
                (user.getId(), car.getId(), rentCarDto.getPickupDate(), rentCarDto.getReturnDate())).willReturn(Optional.empty()); // 대여 기록이 없는 상태

        // when
        Throwable thrown = catchThrowable(() -> carRentalService.deleteRental(rentCarDto, user, car));

        // then
        assertThat(thrown).isInstanceOf(ResponseStatusException.class).hasMessage("400 BAD_REQUEST \"예약되지 않은 차량입니다.\"");
    }
}
