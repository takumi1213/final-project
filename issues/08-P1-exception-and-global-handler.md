# [P1] 例外設計とグローバルハンドラがない（NPEで500が出る経路あり）

## 概要
`taskService.findById(id)` が `null` を返したケースで、後続の Thymeleaf レンダリングや所有者チェックで `NullPointerException` になり、ユーザー側にSpring Bootデフォルトの500画面（スタックトレース付き）が表示されます。情報漏洩観点でも、UX観点でも避けたい挙動です。業務例外と `@ControllerAdvice` でのグローバルハンドラを整備すべきタイミングです。

## 該当箇所
`samplePJ/app/src/main/java/sample/thymeleaf/web/TaskController.java:80-87`

```java
@GetMapping("/tasks/edit/{id}")
public String edit(@PathVariable("id") Long id, Model model, HttpSession session) {
    if (session.getAttribute("userId") == null) return "redirect:/login";
    Task task = taskService.findById(id);   // ← nullの可能性あり
    model.addAttribute("task", task);
    return "tasks/edit";
}
```

`samplePJ/app/src/main/java/sample/common/service/impl/TaskServiceImpl.java:34-37`
```java
@Override
public Task findById(Long id) {
    return taskMapper.findById(id);  // 該当なしならnullを返す
}
```

## 何が問題か
1. `null` の伝播はバグの温床。`Optional` を返すか、業務例外を投げる方針に統一すべきです。
2. グローバル例外ハンドラがないため、想定外例外がそのままスタックトレースとしてユーザーに見えます。
3. 404相当の状態（存在しないID）と403相当の状態（他人のリソース）が区別されていません。

## 修正方針
業務例外クラスを切る → `@ControllerAdvice` で受ける、というのが王道です。

`TaskNotFoundException.java`（新規）
```java
package sample.common.exception;
public class TaskNotFoundException extends RuntimeException {
    public TaskNotFoundException(Long id) { super("task not found: id=" + id); }
}
```

`TaskServiceImpl.java`
```java
@Override
@Transactional(readOnly = true)
public Task findByIdForUser(Long id, Long userId) {
    Task task = taskMapper.findById(id);
    if (task == null) throw new TaskNotFoundException(id);
    if (!task.getUserId().equals(userId)) throw new AccessDeniedException("forbidden");
    return task;
}
```

`GlobalExceptionHandler.java`（新規）
```java
package sample.thymeleaf.web;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import sample.common.exception.TaskNotFoundException;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(TaskNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleNotFound() { return "error/404"; }

    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public String handleForbidden() { return "error/403"; }
}
```

`templates/error/404.html` `templates/error/403.html` を用意。Spring Boot は `templates/error/{code}.html` を自動マッピングする仕組みもあるので、`@ControllerAdvice` を使わずそれだけでも可。

## 検証
- 存在しない `/tasks/edit/99999` → 404ページが出ること
- 他ユーザーのID → 403ページが出ること
- ログにはスタックトレースが残るが、ユーザー画面には漏れないこと

## 関連
- Issue 02（IDOR）
- Issue 07（トランザクション）
