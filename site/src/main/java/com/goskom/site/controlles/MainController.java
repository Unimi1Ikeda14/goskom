package com.goskom.site.controlles;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Principal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.goskom.site.dto.RegistrationDto;
import com.goskom.site.entities.Bill;
import com.goskom.site.entities.BillType;
import com.goskom.site.entities.News;
import com.goskom.site.entities.Outage;
import com.goskom.site.entities.User;
import com.goskom.site.repositories.BillRepository;
import com.goskom.site.repositories.NewsRepository;
import com.goskom.site.repositories.OutageRepository;
import com.goskom.site.repositories.UserRepositories;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

@Controller
public class MainController {

    private static final Logger logger = LoggerFactory.getLogger(MainController.class);

    @Autowired
    private BillRepository billRepository;

    @Autowired
    private UserRepositories userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private NewsRepository newsRepository;
    @Autowired
    private org.springframework.security.authentication.AuthenticationManager authenticationManager;
    @Autowired
    private OutageRepository outageRepository;
    @org.springframework.beans.factory.annotation.Value("${goskom.upload.dir:uploads}")
    private String uploadDirSetting;
    
    @GetMapping("/")
    public ModelAndView index(Principal principal, Model model) {
        if (principal != null) {
            User currentUser = userRepository.findByEmail(principal.getName());
            model.addAttribute("currentUser", currentUser);
        }
        
        // 1. Вытягиваем новости (как у тебя и было)
        List<News> allNews = newsRepository.findAllByOrderByCreatedAtDesc();
        List<News> latestNews = allNews.stream().limit(3).toList();
        System.out.println("=== ДИАГНОСТИКА: Найдено новостей в базе данных -> " + latestNews.size());
        model.addAttribute("latestNews", latestNews);

        // 2. ДОБАВИЛИ: Вытягиваем ВСЕ актуальные отключения (или limit(5), если их слишком много)
        List<Outage> latestOutages = outageRepository.findAllByOrderByCreatedAtDesc();
        model.addAttribute("latestOutages", latestOutages);

        model.addAttribute("page", "welcome"); 
        return new ModelAndView("index", HttpStatus.OK);
    }

    @GetMapping("/welcome")
    public String welcomeFragment(Model model) {
        // 1. Вытягиваем 3 свежие новости
        List<News> latestNews = newsRepository.findAllByOrderByCreatedAtDesc().stream().limit(3).toList();
        model.addAttribute("latestNews", latestNews);
        
        // 2. ДОБАВИЛИ: Вытягиваем отключения для HTMX-запроса
        List<Outage> latestOutages = outageRepository.findAllByOrderByCreatedAtDesc();
        model.addAttribute("latestOutages", latestOutages);
        
        return "welcome :: content"; // Возвращаем только чистый фрагмент без всей оболочки index.html
    }

    @GetMapping("/register")
    public ModelAndView showRegistrationPage(@RequestHeader(value = "HX-Request", required = false) String hxRequest, Model model) {
        model.addAttribute("userDto", new RegistrationDto());
        if (hxRequest != null) {
            return new ModelAndView("register", HttpStatus.OK);
        } else {
            model.addAttribute("page", "register");
            return new ModelAndView("index", HttpStatus.OK);
        }
    }

    @PostMapping("/register")
    public ModelAndView registerUser(@Valid @ModelAttribute("userDto") RegistrationDto registrationDto, 
                                    BindingResult bindingResult, 
                                    @RequestHeader(value = "HX-Request", required = false) String hxRequest, 
                                    HttpServletResponse response, 
                                    Model model) {
        
        if (bindingResult.hasErrors()) {
            return new ModelAndView("register", HttpStatus.OK);
        }

        if (userRepository.findByEmail(registrationDto.getEmail()) != null) {
            model.addAttribute("error", "Пользователь с таким Email уже существует");
            return new ModelAndView("register", HttpStatus.OK);
        }

        User user = new User();
        user.setName(registrationDto.getName());
        user.setEmail(registrationDto.getEmail());
        user.setPassword(passwordEncoder.encode(registrationDto.getPassword()));
        user.setRole("USER");
        userRepository.save(user);

        model.addAttribute("successMsg", "Регистрация успешна! Войдите под своими данными.");

        if (hxRequest != null) {
            response.setHeader("HX-Push-Url", "/login");
            return new ModelAndView("login", HttpStatus.OK); 
        } else {
            return new ModelAndView("login", HttpStatus.OK);
        }
    }

