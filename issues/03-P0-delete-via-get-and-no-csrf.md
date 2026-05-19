# [P0] 削除がGETメソッドで実装されており、CSRF対策もない

## 概要
タスク削除が `GET /tasks/delete/{id}` で実装されています。`<a>` リンクで叩く設計ですが、HTTP の流儀では「副作用のある操作はGETにしてはいけない」が原則です。さらに Spring Security が入っていないので CSRF トークン保護がなく、外部サイトに `<img src="http://.../tasks/delete/123">` を仕込まれるだけで削除されてしまいます。

## 該当箇所
`samplePJ/app/src/main/java/sample/thymeleaf/web/TaskController.java:100-106`

```java
@GetMapping("/tasks/delete/{id}")
public String delete(@PathVariable("id") Long id, HttpSession session) {
    if (session.getAttribute("userId") == null) return "redirect:/login";
    taskService.delete(id);
    return "redirect:/tasks";
}
```

`samplePJ/app/src/main/resources/templates/tasks/list.html:118-119`

```html
<a th:href="@{/tasks/delete/{id}(id=${task.id})}" class="btn-red"
   onclick="return confirm('このタスクを削除してもよろしいですか？');">削除</a>
```

## 何が問題か
1. GET で副作用がある＝ブラウザのプリフェッチや検索エンジンのクロールでも消える可能性があります。
2. CSRF対策がなく、ログイン中の被害者を別サイトに誘導するだけで削除を発火できます。
3. `confirm` はJavaScript一行で外せる気休めで、セキュリティ対策にはなりません。
4. RESTfulな原則からも、削除は `POST`（あるいは `DELETE`）であるべきです。

## 修正方針
削除を `POST` に変更し、可能であれば Spring Security を導入して CSRFトークンを必須化します。学習段階で Security 導入が重い場合でも、最低限「GET → POST」に変えるだけで露出は大きく減らせます。

`TaskController.java`
```java
@PostMapping("/tasks/delete/{id}")
public String delete(@PathVariable("id") Long id, HttpSession session) {
    Long loginUserId = (Long) session.getAttribute("userId");
    if (loginUserId == null) return "redirect:/login";
    taskService.deleteForUser(id, loginUserId);
    return "redirect:/tasks";
}
```

`list.html`
```html
<form th:action="@{/tasks/delete/{id}(id=${task.id})}" method="post" style="display:inline">
    <button type="submit" class="btn-red"
            onclick="return confirm('このタスクを削除してもよろしいですか？');">削除</button>
</form>
```

将来的には `spring-boot-starter-security` を追加し、CSRFトークンの hidden を `<input type="hidden" th:name="${_csrf.parameterName}" th:value="${_csrf.token}">` のように埋め込むのが標準です。

## 検証
- 削除ボタンを押して動作すること（POSTになっていることを開発者ツールで確認）
- アドレスバーに `/tasks/delete/1` をGETで直打ちしても 405 Method Not Allowed になること
- 他オリジンからのフォーム送信が拒否されること（Security導入後）

## 関連
- Issue 02（IDOR）
- Issue 04（セッション固定攻撃）
