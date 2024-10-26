package springcar.RentalCar.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import springcar.RentalCar.dto.*;
import springcar.RentalCar.dto.car.*;
import springcar.RentalCar.entity.Car;
import springcar.RentalCar.entity.User;
import springcar.RentalCar.service.CarManageService;
import springcar.RentalCar.service.CarRentalService;
import springcar.RentalCar.service.LoginService;
import springcar.RentalCar.utils.Converter;
import springcar.RentalCar.dto.AvailableDate;
import springcar.RentalCar.dto.JsonBody;
import springcar.RentalCar.dto.car.*;

import java.time.LocalDate;
import java.util.List;

@Controller
@PreAuthorize(value = "hasAnyAuthority('READ')")
@RequestMapping("/car")
@RequiredArgsConstructor
public class CarController {
    private final LoginService loginService;
    private final CarManageService carManageService;
    private final CarRentalService carRentalService;
    private final Converter converter;

    /* 차량 대여 */
    @PreAuthorize(value = "hasAnyAuthority('CAR_RENTAL')")
    @GetMapping("/rental")  // 차량 모델 선택 페이지
    public String rentalGet(Model model) {
        List<List<String>> carModelList = carManageService.getAllCarModelNames();
        model.addAttribute("carModelList", carModelList);
        return "car/carModelSelect";
    }

    @PreAuthorize(value = "hasAnyAuthority('CAR_RENTAL')")
    @GetMapping("/rental/{carModel}")  // 차량 대여 페이지
    public String rentalGet(Model model,
                            @PathVariable(value = "carModel", required = false) Long carModelId,
                            @RequestParam(value = "q", required = false) String search) {
        List<CarDto> carDtoList = carManageService.searchCarDto(carModelId, search);
        String[] carInfo = {"차량 번호", "차종", "상태", "메모", "대여"};
        List<List<String>> listList = converter.convertCarDtoList(carDtoList);
        model.addAttribute("carList", listList);
        model.addAttribute("carListTitles", carInfo);
        model.addAttribute("search", search);
        return "car/carRental";
    }

    @PreAuthorize(value = "hasAnyAuthority('CAR_RENTAL')")
    @GetMapping("/rental/{carModel}/{carId}") // 차량 대여 날짜 선택 페이지
    public String rentalGet(@PathVariable("carId") Long carId, Model model) {
        Car car = carManageService.getCar(carId);
        LocalDate now = LocalDate.now();

        List<List<AvailableDate>> dateArr = carRentalService.getAvailableDate(car);

        model.addAttribute("dateArr", dateArr);
        model.addAttribute("carId", carId);
        return "car/carRentalDate";
    }

    @PreAuthorize(value = "hasAnyAuthority('CAR_RENTAL')")
    @GetMapping("/rental/history") // 차량 대여 내역 페이지
    public String rentalHistoryGet(@RequestParam(value = "q", required = false) String search,
                                   Model model) {
        User user = loginService.getLoginUser();
        String[] rentalHistoryTitles = {"차량 번호", "차종", "대여일", "반납일", "상태", "취소/반납"};

        List<List<String>> rentalHistoryList = carRentalService.getRentalHistoryList(user, search);
        model.addAttribute("rentalHistoryTitles", rentalHistoryTitles);
        model.addAttribute("rentalHistoryList", rentalHistoryList);
        model.addAttribute("search", search);
        return "car/carRentalHistory";
    }


    @ResponseBody
    @PreAuthorize(value = "hasAnyAuthority('CAR_RENTAL')")
    @PostMapping("/rental") // 차량 대여 신청
    public JsonBody rentalPost(@Validated @RequestBody RentCarDto rentCarDto) {
        User user = loginService.getLoginUser();
        Car car = carManageService.getCar(rentCarDto.getCarId());
        return JsonBody.builder()
                .message("차량 대여 성공")
                .data(carRentalService.rentCar(rentCarDto, user, car))
                .build();
    }

    @ResponseBody
    @PreAuthorize(value = "hasAnyAuthority('CAR_RENTAL')")
    @PostMapping("/rental/return/{historyId}")  // 차량 반납
    public JsonBody rentalPut(@PathVariable("historyId") Long historyId) {
        return JsonBody.builder()
                .message("차량 반납 성공")
                .data(carRentalService.returnCar(historyId))
                .build();
    }

