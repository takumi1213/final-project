package sample.thymeleaf.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomePageController {

    /**
     * TOP画面 (http://localhost:8080/) を表示する
     */
    @GetMapping("/")
    public String index() {
        // templates/homePage.html を呼び出す
        return "homePage";
    }
}
