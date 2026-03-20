package com.mipt.portal.users.controller;

import com.mipt.portal.address.Address;
import com.mipt.portal.users.User;
import com.mipt.portal.users.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Optional;

@Slf4j
@Controller
@RequiredArgsConstructor
public class RegistrationController {

  private final UserService userService;

  @GetMapping("/register")
  public String showRegistrationForm() {
    return "register";
  }

  @PostMapping("/register")
  public String registerUser(
      @RequestParam String email,
      @RequestParam String name,
      @RequestParam String password,
      @RequestParam String passwordAgain,
      @RequestParam(required = false) String addressFull,
      @RequestParam(required = false) String addressCity,
      @RequestParam(required = false) String addressStreet,
      @RequestParam(required = false) String addressHouseNumber,
      @RequestParam(required = false) String addressBuilding,
      @RequestParam String studyProgram,
      @RequestParam(defaultValue = "1") int course,
      Model model) {

    // Создаем объект Address
    Address address = new Address(addressFull);

    if (addressCity != null && !addressCity.isEmpty()) {
      address.setCity(addressCity);
    }
    if (addressStreet != null && !addressStreet.isEmpty()) {
      address.setStreet(addressStreet);
    }
    if (addressHouseNumber != null && !addressHouseNumber.isEmpty()) {
      address.setHouseNumber(addressHouseNumber);
    }
    if (addressBuilding != null && !addressBuilding.isEmpty()) {
      address.setBuilding(addressBuilding);
    }

    log.info("Registering user with email: {}", email);

    Optional<User> result = userService.registerUser(
        email, name, password, passwordAgain, address, studyProgram, course
    );

    if (result.isPresent()) {
      model.addAttribute("message", "Регистрация прошла успешно!");
      model.addAttribute("messageType", "success");
      model.addAttribute("registrationSuccess", true);
    } else {
      model.addAttribute("message", "Ошибка при регистрации. Проверьте введенные данные.");
      model.addAttribute("messageType", "error");
      model.addAttribute("registrationSuccess", false);
      // Возвращаем введенные данные обратно на форму
      model.addAttribute("email", email);
      model.addAttribute("name", name);
      model.addAttribute("addressFull", addressFull);
      model.addAttribute("addressCity", addressCity);
      model.addAttribute("addressStreet", addressStreet);
      model.addAttribute("addressHouseNumber", addressHouseNumber);
      model.addAttribute("addressBuilding", addressBuilding);
      model.addAttribute("studyProgram", studyProgram);
      model.addAttribute("course", course);
    }

    return "register";
  }
}