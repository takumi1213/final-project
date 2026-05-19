package sample.thymeleaf.web;

import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.server.ResponseStatusException; // ★変更：Spring標準のクラスをインポート
import sample.common.exception.TaskNotFoundException;

/**
 * ★P1-08：アプリ全体の例外（エラー）をすべてキャッチして、
 * ユーザーに優しい専用エラー画面へ案内するグローバル司令塔だぜ！
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 1. タスクが見つからないエラー（TaskNotFoundException）をキャッチ！
     * HTTPステータスを「404 Not Found」にして、error/404.html を表示するぜ！
     */
    @ExceptionHandler(TaskNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleNotFound() {
        return "error/404";
    }

    /**
     * 2. 他人のタスクにアクセスしたエラー（ResponseStatusException）をキャッチ！
     * HTTPステータスを「403 Forbidden」にして、error/403.html を表示するぜ！
     */
    @ExceptionHandler(ResponseStatusException.class) // ★変更：Spring標準のクラスでキャッチ
    public String handleForbidden(ResponseStatusException ex) {
        // もし403エラーだったら、403画面へ案内するぜ
        if (ex.getStatusCode() == HttpStatus.FORBIDDEN) {
            return "error/403";
        }
        // それ以外のResponseStatusExceptionなら通常のエラー画面へ
        return "error";
    }

    /**
     * 3. 以前作った、その他の不正な引数エラー（IllegalArgumentException）も残しておくぜ！
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public String handleIllegalArgumentException(IllegalArgumentException ex, Model model) {
        model.addAttribute("errorMessage", ex.getMessage());
        return "error";
    }
}