package sample.thymeleaf.web;

import jakarta.servlet.http.HttpServletRequest; // ★追加：セッションを再発行するために必要
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
        if (result.hasErrors()) return "register";

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

    /**
     * ログインを実行する（★P1-05：セッション固定攻撃対策を追加！）
     */
    @PostMapping("/login")
    public String login(@Validated @ModelAttribute("login") Login login, 
                        BindingResult result, 
                        HttpServletRequest request, // ★修正：HttpSession から HttpServletRequest に変更
                        Model model) {
        if (result.hasErrors()) return "login";

        Login user = loginService.authenticate(login.getUsername(), login.getPassword());
        if (user != null) {
            
            // 🛠️ セッション固定攻撃対策：ログイン前の古いセッションを一度完全に爆破するぜ！
            HttpSession oldSession = request.getSession(false);
            if (oldSession != null) {
                oldSession.invalidate();
            }
            
            // 🛠️ まっさらな新しいセッション（新しいセッションID）を強制的に再生成する！
            HttpSession newSession = request.getSession(true);
            newSession.setAttribute("userId", user.getId()); 
            
            return "redirect:/tasks"; 
        } else {
            model.addAttribute("error", "ユーザー名かパスワードが違います");
            return "login";
        }
    }

    /**
     * ログアウトを実行する（★P1-05：新しく追加！）
     */
    @PostMapping("/logout")
    public String logout(HttpSession session) {
        // セッションを無効化（爆破）して、ログイン情報を消し去るぜ
        session.invalidate();
        // ログアウトしたらログイン画面へ戻す
        return "redirect:/login";
    }
}