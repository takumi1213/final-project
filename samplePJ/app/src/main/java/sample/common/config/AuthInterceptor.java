package sample.common.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AuthInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest req, HttpServletResponse res, Object handler) throws Exception {
        // セッションを確認し、なければログイン画面へ強制送還！
        if (req.getSession().getAttribute("userId") == null) {
            res.sendRedirect("/login");
            return false;
        }
        return true;
    }
}