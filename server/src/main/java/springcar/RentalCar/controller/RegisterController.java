package springcar.RentalCar.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ResponseStatusException;
import springcar.RentalCar.dto.user.UserDto;
import springcar.RentalCar.service.LoginService;

import static springcar.RentalCar.service.LoginService.isAuthenticated;

@Controller
@RequiredArgsConstructor
@RequestMapping("/register")
@Slf4j(topic = "registerController")
public class RegisterController {
    private final LoginService loginService;

    @GetMapping("")
    public String register(Model model) {
        if (isAuthenticated()) {
            return "redirect:/home";
        }
        model.addAttribute("groupNameList", loginService.findAllGroupNames());
        return "register";
    }
    @PostMapping("")
    public String register(@Validated UserDto userDto) {
        if (isAuthenticated()) {
            return "redirect:/home";
        }
        try {
            loginService.register(userDto);
        } catch (ResponseStatusException e) {
            log.error("회원가입 실패", e);
            return "redirect:/register?error";
        }
        return "login";
    }
}
