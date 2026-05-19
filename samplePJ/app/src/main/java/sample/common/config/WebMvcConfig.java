package sample.common.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    private final AuthInterceptor authInterceptor;
    public WebMvcConfig(AuthInterceptor authInterceptor) { this.authInterceptor = authInterceptor; }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // /tasks/** の配下はすべて門番（authInterceptor）を通す
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/tasks/**")
                .excludePathPatterns("/login", "/register", "/", "/css/**", "/js/**");
    }
}