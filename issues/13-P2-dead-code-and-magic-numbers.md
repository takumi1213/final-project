# [P2] 未使用メソッド・マジックナンバー・無意味な設定の整理

## 概要
動くがコードベース上ノイズになっている要素がいくつかあります。実害は小さいですが、レビューでは確実に拾われる項目なので整理しておきましょう。

## 該当箇所と内容

### 1. 未使用のService/Mapperメソッド
`samplePJ/app/src/main/java/sample/common/service/TaskService.java:27-28`
```java
List<Task> findAll();
List<Task> findByUserId(Long userId);
```
`findPageByUserId` 導入後、`findAll` / `findByUserId` は誰も呼んでいません。Mapper にもXMLにも残っています（`samplePJ/app/src/main/resources/sample/common/dao/mapper/TaskMapper.xml:26-40`）。

### 2. マジックナンバー
`samplePJ/app/src/main/java/sample/thymeleaf/web/TaskController.java:48`
```java
int pageSize = 10;
```
意味のある定数として外出ししましょう。

### 3. 使われていない設定
`samplePJ/app/src/main/resources/application.properties:4`
```properties
password.hash.code=SHA-256
```
コード側で参照されておらず、Issue 01 を直すなら BCrypt 採用で別途要らなくなります。

### 4. WHATコメント
`samplePJ/app/src/main/java/sample/common/dao/entity/Task.java:30`
```java
// ここを getUserId / setUserId に修正
```
作業履歴メモが残っています。Gitが履歴を覚えているので、コードに残すのはノイズです。

### 5. 古い口調コメント
`samplePJ/app/src/main/java/sample/common/service/impl/LoginServiceImpl.java:1`
```java
package sample.common.service.impl; // パッケージ名は環境に合わせてくれ
```
テンプレ由来と思われる口語コメント。本人の手で書き直してください。

## 何が問題か
1. 未使用コードが残ると「これは将来使う？それとも消し忘れ？」をレビュアーが毎回判断する羽目になります。
2. マジックナンバーは「変更時にどこを直せばいいか」が分散します。
3. 使われていない設定キーは「使われている前提で読む」誤解を招きます。

## 修正方針
1. 未使用メソッドを Service / Mapper / XML から削除
2. `pageSize` を `private static final int PAGE_SIZE = 10;` として定数化（環境差で変えたいなら `@Value` で `application.properties` 化）
3. `password.hash.code` を削除（Issue 01 で BCrypt 化する前提）
4. WHAT/作業ログコメントを削除し、必要なら WHY コメントに書き直す

`TaskController.java`
```java
private static final int PAGE_SIZE = 10;

@GetMapping("/tasks")
public String list(@RequestParam(name = "page", defaultValue = "1") int page,
                   Model model, HttpSession session) {
    ...
    List<Task> tasks = taskService.findPageByUserId(loginUserId, page, PAGE_SIZE);
    int totalPages = taskService.getTotalPages(loginUserId, PAGE_SIZE);
    ...
}
```

## 検証
- 一覧表示が今まで通り動くこと
- 削除したメソッドへの参照が他に残っていないこと（IDEの "Find Usages"）

## 関連
- Issue 01（パスワードハッシュ）
