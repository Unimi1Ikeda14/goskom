package com.goskom.site.controlles;

import java.security.Principal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.goskom.site.entities.Bill;
import com.goskom.site.entities.User;
import com.goskom.site.repositories.BillRepository;
import com.goskom.site.repositories.UserRepositories;

@Controller
@RequestMapping("/api/bills")
public class BillController {

    @Autowired
    private BillRepository billRepository;

    @Autowired
    private UserRepositories userRepository;

    // 1. Чтение (Read): Получение всех счетов текущего пользователя
    @GetMapping
    public String getUserBills(Model model, Principal principal) {
        if (principal == null) {
            return "redirect:/login"; // Редирект, если не авторизован 
        }
        
        User user = userRepository.findByEmail(principal.getName());
        List<Bill> bills = billRepository.findByUserId(user.getId());
        
        model.addAttribute("bills", bills);
        model.addAttribute("userName", user.getName());
        return "billing"; // Имя HTML-шаблона
    }

    // 2. Создание (Create): Добавление нового счета (ЗАЩИЩЕННЫЙ МЕТОД)
    @PostMapping("/add")
    public String addBill(@RequestParam String title, 
                          @RequestParam Double amount, 
                          Principal principal) {
        // Проверка авторизации согласно требованию 3.2 
        if (principal == null) {
            return "redirect:/login";
        }

        User user = userRepository.findByEmail(principal.getName());
        
        Bill bill = new Bill();
        bill.setTitle(title);
        bill.setAmount(amount);
        bill.setPaid(false);
        bill.setUser(user);
        
        billRepository.save(bill); // Сохранение в SQLite
        return "redirect:/api/bills";
    }
}