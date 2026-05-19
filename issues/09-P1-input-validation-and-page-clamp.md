# [P1] タスクのバリデーションと、ページネーション入力のクランプが不足

## 概要
タスク登録・更新時、`Task` エンティティに Bean Validation のアノテーションが一切付いていません。タイトル空欄でも登録できますし、内容に長大な文字列を入れることも可能です。さらに、一覧画面のページパラメータが負の値や巨大な値でもそのまま OFFSET 計算に流れていきます。

## 該当箇所
`samplePJ/app/src/main/java/sample/common/dao/entity/Task.java:9-19`（バリデーション無し）

`samplePJ/app/src/main/java/sample/thymeleaf/web/TaskController.java:37-61`
```java
@GetMapping("/tasks")
public String list(
        @RequestParam(name = "page", defaultValue = "1") int page,
        Model model, HttpSession session) {
    ...
    List<Task> tasks = taskService.findPageByUserId(loginUserId, page, pageSize);
    ...
}
```

`samplePJ/app/src/main/java/sample/common/service/impl/TaskServiceImpl.java:17-21`
```java
public List<Task> findPageByUserId(Long userId, int page, int size) {
    int offset = (page - 1) * size;  // page=0 → offset=-10 になり SQL エラー
    return taskMapper.findPageByUserId(userId, offset, size);
}
```

## 何が問題か
1. `page=0` や `page=-1` の場合、OFFSET が負数になり PostgreSQL がエラーを返します。
2. `page=99999999` のような大きな値は、SQLにそのまま流れて無駄な計算を発生させます。
3. タイトル必須・長さ上限のような業務ルールがDB側のみに頼っており、エラー画面の見栄えがDB依存になります。
4. `Task` Entity と画面用Formを分けると、Formにバリデーションをまとめて配置できます（Issue 06 と合わせると綺麗）。

## 修正方針
**入力クランプ**

`TaskServiceImpl.java`
```java
private static final int MIN_PAGE = 1;

@Override
public List<Task> findPageByUserId(Long userId, int page, int size) {
    int totalPages = Math.max(1, getTotalPages(userId, size));
    int safePage = Math.min(Math.max(page, MIN_PAGE), totalPages);
    int offset = (safePage - 1) * size;
    return taskMapper.findPageByUserId(userId, offset, size);
}
```

または Controller 側で
```java
int safePage = Math.max(1, page);
```
を入れるだけでも最低限のガードになります。

**バリデーション**（Issue 06 の `TaskForm` 前提）

```java
public class TaskForm {
    private Long id;

    @NotBlank(message = "タイトルを入力してください")
    @Size(max = 100, message = "タイトルは100文字以内で入力してください")
    private String title;

    @Size(max = 1000, message = "内容は1000文字以内で入力してください")
    private String content;

    private LocalDate startDate;

    @FutureOrPresent(message = "終了日は今日以降にしてください") // 任意
    private LocalDate endDate;

    // クロスフィールド検証で startDate <= endDate を担保したい場合はカスタムバリデータ
}
```

`new.html` / `edit.html` には Thymeleaf のエラー表示を追加します。
```html
<p class="error-message" th:if="${#fields.hasErrors('title')}" th:errors="*{title}"></p>
```

## 検証
- `?page=0` `?page=-5` `?page=99999` を叩いて 500 が出ないこと
- タイトル空欄で送信 → エラーメッセージが表示され、再表示時に入力値が保持されること
- タイトル101文字 → エラーが出ること

## 関連
- Issue 06（Form/DTO分離）
