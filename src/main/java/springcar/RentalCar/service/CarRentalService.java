package springcar.RentalCar.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import springcar.RentalCar.dto.AvailableDate;
import springcar.RentalCar.dto.car.CarDto;
import springcar.RentalCar.dto.car.RentCarDto;
import springcar.RentalCar.entity.Car;
import springcar.RentalCar.entity.RentCar;
import springcar.RentalCar.entity.User;
import springcar.RentalCar.repository.RentCarRepository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j(topic = "CarRentalService")
@Transactional
@RequiredArgsConstructor
public class CarRentalService {
    private final static String DATE_ERROR_MESSAGE_PREFIX = "대여는 오늘부터 가능합니다.";
    private final static String DATE_ERROR_MESSAGE_SUFFIX = "반납일이 대여일보다 빠를 수 없습니다.";
    private final static String DATE_ERROR_MESSAGE_OVER_21 = "21일 이내로만 신청할 수 있습니다.";
    private final static String DATE_ERROR_MESSAGE_OVER_7 = "최대 7일동안 대여할 수 있습니다.";
    private final static String DATE_ERROR_MESSAGE_RESERVED = "이미 예약된 날짜입니다.";
    private final static String DATE_ERROR_MESSAGE_NOT_RENTED = "아직 대여하지 않은 차량입니다.";
    private final static String DATE_ERROR_MESSAGE_ALREADY_RETURNED = "이미 반납된 차량입니다.";
    private final static String DATE_ERROR_MESSAGE_NOT_RESERVED = "예약되지 않은 차량입니다.";


    private final RentCarRepository rentCarRepository;

