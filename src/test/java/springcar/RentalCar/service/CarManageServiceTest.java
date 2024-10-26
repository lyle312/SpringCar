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
import springcar.RentalCar.entity.Car;
import springcar.RentalCar.entity.CarModel;
import springcar.RentalCar.repository.CarModelRepository;
import springcar.RentalCar.repository.CarRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;

@ExtendWith(MockitoExtension.class)
@DisplayName("차량 관리 서비스 테스트")
public class CarManageServiceTest {
    @InjectMocks
    private CarManageService carManageService;
    @Mock
    private CarRepository carRepository;
    @Mock
    private CarModelRepository carModelRepository;

    public static CarDto makeCarDto() {
        return CarDto.builder()
                .id(1L)
                .number("12가1234")
                .model("아반떼")
                .status("정상")
                .comment("테스트")
                .build();
    }

    public static CarModel makeCarModel() {
        return CarModel.builder()
                .id(1L)
                .name("아반떼")
                .build();
    }

    public static Car makeCar(CarModel carModel) {
        return Car.builder()
                .id(1L)
                .number("12가1234")
                .carModel(carModel)
                .status("정상")
                .comment("테스트")
                .build();
    }

    public static Car makeCar() {
        return Car.builder()
                .id(1L)
                .number("12가1234")
                .carModel(makeCarModel())
                .status("정상")
                .comment("테스트")
                .build();
    }

    @Test
    @DisplayName("차량 등록 성공")
    public void createCarSuccess() {
        // given
        CarDto newCarReq = makeCarDto();
        CarModel carModel = makeCarModel();
        // 차량 번호 중복 검사
        given(carRepository.existsByNumber(newCarReq.getNumber())).willReturn(false); // 차량 번호 중복 없음
        // 차량 종류 검사
        given(carModelRepository.findByName(newCarReq.getModel())).willReturn(Optional.of(carModel)); // 차량 종류 있음
        given(carRepository.save(Mockito.any(Car.class))).willReturn(Mockito.any(Car.class));

        // when
        CarDto carDto = carManageService.createCar(newCarReq);

        // then
        assertThat(carDto).isInstanceOf(CarDto.class);
        assertThat(carDto.getNumber()).isEqualTo(newCarReq.getNumber());
        assertThat(carDto.getModel()).isEqualTo(newCarReq.getModel());
        assertThat(carDto.getStatus()).isEqualTo(newCarReq.getStatus());
        assertThat(carDto.getComment()).isEqualTo(newCarReq.getComment());
    }

    @Test
    @DisplayName("차량 등록 실패 - 번호 중복")
    public void createCarFail1() {
        // given
        CarDto newCarReq = makeCarDto();
        given(carRepository.existsByNumber(newCarReq.getNumber())).willReturn(true);

        // when
        Throwable thrown = catchThrowable(() -> carManageService.createCar(newCarReq));

        // then
        assertThat(thrown).isInstanceOf(ResponseStatusException.class)
                .hasMessage("400 BAD_REQUEST \"이미 존재하는 차량 번호입니다.\"");
    }

    @Test
    @DisplayName("차량 등록 실패 - 차량 종류 없음")
    public void createCarFail2() {
        // given
        CarDto newCarReq = makeCarDto();
        given(carRepository.existsByNumber(newCarReq.getNumber())).willReturn(false);
        given(carModelRepository.findByName(newCarReq.getModel())).willReturn(Optional.empty());

        // when
        Throwable thrown = catchThrowable(() -> carManageService.createCar(newCarReq));

        // then
        assertThat(thrown).isInstanceOf(ResponseStatusException.class)
                .hasMessage("400 BAD_REQUEST \"존재하지 않는 차량 종류입니다.\"");
    }

    @Test
    @DisplayName("차량 등록 실패 - 차량 저장 실패")
    public void createCarFail3() {
        // given
        CarDto newCarReq = makeCarDto();
        given(carRepository.existsByNumber(newCarReq.getNumber())).willReturn(false);
        given(carModelRepository.findByName(newCarReq.getModel())).willReturn(Optional.of(CarModel.builder()
                .name(newCarReq.getModel())
                .build()));
        given(carRepository.save(Mockito.any(Car.class))).willThrow(new RuntimeException());

        // when
        Throwable thrown = catchThrowable(() -> carManageService.createCar(newCarReq));

        // then
        assertThat(thrown).isInstanceOf(ResponseStatusException.class)
                .hasMessage("500 INTERNAL_SERVER_ERROR \"차량 저장 실패\"");
    }

    @Test
    @DisplayName("차량 정보 조회 성공")
    public void getCarSuccess() {
        // given
        CarModel carModel = makeCarModel();
        Car car = makeCar(carModel);
        Long id = car.getId();
        given(carRepository.findById(id)).willReturn(
                Optional.of(car));

        // when
        Car res = carManageService.getCar(id);

        // then
        assertThat(res).isInstanceOf(Car.class);
        assertThat(res.getId()).isEqualTo(id);
        assertThat(res.getNumber()).isEqualTo(car.getNumber());
        assertThat(res.getCarModel()).isEqualTo(carModel);
        assertThat(res.getStatus()).isEqualTo("정상");
        assertThat(res.getComment()).isEqualTo("테스트");
    }

