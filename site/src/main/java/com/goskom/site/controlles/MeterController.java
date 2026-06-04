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
import org.springframework.web.servlet.ModelAndView;

import com.goskom.site.entities.BillType;
import com.goskom.site.entities.MeterReading;
import com.goskom.site.entities.User;
import com.goskom.site.repositories.MeterReadingRepository;
import com.goskom.site.repositories.UserRepositories;

@Controller
@RequestMapping("/meters")
public class MeterController {

    @Autowired
    private UserRepositories userRepository;

    @Autowired
    private MeterReadingRepository readingRepository;

    @GetMapping
    public ModelAndView getMetersPage(Principal principal, Model model) {
        if (principal == null) return new ModelAndView("redirect:/login");
        
        User user = userRepository.findByEmail(principal.getName());
        model.addAttribute("user", user);
        return new ModelAndView("meters");
    }
    @GetMapping("/history")
    public String getMetersHistory(@RequestParam(value = "historyType", defaultValue = "all") String historyType, 
                                Principal principal, Model model) {
        if (principal == null) return "redirect:/login";
        
        User user = userRepository.findByEmail(principal.getName());
        List<MeterReading> readings;
        
        if ("all".equals(historyType)) {
            readings = readingRepository.findByUserOrderByCreatedAtDesc(user);
        } else {
            try {
                BillType type = BillType.valueOf(historyType);
                readings = readingRepository.findByUserAndTypeOrderByCreatedAtDesc(user, type);
            } catch (IllegalArgumentException e) {
                readings = readingRepository.findByUserOrderByCreatedAtDesc(user);
            }
        }
        
        model.addAttribute("readings", readings);
        model.addAttribute("selectedType", historyType);
        
        return "meters-history"; // Просто возвращаем имя HTML-шаблона
    }
    @PostMapping("/submit")
    public ModelAndView submitMeter(@RequestParam("type") String typeStr, 
                                    @RequestParam("value") String valueStr, 
                                    Principal principal, Model model) {
        if (principal == null) return new ModelAndView("redirect:/login");
        
        User user = userRepository.findByEmail(principal.getName());
        
        try {
            Double value = Double.parseDouble(valueStr.replace(",", "."));
            BillType type = BillType.valueOf(typeStr);
            Double currentVal = 0.0;
            if (type == BillType.COLD_WATER) currentVal = user.getColdWater() != null ? user.getColdWater() : 0.0;
            else if (type == BillType.HOT_WATER) currentVal = user.getHotWater() != null ? user.getHotWater() : 0.0;
            else if (type == BillType.ELECTRICITY) currentVal = user.getElectricity() != null ? user.getElectricity().doubleValue() : 0.0;
            else if (type == BillType.GAS) currentVal = user.getGas() != null ? user.getGas() : 0.0; // Логика для Газа
            else if (type == BillType.HEATING) currentVal = user.getHeating() != null ? user.getHeating() : 0.0; // Логика для Отопления

            if (value < currentVal) {
                model.addAttribute("error", "Новые показания не могут быть меньше предыдущих (" + currentVal + ")!");
                model.addAttribute("user", user);
                return new ModelAndView("meters");
            }
            if (type == BillType.COLD_WATER) user.setColdWater(value);
            else if (type == BillType.HOT_WATER) user.setHotWater(value);
            else if (type == BillType.ELECTRICITY) user.setElectricity(value.intValue());
            else if (type == BillType.GAS) user.setGas(value); // Сохраняем Газ
            else if (type == BillType.HEATING) user.setHeating(value); // Сохраняем Отопление
            Double consumption = value - currentVal;
            userRepository.save(user);
            MeterReading reading = new MeterReading();
            reading.setUser(user);
            reading.setType(type);
            reading.setValue(value);
            reading.setConsumption(consumption);
            readingRepository.save(reading);

            model.addAttribute("successMessage", "Показания успешно сохранены!");
            
        } catch (NumberFormatException e) {
            model.addAttribute("error", "Некорректный формат числового значения!");
        } catch (Exception e) {
            model.addAttribute("error", "Произошла ошибка при сохранении данных.");
            e.printStackTrace();
        }

        model.addAttribute("user", user);
        return new ModelAndView("meters");
    }
}