    /**
     * <h3>차량 대여 날짜 검사.</h3>
     * */
    private void validatePickUpDateAndReturnDate(RentCarDto rentCarDto) {
        // 겹치는 차량 예약 내역이 있으면 ResponseStatusException 발생
        if (rentCarRepository.existsByCar_IdAndReturnDateGreaterThanEqualAndPickupDateLessThanEqualAndIsReturned(
                rentCarDto.getCarId(), rentCarDto.getPickupDate(), rentCarDto.getReturnDate(), false)) {
            log.error(DATE_ERROR_MESSAGE_RESERVED);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, DATE_ERROR_MESSAGE_RESERVED);
        }
        // 대여일이 오늘 이전이면 ResponseStatusException 발생
        if (rentCarDto.getPickupDate().isBefore(LocalDate.now())) {
            log.error(DATE_ERROR_MESSAGE_PREFIX);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, DATE_ERROR_MESSAGE_PREFIX);
        }
        // 반납일이 대여일보다 빠르면 ResponseStatusException 발생
        if (rentCarDto.getReturnDate().isBefore(rentCarDto.getPickupDate())) {
            log.error(DATE_ERROR_MESSAGE_SUFFIX);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, DATE_ERROR_MESSAGE_SUFFIX);
        }
        // 오늘로부터 21일 이후 반납이면 ResponseStatusException 발생
        if (rentCarDto.getReturnDate().isAfter(LocalDate.now().plusDays(20))) {
            log.error(DATE_ERROR_MESSAGE_OVER_21);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, DATE_ERROR_MESSAGE_OVER_21);
        }
        // 대여일로부터 7일 이후 반납이면 ResponseStatusException 발생
        if (rentCarDto.getReturnDate().isAfter(rentCarDto.getPickupDate().plusDays(6))) {
            log.error(DATE_ERROR_MESSAGE_OVER_7);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, DATE_ERROR_MESSAGE_OVER_7);
        }
    }

    /**
     * <h3>차량 대여.</h3>
     * 차량 번호가 존재하지 않으면, 이미 대여된 차량이면, 대여일이 오늘 이전이면,
     * 반납일이 대여일보다 빠르거나 같으면, 대여일로부터 7일 이후 반납이면 ResponseStatusException 발생
     */
    public RentCarDto rentCar(RentCarDto rentCarDto, User user, Car car) {
        rentCarDto.setCarId(car.getId());
        rentCarDto.setIsReturned(false);
        validatePickUpDateAndReturnDate(rentCarDto);

        RentCar rentCar = RentCar.builder()
                .car(car)
                .user(user)
                .pickupDate(rentCarDto.getPickupDate())
                .returnDate(rentCarDto.getReturnDate())
                .isReturned(false)
                .build();
        try {
            rentCarRepository.save(rentCar);
        } catch (Exception e) {
            log.error("대여 기록 저장 실패", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "대여 기록 저장 실패");
        }
        return rentCarDto;
    }

    /**
     * <h3>차량 반납.</h3>
     * 차량 번호가 존재하지 않으면, 대여 기록이 없으면 ResponseStatusException 발생
     */
    public RentCarDto returnCar(RentCarDto rentCarDto, User user, Car car) {
        // 대여 기록이 없으면 ResponseStatusException 발생
        RentCar rentCar = rentCarRepository
                .findByUser_IdAndCarIdAndPickupDateAndReturnDate(user.getId(), car.getId(), rentCarDto.getPickupDate(), rentCarDto.getReturnDate()).orElseThrow(
                    () -> new ResponseStatusException(HttpStatus.BAD_REQUEST, DATE_ERROR_MESSAGE_NOT_RESERVED));
        if (rentCar.getIsReturned()) {
            log.error(DATE_ERROR_MESSAGE_ALREADY_RETURNED);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, DATE_ERROR_MESSAGE_ALREADY_RETURNED);
        }
        if (rentCar.getPickupDate().isAfter(LocalDate.now())) {
            log.error(DATE_ERROR_MESSAGE_NOT_RENTED);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, DATE_ERROR_MESSAGE_NOT_RENTED);
        }

        rentCar.setReturnDate(LocalDate.now());
        rentCar.setIsReturned(true);
        return RentCarDto.builder()
                .id(rentCar.getId())
                .carId(car.getId())
                .pickupDate(rentCar.getPickupDate())
                .returnDate(rentCar.getReturnDate())
                .isReturned(true)
                .build();
    }

    public RentCarDto returnCar(Long historyId) {
        RentCar rentCar = rentCarRepository.findById(historyId).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.BAD_REQUEST, DATE_ERROR_MESSAGE_NOT_RESERVED));
        if (rentCar.getIsReturned()) {
            log.error(DATE_ERROR_MESSAGE_ALREADY_RETURNED);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, DATE_ERROR_MESSAGE_ALREADY_RETURNED);
        }
        if (rentCar.getPickupDate().isAfter(LocalDate.now())) {
            log.error(DATE_ERROR_MESSAGE_NOT_RENTED);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, DATE_ERROR_MESSAGE_NOT_RENTED);
        }
        rentCar.setReturnDate(LocalDate.now());
        rentCar.setIsReturned(true);
        return RentCarDto.builder()
                .id(rentCar.getId())
                .userId(rentCar.getUser().getId())
                .carId(rentCar.getCar().getId())
                .pickupDate(rentCar.getPickupDate())
                .returnDate(rentCar.getReturnDate())
                .isReturned(true)
                .build();
    }

    /**
     * <h3>차량 대여 내역.</h3>
     * 오늘 이후 날짜로 차량 예약 내역을 반환
     */
    public Map<Long, List<RentCar>> getRentalHistory(List<CarDto> carDtoList) {
        Map<Long, List<RentCar>> rentalHistoryMap = new HashMap<>();

        carDtoList.forEach(carDto -> {
            Long id = carDto.getId();
            rentalHistoryMap.put(id, rentCarRepository.findByCar_IdAndReturnDateGreaterThanEqualAndIsReturned(id, LocalDate.now(), false));
        });

        return rentalHistoryMap;
    }

    public List<RentCar> getRentalHistory(User user) {
        return rentCarRepository.findByUser_Id(user.getId());
    }

    /**
     * <h3>차량 대여 취소.</h3>
     * 차량 번호가 존재하지 않으면, 대여 기록이 없으면 ResponseStatusException 발생
     */
    public RentCarDto deleteRental(RentCarDto rentCarDto, User user, Car car) {
        // 대여 기록이 없으면 ResponseStatusException 발생
        RentCar rentCar = rentCarRepository
                .findByUser_IdAndCarIdAndPickupDateAndReturnDate(user.getId(), car.getId(), rentCarDto.getPickupDate(), rentCarDto.getReturnDate()).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.BAD_REQUEST, DATE_ERROR_MESSAGE_NOT_RESERVED));

        try {
            rentCarRepository.delete(rentCar);
        } catch (Exception e) {
            log.error("대여 기록 삭제 실패", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "대여 기록 삭제 실패");
        }
        return RentCarDto.builder()
                .id(rentCar.getId())
                .carId(car.getId())
                .pickupDate(rentCar.getPickupDate())
                .returnDate(rentCar.getReturnDate())
                .isReturned(rentCar.getIsReturned())
                .build();
    }

    public RentCarDto deleteRental(Long historyId) {
        // 대여 기록이 없으면 ResponseStatusException 발생
        RentCar rentCar = rentCarRepository.findById(historyId).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.BAD_REQUEST, DATE_ERROR_MESSAGE_NOT_RESERVED));
        try {
            rentCarRepository.delete(rentCar);
        } catch (Exception e) {
            log.error("대여 기록 삭제 실패", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "대여 기록 삭제 실패");
        }
        return RentCarDto.builder()
                .id(rentCar.getId())
                .carId(rentCar.getCar().getId())
                .pickupDate(rentCar.getPickupDate())
                .returnDate(rentCar.getReturnDate())
                .isReturned(rentCar.getIsReturned())
                .build();
    }

    /**
     * <h3>차량 대여 가능 날짜.</h3>
    * */
    public List<List<AvailableDate>> getAvailableDate(Car car) {
        // 오늘 이후 대여 날짜를 가져옴
        List<RentCar> unavailableDate = rentCarRepository.findByCar_IdAndReturnDateGreaterThanEqualAndIsReturned(
                car.getId(), LocalDate.now(), false);
        List<List<AvailableDate>> availableDate = new ArrayList<>();

        // 3주치 날짜를 미리 만들어놓고, 대여 기록이 있는 날짜는 제거
        LocalDate now = LocalDate.now();
        for (int i = 0; i < 5; i++) {
            availableDate.add(new ArrayList<>());
            for (int j = 0; j < 7; j++) {
                LocalDate date = now.plusDays(i * 7 + j);

                AvailableDate carAvailableDate = AvailableDate.builder()
                        .day(date.getDayOfMonth())
                        .available(true)
                        .build();

                if (i >= 3) {
                    carAvailableDate.setAvailable(false);
                    availableDate.get(i).add(carAvailableDate);
                    continue;
                }

                // 대여 기록이 있는 날짜를 제거
                for (RentCar rentCar : unavailableDate) {
                    if ((date.isEqual(rentCar.getPickupDate()) || date.isAfter(rentCar.getPickupDate()))
                            && (date.isEqual(rentCar.getReturnDate()) || date.isBefore(rentCar.getReturnDate()))) {
                        carAvailableDate.setAvailable(false);
                        break;
                    }
                }
                availableDate.get(i).add(carAvailableDate);
            }
        }

        return availableDate;
    }

    public List<RentCarDto> getRentalHistoryDto(User user) {
        List<RentCar> rentCarList = rentCarRepository.findByUser_Id(user.getId());
        List<RentCarDto> rentCarDtoList = new ArrayList<>();

        rentCarList.forEach(rentalHistory -> {
            rentCarDtoList.add(RentCarDto.builder()
                    .id(rentalHistory.getId())
                    .userId(rentalHistory.getUser().getId())
                    .carId(rentalHistory.getCar().getId())
                    .pickupDate(rentalHistory.getPickupDate())
                    .returnDate(rentalHistory.getReturnDate())
                    .isReturned(rentalHistory.getIsReturned())
                    .build());
        });

        return rentCarDtoList;
    }

    public List<List<String>> getRentalHistoryList(User user, String search) {
        List<RentCar> rentCarList = new ArrayList<>();
        List<List<String>> listList = new ArrayList<>();

        if (search == null || search.isEmpty()) {
            rentCarList.addAll(rentCarRepository.findByUser_Id(user.getId()));
        } else {
            rentCarList.addAll(rentCarRepository.findByUser_IdAndCar_NumberContaining(user.getId(), search));
            rentCarList.addAll(rentCarRepository.findByUser_IdAndCar_CarModel_NameContaining(user.getId(), search));
        }

        for (RentCar rentCar : rentCarList) {
            List<String> list = new ArrayList<>();

            list.add(rentCar.getId().toString());
            list.add(rentCar.getCar().getNumber());
            list.add(rentCar.getCar().getCarModel().getName());
            list.add(rentCar.getPickupDate().toString());
            list.add(rentCar.getReturnDate().toString());

            if (rentCar.getIsReturned()) {
                if (rentCar.getPickupDate().isAfter(LocalDate.now()))
                    list.add("취소 완료");
                else
                    list.add("반납 완료");
            } else if (rentCar.getPickupDate().isAfter(LocalDate.now())) {
                list.add("대여 신청");
            } else {
                list.add("대여 중");
            }
            list.add(rentCar.getCar().getCarModel().getImage());

            listList.add(list);
        }

        return listList;
    }

    public RentCarDto cancelRental(Long historyId) {
        RentCar rentCar = rentCarRepository.findById(historyId).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.BAD_REQUEST, DATE_ERROR_MESSAGE_NOT_RESERVED));
        if (rentCar.getIsReturned()) {
            log.error(DATE_ERROR_MESSAGE_ALREADY_RETURNED);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, DATE_ERROR_MESSAGE_ALREADY_RETURNED);
        }
        rentCar.setIsReturned(true);
        return RentCarDto.builder()
                .id(rentCar.getId())
                .userId(rentCar.getUser().getId())
                .carId(rentCar.getCar().getId())
                .pickupDate(rentCar.getPickupDate())
                .returnDate(rentCar.getReturnDate())
                .isReturned(true)
                .build();
    }
}
