# [P1] Service 層に `@Transactional` がない

## 概要
更新系処理に `@Transactional` が付いていません。今は1Mapper呼び出しで完結する処理ばかりなので「実害」までは出にくいですが、Service層は本来トランザクション境界として扱うのが定石で、将来「Mapperを2回呼ぶ」「ドメイン例外でロールバックしたい」となった時に取りこぼします。

## 該当箇所
`samplePJ/app/src/main/java/sample/common/service/impl/TaskServiceImpl.java:10-55`
`samplePJ/app/src/main/java/sample/common/service/impl/LoginServiceImpl.java:9-35`

## 何が問題か
1. トランザクション境界がないため、複数Mapperを跨ぐ処理に拡張したとき部分失敗のリスクが出ます。
2. 例外が起きた場合のロールバック挙動が「自動コミットの単発SQL」に依存し、レビュアーに意図が伝わりません。
3. クラス全体に付けて「参照系は `readOnly = true` で上書き」がSpringの推奨パターンです。

## 修正方針
クラス単位で `@Transactional` を付け、参照系メソッドにのみ `readOnly = true` を上書きします。

`TaskServiceImpl.java`
```java
@Service
@Transactional
public class TaskServiceImpl implements TaskService {

    private final TaskMapper taskMapper;
    public TaskServiceImpl(TaskMapper taskMapper) { this.taskMapper = taskMapper; }

    @Override
    @Transactional(readOnly = true)
    public List<Task> findPageByUserId(Long userId, int page, int size) { ... }

    @Override
    @Transactional(readOnly = true)
    public int getTotalPages(Long userId, int size) { ... }

    @Override
    public void save(Task task) { taskMapper.insert(task); }

    @Override
    @Transactional(readOnly = true)
    public Task findById(Long id) { return taskMapper.findById(id); }

    @Override
    public void update(Task task) { taskMapper.update(task); }

    @Override
    public void delete(Long id) { taskMapper.delete(id); }
}
```

## 検証
- 既存の正常系操作（登録・更新・削除）が引き続き動作すること
- 任意のサービス内で意図的に `RuntimeException` を投げて、コミットされていないこと（学習用に一時的に確認）

## 関連
- Issue 08（例外設計）
