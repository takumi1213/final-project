package sample.thymeleaf.web;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import sample.common.dao.entity.Login;
import sample.common.service.LoginService;

@Controller
public class LoginController {
    @Autowired
    private LoginService loginService;

    @GetMapping("/register")
    public String registerInit(Model model) {
        model.addAttribute("login", new Login());
        return "register";
    }

    @PostMapping("/register")
    public String register(@Validated @ModelAttribute("login") Login login, BindingResult result) {
        // 1. 入力形式チェック
        if (result.hasErrors()) return "register";

        // 2. ユーザー名重複チェック
        if (loginService.existsUsername(login.getUsername())) {
            result.rejectValue("username", null, "このユーザー名は既に使用されています");
            return "register";
        }

        loginService.register(login.getUsername(), login.getPassword());
        return "redirect:/login";
    }

    @GetMapping("/login")
    public String loginInit(Model model) {
        model.addAttribute("login", new Login());
        return "login";
    }

    @PostMapping("/login")
    public String login(@Validated @ModelAttribute("login") Login login, BindingResult result, HttpSession session, Model model) {
        if (result.hasErrors()) return "login";

        Login user = loginService.authenticate(login.getUsername(), login.getPassword());
        if (user != null) {
            session.setAttribute("userId", user.getId()); 
            return "redirect:/tasks"; 
        } else {
            model.addAttribute("error", "ユーザー名かパスワードが違います");
            return "login";
        }
    }
}