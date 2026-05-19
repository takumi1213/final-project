# [P1] ログイン判定が各メソッドにベタ書きされている（横断的関心事の漏れ）

## 概要
全ての Controller メソッドで `if (session.getAttribute("userId") == null) return "redirect:/login";` を書いています。書き忘れが起きた瞬間に未認証アクセスを許してしまう設計です。Spring の `HandlerInterceptor` または Filter で「保護対象URLは全部認証必須」と1箇所に集約するのが正攻法です。

## 該当箇所
`samplePJ/app/src/main/java/sample/thymeleaf/web/TaskController.java:25-106`

```java
if (session.getAttribute("userId") == null) return "redirect:/login";
```
が `newTask` / `list` / `insert` / `edit` / `update` / `delete` の各メソッドに重複しています。

## 何が問題か
1. DRY違反。1メソッド追加時に書き忘れると、未認証アクセスを許す致命的バグになります。
2. 認証は典型的な「横断的関心事」で、メソッド内に書く性質のものではないです。
3. Spring Security を入れるとこの種の話はフレームワーク任せにできますが、入れない場合でも `HandlerInterceptor` で十分集約できます。

## 修正方針
`AuthInterceptor.java`（新規）
```java
package sample.common.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AuthInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest req, HttpServletResponse res, Object handler) throws Exception {
        Object userId = req.getSession().getAttribute("userId");
        if (userId == null) {
            res.sendRedirect("/login");
            return false;
        }
        return true;
    }
}
```

`WebMvcConfig.java`（新規）
```java
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
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/tasks/**")
                .excludePathPatterns("/login", "/register", "/", "/css/**", "/js/**");
    }
}
```

これで `TaskController` の各メソッドから認証チェックを全部削除できます。ログインユーザーIDだけは引き続き session から取ればOK。

将来的に Spring Security を入れる場合は、これら全部 Security の `SecurityFilterChain` 設定に置き換えます。

## 検証
- ログアウト状態で `/tasks` 系URLにアクセス → `/login` にリダイレクトすること
- 新しい保護対象URLを追加しても、設定だけで認証必須になること

## 関連
- Issue 02（IDOR）
- Issue 05（セッション固定）
