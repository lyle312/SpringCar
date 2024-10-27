package springcar.RentalCar.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import springcar.RentalCar.service.LoginService;

import static springcar.RentalCar.service.LoginService.isAuthenticated;

@Slf4j(topic = "loginController")
@Controller
@RequiredArgsConstructor
@RequestMapping("/login")
public class LoginController {
    private final LoginService loginService;
    @GetMapping("")
    public String login() {
        if (isAuthenticated()) {
            return "redirect:/home";
        }
        return "login";
    }
}
