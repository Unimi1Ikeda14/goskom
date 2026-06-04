package com.goskom.site.controlles;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.goskom.site.entities.SupportTicket;
import com.goskom.site.entities.User;
import com.goskom.site.repositories.SupportTicketRepository;
import com.goskom.site.repositories.UserRepositories;

@Controller
@RequestMapping("/support")
public class SupportController {

    @Autowired
    private SupportTicketRepository ticketRepository;

    @Autowired
    private UserRepositories userRepository;
    @GetMapping
    public ModelAndView supportCenter(@RequestHeader(value = "HX-Request", required = false) String hxRequest,
                                      Principal principal, Model model) {
        if (principal == null) {
            return new ModelAndView("redirect:/login");
        }

        User currentUser = userRepository.findByEmail(principal.getName());
        List<SupportTicket> userTickets = ticketRepository.findByUserOrderByCreatedAtDesc(currentUser);
        
        model.addAttribute("tickets", userTickets);
        model.addAttribute("currentUser", currentUser);

        if (hxRequest != null) {
            return new ModelAndView("support :: content", HttpStatus.OK);
        } else {
            model.addAttribute("page", "support");
            return new ModelAndView("index", HttpStatus.OK);
        }
    }

    // 12. Форма создания обращения (интерфейс)
    @GetMapping("/create")
    public ModelAndView showCreateForm(@RequestHeader(value = "HX-Request", required = false) String hxRequest,
                                   Principal principal, Model model) {
        if (hxRequest != null) {
            return new ModelAndView("support-create :: content", HttpStatus.OK);
        } else {
            model.addAttribute("page", "support-create");
            return new ModelAndView("index", HttpStatus.OK);
        }
    }

    // Обработка отправки формы создания заявки
    @PostMapping("/create")
    public String createTicket(@RequestParam("title") String title,
                            @RequestParam("description") String description,
                            @RequestParam("category") String category,
                            @RequestParam("address") String address,
                            Principal principal) {
        if (principal != null) {
            User currentUser = userRepository.findByEmail(principal.getName());
            
            SupportTicket ticket = new SupportTicket();
            ticket.setTitle(title.trim());
            ticket.setDescription(description.trim());
            ticket.setCategory(category);
            ticket.setUser(currentUser);
            
            String userAddress = (currentUser.getAddress() != null) ? currentUser.getAddress() : "Адрес не указан";
            ticket.setAddress(userAddress);

            ticketRepository.save(ticket);
        }
        return "redirect:/support";
    }
    @GetMapping("/support")
    public ModelAndView showSupportCenter(@RequestHeader(value = "HX-Request", required = false) String hxRequest,
                                        Principal principal, 
                                        Model model) {
        // 1. Проверяем, авторизован ли пользователь
        if (principal == null) {
            if (hxRequest != null) return new ModelAndView("login", HttpStatus.OK);
            model.addAttribute("page", "login");
            return new ModelAndView("index", HttpStatus.OK);
        }

        try {
            // 2. Берем текущего пользователя
            User currentUser = userRepository.findByEmail(principal.getName());
            
            // 3. Вытаскиваем его заявки
            List<SupportTicket> tickets = ticketRepository.findByUserOrderByCreatedAtDesc(currentUser);
            
            // Проверка на null (на случай, если репозиторий почему-то вернул null вместо пустого списка)
            if (tickets == null) {
                tickets = new ArrayList<>();
            }
            
            // 4. Запихиваем в модель именно те имена, которые объявлены в Thymeleaf
            model.addAttribute("tickets", tickets);
            model.addAttribute("currentUser", currentUser);

        } catch (Exception e) {
            // Если база данных ругнётся на поле или метод репозитория — мы увидим это прямо в консоли Idea
            System.err.println("=== ОШИБКА ПРИ ПОЛУЧЕНИИ ЗАЯВОК ЖИЛЬЦА ===");
            e.printStackTrace();
            
            // Чтобы страница не отдавала 500, подсовываем пустой список в случае аварии
            model.addAttribute("tickets", new ArrayList<>());
        }

        // 5. Возвращаем фрагмент для HTMX или целую страницу для обычной перезагрузки
        if (hxRequest != null) {
            return new ModelAndView("support", HttpStatus.OK);
        } else {
            model.addAttribute("page", "support");
            return new ModelAndView("index", HttpStatus.OK);
        }
    }
}