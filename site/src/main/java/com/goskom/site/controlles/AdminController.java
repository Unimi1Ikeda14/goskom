package com.goskom.site.controlles;

import java.security.Principal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.goskom.site.entities.Bill;
import com.goskom.site.entities.BillType;
import com.goskom.site.entities.MeterReading;
import com.goskom.site.entities.News;
import com.goskom.site.entities.User;
import com.goskom.site.repositories.BillRepository;
import com.goskom.site.repositories.MeterReadingRepository;
import com.goskom.site.repositories.NewsRepository;
import com.goskom.site.repositories.OutageRepository;
import com.goskom.site.repositories.UserRepositories;

@Controller
@RequestMapping("/admin") // Базовый путь для всех методов админки
public class AdminController {

    @Autowired
    private UserRepositories userRepository;
    @Autowired
    private MeterReadingRepository readingRepository;
    @Autowired
    private BillRepository billRepository;
    @Autowired
    private NewsRepository newsRepository;
    @Autowired
    private OutageRepository outageRepository;
    private User getAuthenticatedUser(Principal principal) {
        if (principal != null) {
            return userRepository.findByEmail(principal.getName());
        }
        return null;
    }

    @GetMapping
    public ModelAndView showAdminPanel(@RequestHeader(value = "HX-Request", required = false) String hxRequest, 
                                       Principal principal, 
                                       Model model) {
        
        model.addAttribute("currentUser", getAuthenticatedUser(principal));
        
        List<User> residents = userRepository.findAll();
        model.addAttribute("residents", residents);

        if (hxRequest != null) {
            return new ModelAndView("admin", HttpStatus.OK);
        } else {
            model.addAttribute("page", "admin");
            return new ModelAndView("index", HttpStatus.OK);
        }
    }

    // Исправили путь: теперь полный URL будет /admin/invoice, как и прописано в hx-post формы
    @PostMapping("/invoice")
    public ModelAndView createInvoice(@RequestParam(name = "residentId") Long residentId,
                                      @RequestParam(name = "type") String type, // Принимаем строку type из селекта формы
                                      @RequestParam(name = "amount") Double amount,
                                      Principal principal,
                                      Model model) {
        try {
            User resident = userRepository.findById(residentId).orElse(null);
            
            if (resident != null && amount != null && amount > 0) {
                // Конвертируем строку из формы в твой Enum BillType
                BillType billType = BillType.valueOf(type);
                
                // Используем удобный конструктор, который ты прописал в Bill.java
                Bill bill = new Bill(billType, amount, resident);
                
                billRepository.save(bill);
                model.addAttribute("successMessage", "Счёт успешно выставлен!");
            } else {
                model.addAttribute("errorMessage", "Ошибка: неверные данные или пользователь не найден.");
            }
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", "Ошибка: указан неверный тип услуги.");
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Внутренняя ошибка сервера: " + e.getMessage());
        }

        // Подгружаем актуальный список пользователей для HTMX-обновления шаблона
        model.addAttribute("residents", userRepository.findAll());
        model.addAttribute("currentUser", getAuthenticatedUser(principal));
        return new ModelAndView("admin", HttpStatus.OK);
    }

