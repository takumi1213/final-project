# [P1] セッション固定攻撃対策がなく、ログアウト機能もない

## 概要
ログイン成功時にセッションIDを再生成していません。攻撃者が事前にセッションIDを被害者のブラウザに仕込めば、ログイン後もそのセッションを乗っ取れます（セッション固定攻撃）。また、ログアウト用のエンドポイントが存在しません。

## 該当箇所
`samplePJ/app/src/main/java/sample/thymeleaf/web/LoginController.java:47-59`

```java
@PostMapping("/login")
public String login(@Validated @ModelAttribute("login") Login login, BindingResult result, HttpSession session, Model model) {
    if (result.hasErrors()) return "login";

    Login user = loginService.authenticate(login.getUsername(), login.getPassword());
    if (user != null) {
        session.setAttribute("userId", user.getId());   // ← セッションIDが流用されたまま
        return "redirect:/tasks";
    } else {
        model.addAttribute("error", "ユーザー名かパスワードが違います");
        return "login";
    }
}
```

ログアウトエンドポイントは存在しない。

## 何が問題か
1. ログイン前のセッションをそのまま流用していて、固定化されたセッションIDで認証情報が乗せ替えられます。
2. ログアウトがないので「複数人が同一ブラウザを使う」「公共PCで使う」場合に痕跡が残ります。
3. 教科書的な対策は「ログイン直後に `session.invalidate()` → 新しいセッションを発行」です。

## 修正方針
`LoginController.java`
```java
@PostMapping("/login")
public String login(@Validated @ModelAttribute("login") Login login,
                    BindingResult result,
                    HttpServletRequest request,
                    Model model) {
    if (result.hasErrors()) return "login";

    Login user = loginService.authenticate(login.getUsername(), login.getPassword());
    if (user == null) {
        model.addAttribute("error", "ユーザー名かパスワードが違います");
        return "login";
    }

    // セッション固定攻撃対策：いったん破棄して新しいセッションを発行
    HttpSession oldSession = request.getSession(false);
    if (oldSession != null) oldSession.invalidate();
    HttpSession newSession = request.getSession(true);
    newSession.setAttribute("userId", user.getId());

    return "redirect:/tasks";
}

@PostMapping("/logout")
public String logout(HttpSession session) {
    session.invalidate();
    return "redirect:/login";
}
```

`list.html` などにログアウトボタンを追加
```html
<form th:action="@{/logout}" method="post" style="display:inline">
    <button type="submit">ログアウト</button>
</form>
```

将来的に Spring Security を導入すれば、`http.sessionManagement().sessionFixation().migrateSession()` がデフォルトで効くので、車輪の再発明は不要になります。

## 検証
- ログイン前後で `JSESSIONID` Cookie が変わること（ブラウザDevToolsで確認）
- ログアウト後に `/tasks` へアクセスすると `/login` にリダイレクトされること

## 関連
- Issue 01（パスワード平文）
- Issue 03（CSRF）
