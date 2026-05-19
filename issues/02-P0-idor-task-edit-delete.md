# [P0] 他人のタスクを編集・削除できてしまう（IDOR）

## 概要
タスクの編集 (`/tasks/edit/{id}`)・更新 (`/tasks/update`)・削除 (`/tasks/delete/{id}`) で、対象タスクの所有者（`user_id`）チェックが一切されていません。ログイン状態でさえあれば、URLの `id` をいじることで他ユーザーのタスクを編集・削除可能です。OWASP の言う典型的なIDOR（Insecure Direct Object Reference）です。

## 該当箇所
`samplePJ/app/src/main/java/sample/thymeleaf/web/TaskController.java:80-106`

```java
@GetMapping("/tasks/edit/{id}")
public String edit(@PathVariable("id") Long id, Model model, HttpSession session) {
    if (session.getAttribute("userId") == null) return "redirect:/login";
    Task task = taskService.findById(id);   // ← 所有者チェック無し
    model.addAttribute("task", task);
    return "tasks/edit";
}

@PostMapping("/tasks/update")
public String update(Task task, HttpSession session) {
    if (session.getAttribute("userId") == null) return "redirect:/login";
    taskService.update(task);               // ← userIdを検証していない
    return "redirect:/tasks";
}

@GetMapping("/tasks/delete/{id}")
public String delete(@PathVariable("id") Long id, HttpSession session) {
    if (session.getAttribute("userId") == null) return "redirect:/login";
    taskService.delete(id);                 // ← 所有者チェック無し
    return "redirect:/tasks";
}
```

## 何が問題か
1. ログインさえしていれば他人のタスクIDを推測して `/tasks/edit/999` のようにアクセスでき、編集画面が表示されてしまいます。
2. `/tasks/update` は `Task` をフォーム経由でバインドしているため、隠しフィールドの `userId` を改ざんすれば、他人のタスクを自分名義に書き換えることも可能です（Mass Assignment脆弱性も併発）。
3. 削除も同様で、ID直打ちで全消ししに行けます。
4. 認証（誰か）と認可（その人がその操作をしてよいか）が別概念であることを意識する必要があります。本実装は認証のみで認可がゼロです。

## 修正方針
Service 層で「ログインユーザーIDと対象タスクのuser_idが一致するか」を必ず確認する作りに変えます。Controller でやってもいいですが、ビジネスルールなのでService側が自然です。

`TaskService.java`
```java
Task findByIdForUser(Long id, Long userId);
void updateForUser(Task task, Long userId);
void deleteForUser(Long id, Long userId);
```

`TaskServiceImpl.java`
```java
@Override
public Task findByIdForUser(Long id, Long userId) {
    Task task = taskMapper.findById(id);
    if (task == null || !task.getUserId().equals(userId)) {
        throw new AccessDeniedException("対象タスクにアクセスできません");
    }
    return task;
}

@Override
public void updateForUser(Task task, Long userId) {
    Task current = findByIdForUser(task.getId(), userId);
    // userIdはサーバ側で上書き（Mass Assignment対策）
    task.setUserId(current.getUserId());
    taskMapper.update(task);
}

@Override
public void deleteForUser(Long id, Long userId) {
    findByIdForUser(id, userId); // 所有チェックついでに存在確認
    taskMapper.delete(id);
}
```

`TaskController.java`
```java
@GetMapping("/tasks/edit/{id}")
public String edit(@PathVariable("id") Long id, Model model, HttpSession session) {
    Long loginUserId = (Long) session.getAttribute("userId");
    if (loginUserId == null) return "redirect:/login";
    model.addAttribute("task", taskService.findByIdForUser(id, loginUserId));
    return "tasks/edit";
}

@PostMapping("/tasks/update")
public String update(Task task, HttpSession session) {
    Long loginUserId = (Long) session.getAttribute("userId");
    if (loginUserId == null) return "redirect:/login";
    taskService.updateForUser(task, loginUserId);
    return "redirect:/tasks";
}

@GetMapping("/tasks/delete/{id}")
public String delete(@PathVariable("id") Long id, HttpSession session) {
    Long loginUserId = (Long) session.getAttribute("userId");
    if (loginUserId == null) return "redirect:/login";
    taskService.deleteForUser(id, loginUserId);
    return "redirect:/tasks";
}
```

また、`edit.html` の `<input type="hidden" th:field="*{userId}">` は削除して構いません。サーバ側で必ず上書きするのが安全側です。

## 検証
- ユーザーAでタスク作成 → IDを控える
- ユーザーBでログインし `/tasks/edit/{Aのタスクid}` を直打ち → 403 もしくはエラー画面が出ること
- 同じく `/tasks/delete/{Aのタスクid}` も拒否されること
- ユーザーBが自分のタスクは編集・削除できること

## 関連
- Issue 03（削除がGETメソッドで実装されている → CSRFと相性が悪い）
- Issue 06（Mass Assignment）
