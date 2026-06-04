package com.goskom.site.controlles;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

import com.goskom.site.entities.News;
import com.goskom.site.repositories.NewsRepository;

@Controller
public class NewsController {

    @Autowired
    private NewsRepository newsRepository;

    @GetMapping("/news")
    public String getAllNews(Model model, @RequestHeader(value = "HX-Request", required = false) String hxRequest) {
        List<News> allNews = newsRepository.findAllByOrderByCreatedAtDesc();
        model.addAttribute("newsList", allNews);
        model.addAttribute("page", "news-feed");
        if (hxRequest != null) {
            return "news-feed :: content";
        }
        return "index";
    }

    // 2. Страница одной новости
    @GetMapping("/news/{id}")
    public String getNewsItem(@PathVariable("id") Long id, Model model, @RequestHeader(value = "HX-Request", required = false) String hxRequest) {
        News news = newsRepository.findById(id).orElse(null);
        if (news == null) {
            return "redirect:/news";
        }
        model.addAttribute("newsItem", news);
        model.addAttribute("page", "news-item");
        
        if (hxRequest != null) {
            return "news-item :: content";
        }
        return "index";
    }
}