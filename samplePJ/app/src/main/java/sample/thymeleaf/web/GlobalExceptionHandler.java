package sample.thymeleaf.web;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

// ★アプリ全体の例外（エラー）を監視する特別なアノテーションだぜ！
@ControllerAdvice
public class GlobalExceptionHandler {

    // ★ IllegalArgumentException が発生したら、このメソッドが身代わりに出動する
    @ExceptionHandler(IllegalArgumentException.class)
    public String handleIllegalArgumentException(IllegalArgumentException ex, Model model) {
        
        // 発生したエラーメッセージ（「対象のタスクにアクセスする権限がありません。」）を画面に渡す
        model.addAttribute("errorMessage", ex.getMessage());
        
        // templates/error.html を表示する
        return "error";
    }
}