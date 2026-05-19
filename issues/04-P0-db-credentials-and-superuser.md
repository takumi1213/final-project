# [P0] DB接続情報がリポジトリにハードコードされ、かつスーパーユーザー(postgres)で接続している

## 概要
`application.properties` にDBユーザー・パスワードが直書きされ、しかも接続ユーザーが PostgreSQL のスーパーユーザーである `postgres` です。Gitに乗ったままなので、リポジトリを公開した瞬間に「DBのスーパーユーザー認証情報が公開」されている状態になります。

## 該当箇所
`samplePJ/app/src/main/resources/application.properties:7-10`

```properties
spring.datasource.driver-class-name=org.postgresql.Driver
spring.datasource.url=jdbc:postgresql://localhost:5433/todo_app
spring.datasource.username=postgres
spring.datasource.password=postgres
```

## 何が問題か
1. リポジトリに認証情報を載せる文化はNG。一度Git履歴に入ると `git log` から消すのは手間がかかります。
2. アプリ用DBユーザーには「対象スキーマへのCRUD」だけを許可した最小権限ユーザーを切るのが原則。`postgres` ユーザーはDB作成・破棄まで可能で、SQLインジェクション等で侵入された場合の被害が広がります。
3. 学習目的でもパスワードを `postgres` と同名にしているのは事故の元です。

## 修正方針
1. 接続情報を環境変数経由に切り替える
2. アプリ用ユーザーを別に切り、`todo_app` DBへの権限だけ付与する

`application.properties`
```properties
spring.datasource.driver-class-name=org.postgresql.Driver
spring.datasource.url=${DB_URL:jdbc:postgresql://localhost:5433/todo_app}
spring.datasource.username=${DB_USER:todo_app_user}
spring.datasource.password=${DB_PASSWORD:}
```

ローカル起動時は `.env` や IDE の Run Configuration で環境変数を渡します。`.env` 系ファイルは `.gitignore` に必ず追加してください。

PostgreSQL 側のセットアップ例
```sql
CREATE ROLE todo_app_user LOGIN PASSWORD '<強いランダム文字列>';
GRANT CONNECT ON DATABASE todo_app TO todo_app_user;
GRANT USAGE ON SCHEMA public TO todo_app_user;
GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA public TO todo_app_user;
GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA public TO todo_app_user;
```

なお、すでにリポジトリに `password=postgres` をコミット済みなので、本番に出すなら履歴から削除（`git filter-repo`等）+ ローカルでパスワード変更も合わせて行うのが安全です。

## 検証
- 環境変数を未設定で起動すると、デフォルト値の `todo_app_user` で接続を試みること
- `postgres` ユーザーでないことを `SELECT current_user;` で確認できること
- アプリ用ユーザーで `DROP TABLE` などを発行するとエラーになること

## 関連
- Issue 11（.gitignore不足）
