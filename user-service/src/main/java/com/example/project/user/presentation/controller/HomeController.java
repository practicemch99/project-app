package com.example.project.user.presentation.controller;
import com.example.project.user.client.core.CoreClient;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.client.RestClientException;
@Controller
public class HomeController {
    private final CoreClient core;
    public HomeController(CoreClient core) { this.core = core; }
    @GetMapping("/")
    public String home(Model model) {
        try { model.addAttribute("coreStatus", core.setup()); }
        catch (RestClientException ex) {
            model.addAttribute("coreStatus", "Core 연결 실패: MySQL, migration, core 실행 상태를 확인하세요.");
        }
        return "index";
    }
}
