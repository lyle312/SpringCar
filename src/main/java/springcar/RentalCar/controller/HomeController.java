package springcar.RentalCar.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Slf4j(topic = "HomeController")
@Controller
@RequiredArgsConstructor
@RequestMapping("/home")
public class HomeController {
    @GetMapping("")
    public String home() {
        return "home";
    }
}