    @GetMapping("/login")
    public ModelAndView showLoginPage(@RequestHeader(value = "HX-Request", required = false) String hxRequest, 
                                    HttpServletRequest request, 
                                    Model model) {
        
        java.util.Map<String, ?> flashMap = org.springframework.web.servlet.support.RequestContextUtils.getInputFlashMap(request);
        if (flashMap != null) {
            if (flashMap.containsKey("successMsg")) {
                model.addAttribute("successMsg", flashMap.get("successMsg"));
            }
        } else {
            Object sessionSuccess = request.getSession().getAttribute("successMsg");
            if (sessionSuccess != null) {
                model.addAttribute("successMsg", sessionSuccess);
                request.getSession().removeAttribute("successMsg");
            }
        }

        if (hxRequest != null) {
            return new ModelAndView("login", HttpStatus.OK);
        } else {
            model.addAttribute("page", "login");
            return new ModelAndView("index", HttpStatus.OK);
        }
    }

    @PostMapping("/login")
    public ModelAndView processLogin(@RequestParam String email, 
                                     @RequestParam String password, 
                                     HttpServletRequest request,
                                     HttpServletResponse response,
                                     Model model) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, password)
            );

            SecurityContext securityContext = SecurityContextHolder.getContext();
            securityContext.setAuthentication(authentication);

            request.getSession().setAttribute(
                HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, 
                securityContext
            );

            response.setHeader("HX-Redirect", "/");
            return new ModelAndView();

        } catch (org.springframework.security.core.AuthenticationException e) {
            model.addAttribute("error", "Неверный Email или пароль");
            return new ModelAndView("login", HttpStatus.OK);
        }
    }

    @GetMapping("/profile")
    public ModelAndView showProfilePage(@RequestHeader(value = "HX-Request", required = false) String hxRequest, 
                                        Principal principal, 
                                        Model model) {
        if (principal == null) {
            if (hxRequest != null) return new ModelAndView("login", HttpStatus.OK);
            model.addAttribute("page", "login");
            return new ModelAndView("index", HttpStatus.OK);
        }

        User user = userRepository.findByEmail(principal.getName());
        
        // Вызываем наш хелпер
        populateProfileModel(user, model);

        if (hxRequest != null) {
            return new ModelAndView("profile", HttpStatus.OK);
        } else {
            model.addAttribute("page", "profile");
            return new ModelAndView("index", HttpStatus.OK);
        }
    }

    @PostMapping("/profile/avatar")
    public ModelAndView uploadAvatar(@RequestParam("avatar") org.springframework.web.multipart.MultipartFile file, 
                                     Principal principal, 
                                     Model model) {
        if (principal == null) return new ModelAndView("login", HttpStatus.OK);

        User user = userRepository.findByEmail(principal.getName());

        if (!file.isEmpty()) {
            try {
                Path uploadPath = Paths.get(uploadDirSetting).toAbsolutePath().normalize();
                if (!Files.exists(uploadPath)) {
                    Files.createDirectories(uploadPath);
                }
                String filename = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
                Path filePath = uploadPath.resolve(filename);
                Files.copy(file.getInputStream(), filePath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                user.setAvatarPath("/uploads/" + filename);
                userRepository.save(user);
                
                model.addAttribute("successMessage", "Аватар успешно обновлен!");

            } catch (IOException e) {
                logger.error("Ошибка при сохранении файла аватара", e);
                model.addAttribute("error", "Не удалось сохранить файл изображения.");
            }
        } else {
            model.addAttribute("error", "Выбранный файл пуст.");
        }

        // Пересчет топ-3 для профиля при возврате страницы
        List<Bill> recentBills = new ArrayList<>();
        if (user.getBills() != null) {
            user.getBills().size();
            recentBills = user.getBills().stream()
                .sorted(Comparator
                    .comparing(Bill::isPaid)
                    .thenComparing((Bill b) -> b.getCreatedAt() != null ? b.getCreatedAt() : LocalDateTime.MIN, Comparator.reverseOrder()))
                .limit(3)
                .collect(Collectors.toList());
        }
        model.addAttribute("user", user);
        model.addAttribute("currentUser", user);
        model.addAttribute("recentBills", recentBills);
        
        return new ModelAndView("profile", HttpStatus.OK);
    }

    @GetMapping("/logout")
    public String logout() {
        SecurityContextHolder.clearContext();
        return "redirect:/"; 
    }
    @PostMapping("/profile/meter/cold-water")
    public ModelAndView updateColdWater(@RequestParam Double value, Principal principal, Model model) {
        if (principal == null) return new ModelAndView("login", HttpStatus.OK);

        User user = userRepository.findByEmail(principal.getName());
        Double currentVal = (user.getColdWater() != null) ? user.getColdWater() : 0.0;
        
        if (value == null || value < 0 || value < currentVal) {
            if (user.getBills() != null) user.getBills().size(); 
            model.addAttribute("error", "Ошибка: Новые показания холодной воды не могут быть меньше текущих!");
        } else {
            user.setColdWater(value);
            userRepository.save(user);
            model.addAttribute("successMessage", "Показания холодной воды успешно обновлены!");
        }

        List<Bill> recentBills = user.getBills() != null ? user.getBills().stream()
            .sorted(Comparator.comparing(Bill::isPaid)
                .thenComparing((Bill b) -> b.getCreatedAt() != null ? b.getCreatedAt() : LocalDateTime.MIN, Comparator.reverseOrder()))
            .limit(3).collect(Collectors.toList()) : new ArrayList<>();

        model.addAttribute("user", user);
        model.addAttribute("currentUser", user);
        model.addAttribute("recentBills", recentBills);
        return new ModelAndView("profile", HttpStatus.OK);
    }

    @PostMapping("/profile/meter/hot-water")
    public ModelAndView updateHotWater(@RequestParam Double value, Principal principal, Model model) {
        if (principal == null) return new ModelAndView("login", HttpStatus.OK);

        User user = userRepository.findByEmail(principal.getName());
        Double currentVal = (user.getHotWater() != null) ? user.getHotWater() : 0.0;
        
        if (value == null || value < 0 || value < currentVal) {
            if (user.getBills() != null) user.getBills().size();
            model.addAttribute("error", "Ошибка: Новые показания горячей воды не могут быть меньше текущих!");
        } else {
            user.setHotWater(value);
            userRepository.save(user);
            model.addAttribute("successMessage", "Показания горячей воды успешно обновлены!");
        }

        List<Bill> recentBills = user.getBills() != null ? user.getBills().stream()
            .sorted(Comparator.comparing(Bill::isPaid)
                .thenComparing((Bill b) -> b.getCreatedAt() != null ? b.getCreatedAt() : LocalDateTime.MIN, Comparator.reverseOrder()))
            .limit(3).collect(Collectors.toList()) : new ArrayList<>();

        model.addAttribute("user", user);
        model.addAttribute("currentUser", user);
        model.addAttribute("recentBills", recentBills);
        return new ModelAndView("profile", HttpStatus.OK);
    }

    @PostMapping("/profile/meter/electricity")
    public ModelAndView updateElectricity(@RequestParam Integer value, Principal principal, Model model) {
        if (principal == null) return new ModelAndView("login", HttpStatus.OK);

        User user = userRepository.findByEmail(principal.getName());
        Integer currentVal = (user.getElectricity() != null) ? user.getElectricity() : 0;
        
        if (value == null || value < 0 || value < currentVal) {
            if (user.getBills() != null) user.getBills().size();
            model.addAttribute("error", "Ошибка: Показания электроэнергии не могут быть меньше текущих!");
        } else {
            user.setElectricity(value);
            userRepository.save(user);
            model.addAttribute("successMessage", "Показания электроэнергии успешно обновлены!");
        }

        List<Bill> recentBills = user.getBills() != null ? user.getBills().stream()
            .sorted(Comparator.comparing(Bill::isPaid)
                .thenComparing((Bill b) -> b.getCreatedAt() != null ? b.getCreatedAt() : LocalDateTime.MIN, Comparator.reverseOrder()))
            .limit(3).collect(Collectors.toList()) : new ArrayList<>();

        model.addAttribute("user", user);
        model.addAttribute("currentUser", user);
        model.addAttribute("recentBills", recentBills);
        return new ModelAndView("profile", HttpStatus.OK);
    }

    @PostMapping("/profile/pay-bill/{id}")
    public ModelAndView payBill(@PathVariable Long id, 
                                @RequestHeader(value = "HX-Request", required = false) String hxRequest, 
                                Principal principal, 
                                Model model) {
        if (principal == null) {
            return new ModelAndView("login", HttpStatus.OK);
        }

        User user = userRepository.findByEmail(principal.getName());
        if (user == null) {
            return new ModelAndView("login", HttpStatus.OK);
        }

        Bill bill = billRepository.findById(id).orElse(null);
        if (bill != null && bill.getUser().getId().equals(user.getId())) {
            bill.setPaid(true);
            bill.setPaidAt(LocalDateTime.now()); // Сохраняем точную дату оплаты счета
            billRepository.save(bill);
            model.addAttribute("successMessage", "Счёт успешно оплачен!");
        } else {
            model.addAttribute("error", "Ошибка: Счёт не найден или принадлежит не вам.");
        }

        List<Bill> recentBills = new ArrayList<>();
        if (user.getBills() != null) {
            user.getBills().size();
            recentBills = user.getBills().stream()
                .sorted(Comparator
                    .comparing(Bill::isPaid)
                    .thenComparing((Bill b) -> b.getCreatedAt() != null ? b.getCreatedAt() : LocalDateTime.MIN, Comparator.reverseOrder()))
                .limit(3)
                .collect(Collectors.toList());
        }

        model.addAttribute("user", user);
        model.addAttribute("currentUser", user);
        model.addAttribute("recentBills", recentBills);

        if (hxRequest != null) {
            return new ModelAndView("profile", HttpStatus.OK);
        } else {
            model.addAttribute("page", "profile");
            return new ModelAndView("index", HttpStatus.OK);
        }
    }

    @GetMapping("/billing")
    public ModelAndView showBillingPage(@RequestParam(value = "status", defaultValue = "all") String status,
                                        @RequestParam(value = "sort", defaultValue = "newest") String sort,
                                        @RequestParam(value = "typeFilter", defaultValue = "all") String typeFilter,
                                        @RequestHeader(value = "HX-Request", required = false) String hxRequest,
                                        HttpServletRequest request,
                                        Principal principal,
                                        Model model) {
        if (principal == null) {
            if (hxRequest != null) return new ModelAndView("login", HttpStatus.OK);
            model.addAttribute("page", "login");
            return new ModelAndView("index", HttpStatus.OK);
        }

        User user = userRepository.findByEmail(principal.getName());
        
        model.addAttribute("currentUser", user);
        model.addAttribute("user", user);
        model.addAttribute("currentStatus", status);
        model.addAttribute("currentSort", sort);
        model.addAttribute("currentTypeFilter", typeFilter);
        model.addAttribute("billTypes", BillType.values()); // Передаем список типов услуг

        List<Bill> filteredBills = new ArrayList<>();
        if (user.getBills() != null) {
            Stream<Bill> billStream = user.getBills().stream();

            // 1. Фильтр по типу услуги
            if (!"all".equals(typeFilter)) {
                billStream = billStream.filter(b -> b.getType() != null && b.getType().name().equals(typeFilter));
            }

            // 2. Фильтр по статусу
            if ("paid".equals(status)) {
                billStream = billStream.filter(Bill::isPaid);
            } else if ("unpaid".equals(status)) {
                billStream = billStream.filter(b -> !b.isPaid());
            }

            // 3. Сортировка по датам и ценам
            Comparator<Bill> comparator;
            switch (sort) {
                case "oldest":
                    comparator = Comparator.comparing((Bill b) -> b.getCreatedAt() != null ? b.getCreatedAt() : LocalDateTime.MIN);
                    break;
                case "price-desc":
                    comparator = Comparator.comparing(Bill::getAmount).reversed();
                    break;
                case "price-asc":
                    comparator = Comparator.comparing(Bill::getAmount);
                    break;
                case "newest":
                default:
                    comparator = Comparator.comparing((Bill b) -> b.getCreatedAt() != null ? b.getCreatedAt() : LocalDateTime.MIN).reversed();
                    break;
            }
            
            filteredBills = billStream.sorted(comparator).collect(Collectors.toList());
        }
        model.addAttribute("bills", filteredBills);
        model.addAttribute("dateFormatter", DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"));

        if (hxRequest != null) {
            if (request.getHeader("HX-Target") != null && request.getHeader("HX-Target").equals("billing-container")) {
                return new ModelAndView("billing :: bill-list-fragment", HttpStatus.OK);
            }
            return new ModelAndView("billing", HttpStatus.OK);
        } else {
            model.addAttribute("page", "billing");
            return new ModelAndView("index", HttpStatus.OK);
        }
    }

    @PostMapping("/pay-bill/{id}")
    public ModelAndView payBill(@PathVariable("id") Long id, 
                                Principal principal, 
                                Model model) {
        if (principal == null) {
            return new ModelAndView("login", HttpStatus.OK);
        }

        // 1. Находим и оплачиваем счет (примерная логика)
        Bill bill = billRepository.findById(id).orElseThrow();
        bill.setPaid(true);
        billRepository.save(bill);

        // 2. Получаем актуального юзера и обновляем модель одной строчкой!
        User user = userRepository.findByEmail(principal.getName());
        populateProfileModel(user, model);

        // 3. Возвращаем только фрагмент профиля, так как кнопка ожидает замену всей карточки
        model.addAttribute("successMessage", "Счет успешно оплачен!");
        return new ModelAndView("profile", HttpStatus.OK);
    }
    @GetMapping("/billing/{id}")
    public ModelAndView getBillingDetails(@PathVariable("id") Long id, 
                                        @RequestHeader(value = "HX-Request", required = false) String hxRequest,
                                        Principal principal, 
                                        Model model) {
        // 1. Ищем счёт в базе
        Bill bill = billRepository.findById(id).orElse(null);
        
        if (bill == null) {
            model.addAttribute("errorMessage", "Счёт не найден");
            return new ModelAndView("error", HttpStatus.NOT_FOUND);
        }

        // 2. Безопасность: проверяем, что счёт принадлежит именно текущему авторизованному пользователю
        if (principal == null || !bill.getUser().getEmail().equals(principal.getName())) {
            model.addAttribute("errorMessage", "Доступ запрещён");
            return new ModelAndView("error", HttpStatus.FORBIDDEN);
        }

        // 3. Закидываем данные в модель
        model.addAttribute("bill", bill);
        
        // Передаем текущего юзера (для вывода показаний счетчиков, если нужно)
        model.addAttribute("user", bill.getUser()); 

        // 4. Если запрос от HTMX — отдаем только кусочек, если обычный — всю страницу внутри index.html
        if (hxRequest != null) {
            return new ModelAndView("billing-details", HttpStatus.OK);
        } else {
            model.addAttribute("page", "billing-details");
            return new ModelAndView("index", HttpStatus.OK);
        }
    }
    private void populateProfileModel(User user, Model model) {
        if (user.getBills() != null) {
            user.getBills().size(); // Твоя инициализация Lazy-коллекции
        }

        // Твоя логика виджета: Сначала неоплаченные, затем оплаченные. От новых к старым. Лимит 3.
        List<Bill> recentBills = new ArrayList<>();
        if (user.getBills() != null) {
            recentBills = user.getBills().stream()
                .sorted(Comparator
                    .comparing(Bill::isPaid)
                    .thenComparing((Bill b) -> b.getCreatedAt() != null ? b.getCreatedAt() : LocalDateTime.MIN, Comparator.reverseOrder()))
                .limit(3)
                .collect(Collectors.toList());
        }

        model.addAttribute("user", user);
        model.addAttribute("currentUser", user);
        model.addAttribute("recentBills", recentBills);
    }
}