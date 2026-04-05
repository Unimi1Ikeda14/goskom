package com.goskom.site.controlles;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.goskom.site.entities.User;
import com.goskom.site.repositories.UserRepositories;
import com.goskom.site.services.UserService;
@Controller
public class MainControlles
{
    @Autowired
    private UserRepositories userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private final UserService userService;

    public MainControlles(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/")
    public String addUser()
    {
        User user = new User();
        user.setName("Saad");
        user.setEmail("test2@test.com");
        String rawPassword = "mySuperSecretPassword123";
        String encodedPassword = passwordEncoder.encode(rawPassword);
        user.setPassword(encodedPassword);
        userRepository.save(user);
        return "Пользователь сохранен!";
    }

    @GetMapping("/all")
    public List<User> all()
    {
        return userRepository.findAll();
    }

    @GetMapping("/login")
    public String showLoginPage()
    {
        return "login";
    }
    @PostMapping("/login")
public String login(@RequestParam String email, 
                    @RequestParam String password, 
                    Model model) {
    
    // Вызываем твой метод из UserService
    boolean isAuthenticated = userService.authenticate(email, password);

    if (isAuthenticated) {
        // Если всё верно — отправляем на главную или в профиль
        return "redirect:/profile"; 
    } else {
        // Если ошибка — возвращаем на страницу входа и передаем сигнал об ошибке
        model.addAttribute("error", true);
        return "login"; 
    }
}
}