    @ResponseBody
    @PreAuthorize(value = "hasAnyAuthority('CAR_RENTAL')")
    @PostMapping("/rental/cancel/{historyId}")  // 차량 대여 취소
    public JsonBody rentalCancel(@PathVariable("historyId") Long historyId) {
        return JsonBody.builder()
                .message("차량 대여 취소 성공")
                .data(carRentalService.cancelRental(historyId))
                .build();
    }


    @ResponseBody
    @PreAuthorize(value = "hasAnyAuthority('CAR_RENTAL')")
    @DeleteMapping("/rental/{historyId}")   // 대여 기록 삭제
    public JsonBody rentalDelete(@PathVariable("historyId") Long historyId) {
        return JsonBody.builder()
                .message("차량 대여 취소 성공")
                .data(carRentalService.deleteRental(historyId))
                .build();
    }

    /* 차량 관리 */
    @GetMapping("/manage")  // 차량 페이지
    public String getCar(Model model, @RequestParam(value = "q", required = false) String search) {
        List<CarDto> carDtoList = carManageService.searchCarDto(search);
        String[] carInfo = {"차량 번호", "차종", "상태", "메모", "수정/삭제"};
        List<List<String>> listList = converter.convertCarDtoList(carDtoList);
        model.addAttribute("carList", listList);
        model.addAttribute("carListTitles", carInfo);
        model.addAttribute("search", search);
        return "car/carManage";
    }
    @ResponseBody
    @PreAuthorize("hasAuthority('ADMIN')")
    @PostMapping("/manage") // 새로운 차량 등록
    public JsonBody newCar(@Validated CarDto carDto) {
        return JsonBody.builder()
                .message("차량 등록 성공")
                .data(carManageService.createCar(carDto))
                .build();
    }
    @ResponseBody
    @PreAuthorize("hasAuthority('ADMIN')")
    @PutMapping("/manage")  // 차량 정보 수정
    public JsonBody editCar(@Validated CarDto carDto) {
        return JsonBody.builder()
                .message("차량 정보 수정 성공")
                .data(carManageService.updateCar(carDto))
                .build();
    }
    @ResponseBody
    @PreAuthorize("hasAuthority('ADMIN')")
    @DeleteMapping("/manage")   // 차량 삭제
    public JsonBody deleteCar(@RequestParam("number") String number) {
        return JsonBody.builder()
                .message("차량 삭제 성공")
                .data(carManageService.deleteCar(number))
                .build();
    }

    /* 차량 모델 관리 */
    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping("/model/manage")  // 차량 모델 페이지
    public String getModel(@RequestParam(value = "q", required = false) String search, Model model) {
        List<CarModelRes> carModelResList = carManageService.getAllCarModelDto(search);
        List<List<String>> carModelList = converter.convertCarModelDtoList(carModelResList);
        String[] carModelInfo = {"차종", "이미지 파일", "수정/삭제"};
        model.addAttribute("carModelList", carModelList);
        model.addAttribute("carModelListTitles", carModelInfo);
        model.addAttribute("search", search);
        return "car/carModelManage";
    }

    @ResponseBody
    @PreAuthorize("hasAuthority('ADMIN')")
    @PostMapping("/model/manage") // 새로운 차량 모델 등록
    public JsonBody newModel(@Validated CarModelReq carModelReq) {
        return JsonBody.builder()
                .message("차량 모델 등록 성공")
                .data(carManageService.createCarModel(carModelReq))
                .build();
    }

    @ResponseBody
    @PreAuthorize("hasAuthority('ADMIN')")
    @PutMapping("/model/manage")  // 차량 모델 정보 수정
    public JsonBody editModel(@Validated ModelImageChangeReq carModelReq) {
        return JsonBody.builder()
                .message("차량 모델 정보 수정 성공")
                .data(carManageService.updateCarModel(carModelReq))
                .build();
    }

    @ResponseBody
    @PreAuthorize("hasAuthority('ADMIN')")
    @DeleteMapping("/model/manage/{id}")   // 차량 모델 삭제
    public JsonBody deleteModel(@PathVariable("id") Long id) {
        return JsonBody.builder()
                .message("차량 모델 삭제 성공")
                .data(carManageService.deleteCarModel(id))
                .build();
    }
}
