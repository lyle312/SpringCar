package springcar.RentalCar.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import springcar.RentalCar.dto.user.UserDto;
import springcar.RentalCar.service.LoginService;

@Controller
@RequiredArgsConstructor
@PreAuthorize(value = "isAuthenticated()")
@RequestMapping("/mypage")
public class MyPageController {
    private final LoginService loginService;
    @RequestMapping("")
    public String mypage(Model model) {
        UserDto userDto = loginService.getLoginUserDto();

        model.addAttribute("userDto", userDto);
        return "myPage";
    }
}