    // Путь для удаления жильца: /admin/users/delete/{id}
    @DeleteMapping("/users/delete/{id}")
    public ResponseEntity<Void> deleteResident(@PathVariable("id") Long id, Principal principal) {
        User currentUser = getAuthenticatedUser(principal);
        
        // Защита от удаления самого себя по прямой ссылке
        if (currentUser != null && currentUser.getId().equals(id)) {
            return ResponseEntity.badRequest().build();
        }

        if (userRepository.existsById(id)) {
            userRepository.deleteById(id);
            // Возвращаем статус 200 OK без контента. HTMX сотрет строку <tr> из таблицы
            return ResponseEntity.ok().build();
        }

        return ResponseEntity.notFound().build();
    }
   @GetMapping("/calculate-amount")
    @ResponseBody // Возвращаем чистый тег input прямо в браузер без поиска html-файлов
    public String calculateBillAmount(@RequestParam(value = "residentId", required = false) Long residentId,
                                      @RequestParam(value = "type", required = false) String typeStr) {
        double amount = 0.0;
        double consumption = 0.0;
        double tariff = 0.0;

        // 1. Извлекаем выбранный тип услуги и его тариф из Enum
        BillType type = null;
        if (typeStr != null && !typeStr.isEmpty()) {
            try {
                type = BillType.valueOf(typeStr.trim());
                tariff = type.getTariff();
            } catch (IllegalArgumentException e) {
                // Игнорируем некорректные значения
            }
        }

        // 2. Считаем сумму, только если выбраны и жилец, и тип услуги
        if (residentId != null && type != null) {
            User user = userRepository.findById(residentId).orElse(null);
            if (user != null) {
                
                // Ищем последнюю запись расхода в таблице истории счетчиков
                MeterReading lastReading = readingRepository.findFirstByUserAndTypeOrderByCreatedAtDesc(user, type);
                
                if (lastReading != null && lastReading.getConsumption() != null) {
                    consumption = lastReading.getConsumption();
                } else {
                    // Если истории еще нет, берем текущие показания из карточки жильца (защита от null)
                    Double currentVal = 0.0;
                    switch (type) {
                        case COLD_WATER: 
                            currentVal = user.getColdWater(); 
                            break;
                        case HOT_WATER: 
                            currentVal = user.getHotWater(); 
                            break;
                        case ELECTRICITY: 
                            currentVal = (user.getElectricity() != null) ? Double.valueOf(user.getElectricity().toString()) : 0.0; 
                            break;
                        case GAS: 
                            currentVal = user.getGas(); 
                            break;
                        case HEATING: 
                            currentVal = user.getHeating(); 
                            break;
                        default:
                            currentVal = 0.0;
                    }
                    consumption = (currentVal != null) ? currentVal : 0.0;
                }
                
                // Формула: Расход * Тариф
                amount = consumption * tariff;
            }
        }

        // Округляем до копеек (2 знака после запятой)
        amount = Math.round(amount * 100.0) / 100.0;

        // 3. Возвращаем точно такое же поле, как в твоем HTML шаблоне, но с заполненным value.
        // Если сумма 0 (ничего еще не выбрано нормально), оставляем пустой placeholder.
        String valueAttr = (amount > 0) ? " value=\"" + amount + "\"" : "";
        
        return "<input type=\"number\" step=\"0.01\" min=\"0.01\" name=\"amount\" id=\"bill-amount-field\" " +
               "class=\"input-field\" placeholder=\"0.00\" required" + valueAttr + ">";
    }
    @PostMapping("/news/create")
    public String createNews(@RequestParam("title") String title,
                            @RequestParam("shortDescription") String shortDescription,
                            @RequestParam(value = "imageUrl", required = false) String imageUrl,
                            @RequestParam("fullText") String fullText,
                            Principal principal,
                            Model model) {
        try {
            News news = new News();
            news.setTitle(title.trim());
            news.setShortDescription(shortDescription.trim());
            news.setFullText(fullText.trim());
            
            if (imageUrl != null && !imageUrl.trim().isEmpty()) {
                news.setImageUrl(imageUrl.trim());
            }

            newsRepository.save(news);
            
            // Перенаправляем на GET-метод /admin, который у тебя железно работает и собирает всю админку без ошибок
            return "redirect:/admin"; 
            
        } catch (Exception e) {
            // Если упало само сохранение в БД
            System.out.println("КРИТИЧЕСКАЯ ОШИБКА БАЗЫ ДАННЫХ: " + e.getMessage());
            e.printStackTrace();
            
            model.addAttribute("errorMessage", "❌ Не удалось опубликовать новость: " + e.getMessage());
            model.addAttribute("residents", userRepository.findAll());
            model.addAttribute("currentUser", getAuthenticatedUser(principal));
            return "admin :: content"; // или твое имя файла админки
        }
    }
    @PostMapping("/outages/create")
    public String createOutage(@RequestParam("title") String title,
                            @RequestParam("description") String description,
                            @RequestParam("type") String typeStr,
                            @RequestParam("startDate") String startDateStr,
                            @RequestParam("endDate") String endDateStr,
                            @RequestParam(value = "isEmergency",defaultValue = "false") boolean isEmergency,
                            Principal principal,
                            Model model) {
        try {
            com.goskom.site.entities.Outage outage = new com.goskom.site.entities.Outage();
            outage.setTitle(title.trim());
            outage.setDescription(description.trim());
            outage.setType(com.goskom.site.entities.BillType.valueOf(typeStr));
            
            // Парсим даты из HTML формы (input type="datetime-local" присылает данные в ISO формате)
            outage.setStartDate(java.time.LocalDateTime.parse(startDateStr));
            outage.setEndDate(java.time.LocalDateTime.parse(endDateStr));
            outage.setEmergency(isEmergency);

            outageRepository.save(outage);
            
        } catch (Exception e) {
            System.out.println("Ошибка при создании отключения: " + e.getMessage());
        }

        // Возвращаем админа на рабочую панель
        return "redirect:/admin";
    }
}