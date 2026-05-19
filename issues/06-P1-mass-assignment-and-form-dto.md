# [P1] Entity を直接フォームバインディングしている（Mass Assignment / 責務分離）

## 概要
`Task` Entity を Controller の `@ModelAttribute` でそのまま受けていて、フォーム入力からEntityの全フィールドが書き換え可能になっています。`id`, `userId`, `createdAt`, `updatedAt` までユーザー入力で上書きできるリスクがあります（Mass Assignment）。実務では「DBに直結するEntity」と「画面入出力用のForm/DTO」を分けるのが定石です。

## 該当箇所
`samplePJ/app/src/main/java/sample/thymeleaf/web/TaskController.java:66-98`

```java
@PostMapping("/tasks/insert")
public String insert(Task task, HttpSession session) {  // ← Entity直接バインド
    ...
    taskService.save(task);
    ...
}

@PostMapping("/tasks/update")
public String update(Task task, HttpSession session) {  // ← 同上
    taskService.update(task);
    ...
}
```

`samplePJ/app/src/main/resources/templates/tasks/edit.html:93-94`
```html
<input type="hidden" th:field="*{id}">
<input type="hidden" th:field="*{userId}">  <!-- ← フォームから userId が送れる -->
```

## 何が問題か
1. 攻撃者がフォーム改変で `userId=2` を送れば他人のタスクに変えられます（Issue 02 と直結）。
2. Entityがバリデーションアノテーション・画面用フォーマット・DB列の三役を兼ねていて、責務がぼやけます。
3. `createdAt`/`updatedAt` のような「アプリが管理すべき値」をユーザーから来た値で上書きする経路ができてしまいます。
4. `Task` の `name`（登録者名）はEntityにあるのに DB列としては存在する一方、ログインユーザーから自動で埋めるべき値で、フォーム入力させる必然がありません。

## 修正方針
画面用に `TaskForm` を切り、Controller では `TaskForm` を受けてService層で `Task` に詰め替えます。

`TaskForm.java`（新規）
```java
package sample.thymeleaf.form;

import java.time.LocalDate;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class TaskForm {
    private Long id;  // 編集時のみ

    @NotBlank(message = "タイトルを入力してください")
    @Size(max = 100, message = "タイトルは100文字以内で入力してください")
    private String title;

    @Size(max = 1000, message = "内容は1000文字以内で入力してください")
    private String content;

    private LocalDate startDate;
    private LocalDate endDate;
    // getter / setter
}
```

`TaskController.java`
```java
@PostMapping("/tasks/insert")
public String insert(@Validated @ModelAttribute("task") TaskForm form,
                     BindingResult result, HttpSession session) {
    Long loginUserId = (Long) session.getAttribute("userId");
    if (loginUserId == null) return "redirect:/login";
    if (result.hasErrors()) return "tasks/new";
    taskService.create(form, loginUserId);
    return "redirect:/tasks";
}
```

`TaskServiceImpl.java`
```java
@Override
public void create(TaskForm form, Long userId) {
    Task task = new Task();
    task.setUserId(userId);
    task.setTitle(form.getTitle());
    task.setContent(form.getContent());
    task.setStartDate(form.getStartDate());
    task.setEndDate(form.getEndDate());
    taskMapper.insert(task);
}
```

`edit.html` の `userId` 隠しフィールドは削除します。

## 検証
- フォーム送信後にDevToolsで `userId` を別の値に書き換え送信 → 自分のユーザーIDのままレコードが作成・更新されること
- バリデーションエラー時、入力値が保持されること

## 関連
- Issue 02（IDOR）
- Issue 09（バリデーション弱）
