package com.goskom.site.controlles;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller; // Импорт вашего DTO
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.goskom.site.dto.RegistrationDto;
import com.goskom.site.entities.User;
import com.goskom.site.repositories.UserRepositories;
import com.goskom.site.services.UserService;

@Controller
public class MainControlles {

    @Autowired
    private UserRepositories userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private UserService userService;

    @GetMapping("/register")
    public String showRegistrationPage(Model model) {

        model.addAttribute("userDto", new RegistrationDto());
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(@ModelAttribute("userDto") RegistrationDto registrationDto, Model model) {
        
 
        if (registrationDto.getEmail() == null || registrationDto.getEmail().isEmpty()) {
            model.addAttribute("error", "Email не может быть пустым");
            return "register";
        }


        if (userRepository.findByEmail(registrationDto.getEmail()) != null) {
            model.addAttribute("error", "Пользователь с таким Email уже существует");
            return "register";
        }

  
        User user = new User();
        user.setName(registrationDto.getName());
        user.setEmail(registrationDto.getEmail());
        
   
        String encodedPassword = passwordEncoder.encode(registrationDto.getPassword());
        user.setPassword(encodedPassword);
        
    
        userRepository.save(user);

        return "redirect:/login";
    }
    @GetMapping("/login")
    public String showLoginPage()
    {
        return "login";
    }
    @PostMapping("/login")
    public String login(@RequestParam String email, @RequestParam String password, Model model) {
        if (userService.authenticate(email, password)) {
            // Создаем объект аутентификации
            Authentication auth = new UsernamePasswordAuthenticationToken(email, null, new java.util.ArrayList<>());
            
            // Сохраняем его в контекст безопасности (создаем сессию)
            SecurityContextHolder.getContext().setAuthentication(auth);
            
            return "redirect:/profile"; 
        } else {
            model.addAttribute("error", true);
            return "login"; 
        }
    }
@GetMapping("/profile")
public String showProfilePage(java.security.Principal principal, Model model) {
    // Если пользователь не авторизован (например, зашел по прямой ссылке)
    if (principal == null) {
        return "redirect:/login";
    }

    // Ищем пользователя в базе по email из Principal
    User user = userRepository.findByEmail(principal.getName());
    
    // Передаем объект пользователя в HTML-шаблон
    model.addAttribute("user", user);
    
    return "profile"; // Должен существовать файл profile.html
}

}