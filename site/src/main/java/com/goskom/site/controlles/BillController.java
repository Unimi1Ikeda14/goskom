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
import com.goskom.site.entities.BillType; // Импортируем наш Enum
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

    @GetMapping
    public String getUserBills(Model model, Principal principal) {
        if (principal == null) {
            return "redirect:/login";
        }
        User user = userRepository.findByEmail(principal.getName());
        if (user == null) {
            return "redirect:/login"; 
        }
        List<Bill> bills = billRepository.findByUserId(user.getId());
        
        model.addAttribute("bills", bills);
        model.addAttribute("userName", user.getName());
        return "billing"; 
    }

    @PostMapping("/add")
    public String addBill(@RequestParam String type, // Изменили title на type
                          @RequestParam Double amount, 
                          Principal principal) {
        
        if (principal == null) {
            return "redirect:/login";
        }

        User user = userRepository.findByEmail(principal.getName());
        if (user == null || type == null || amount == null || amount <= 0) {
            return "redirect:/api/bills?error";
        }
        
        try {
            // Превращаем пришедшую строку (например, "GAS") в элемент Enum
            BillType billType = BillType.valueOf(type);
            
            // Используем наш новый конструктор (он сам ставит paid=false и текущую дату createdAt)
            Bill bill = new Bill(billType, amount, user);
            
            billRepository.save(bill); 
        } catch (IllegalArgumentException e) {
            // Если вдруг прилетит неверный тип услуги
            return "redirect:/api/bills?invalidType";
        }

        return "redirect:/api/bills";
    }
}