    @Test
    @DisplayName("차량 정보 조회 실패 - 존재하지 않는 차량 번호")
    public void getCarFail() {
        // given
        Long id = 1L;
        given(carRepository.findById(id)).willReturn(Optional.empty());

        // when
        Throwable thrown = catchThrowable(() -> carManageService.getCar(id));

        // then
        assertThat(thrown).isInstanceOf(ResponseStatusException.class)
                .hasMessage("400 BAD_REQUEST \"존재하지 않는 차량 번호입니다.\"");
    }

    @Test
    @DisplayName("모든 차량 정보 조회 성공")
    public void getAllCarSuccess() {
        // given
        CarModel carModel = makeCarModel();
        Car car = makeCar(carModel);
        List<Car> carList = new ArrayList<>();
        carList.add(car);
        given(carRepository.findAll()).willReturn(carList);

        // when
        List<CarDto> res = carManageService.getAllCar();

        // then
        CarDto resCar = res.get(0);
        assertThat(res).isInstanceOf(List.class);
        assertThat(res.size()).isEqualTo(1);
        assertThat(resCar).isInstanceOf(CarDto.class);
        assertThat(resCar.getId()).isEqualTo(car.getId());
        assertThat(resCar.getNumber()).isEqualTo(car.getNumber());
        assertThat(resCar.getModel()).isEqualTo(carModel.getName());
        assertThat(resCar.getStatus()).isEqualTo(car.getStatus());
        assertThat(resCar.getComment()).isEqualTo(car.getComment());
    }

    @Test
    @DisplayName("모든 차량 정보 조회 성공 - 차량 없음")
    public void getAllCarSuccess2() {
        // given
        List<Car> carList = new ArrayList<>();
        given(carRepository.findAll()).willReturn(carList);

        // when
        List<CarDto> res = carManageService.getAllCar();

        // then
        assertThat(res).isInstanceOf(List.class);
        assertThat(res.size()).isEqualTo(0);
    }

    @Test
    @DisplayName("차량 정보 수정 성공")
    public void updateCarSuccess() {
        // given
        CarDto carDto = makeCarDto();
        CarModel carModel = makeCarModel();
        Car car = makeCar(carModel);
        given(carRepository.findByNumber(carDto.getNumber())).willReturn(Optional.of(car));

        // when
        CarDto res = carManageService.updateCar(carDto);

        // then
        assertThat(res).isInstanceOf(CarDto.class);
        assertThat(res.getNumber()).isEqualTo(carDto.getNumber());
        assertThat(res.getModel()).isEqualTo(carDto.getModel());
        assertThat(res.getStatus()).isEqualTo(carDto.getStatus());
        assertThat(res.getComment()).isEqualTo(carDto.getComment());
    }

    @Test
    @DisplayName("차량 정보 수정 실패 - 존재하지 않는 차량 번호")
    public void updateCarFail() {
        // given
        CarDto carDto = makeCarDto();
        given(carRepository.findByNumber(carDto.getNumber())).willReturn(Optional.empty());
        // when
        Throwable thrown = catchThrowable(() -> carManageService.updateCar(carDto));
        // then
        assertThat(thrown).isInstanceOf(ResponseStatusException.class)
                .hasMessage("400 BAD_REQUEST \"존재하지 않는 차량 번호입니다.\"");
    }

    @Test
    @DisplayName("차량 삭제 성공")
    public void deleteCarSuccess() {
        // given
        CarModel carModel = makeCarModel();
        Car car = makeCar(carModel);
        String number = car.getNumber();
        given(carRepository.findByNumber(number)).willReturn(Optional.of(car));

        // when
        CarDto res = carManageService.deleteCar(number);

        // then
        assertThat(res).isInstanceOf(CarDto.class);
        assertThat(res.getNumber()).isEqualTo(number);
        assertThat(res.getModel()).isEqualTo(carModel.getName());
        assertThat(res.getStatus()).isEqualTo(car.getStatus());
        assertThat(res.getComment()).isEqualTo(car.getComment());
    }

    @Test
    @DisplayName("차량 삭제 실패 - 존재하지 않는 차량 번호")
    public void deleteCarFail1() {
        // given
        String number = "34나1234";
        given(carRepository.findByNumber(number)).willReturn(Optional.empty());

        // when
        Throwable thrown = catchThrowable(() -> carManageService.deleteCar(number));

        // then
        assertThat(thrown).isInstanceOf(ResponseStatusException.class)
                .hasMessage("400 BAD_REQUEST \"존재하지 않는 차량 번호입니다.\"");
    }

    @Test
    @DisplayName("모든 삭제 실패 - 저장 실패")
    public void deleteCarFail2() {
        // given
        CarModel carModel = makeCarModel();
        Car car = makeCar(carModel);
        String number = car.getNumber();
        given(carRepository.findByNumber(number)).willReturn(Optional.of(car));
        willThrow(new RuntimeException()).given(carRepository).delete(car);

        // when
        Throwable thrown = catchThrowable(() -> carManageService.deleteCar(number));

        // then
        assertThat(thrown).isInstanceOf(ResponseStatusException.class)
                .hasMessage("500 INTERNAL_SERVER_ERROR \"차량 삭제 실패\"");
    }
}
