# Code Review Issues — はまもとたくみ さん 最終課題

レビュー対象: https://github.com/takumi1213/final-project.git (branch: `main`)
レビュー観点: 実務3年目レベル / Spring Boot + Thymeleaf + MyBatis
レビュー日: 2026-05-19

## 優先度の凡例
- **P0**: セキュリティ・データ整合性に直結。即修正。
- **P1**: 設計・保守性で、実務PRなら差し戻されるレベル。
- **P2**: 動くが品質改善。指摘はされるが、ブロッカーにはならない。

## Issue 一覧

| #  | 優先度 | タイトル | リンク |
| --- | --- | --- | --- |
| 01 | P0 | パスワードが平文のままDBに保存・比較されている | [01-P0-password-plaintext.md](./01-P0-password-plaintext.md) |
| 02 | P0 | 他人のタスクを編集・削除できてしまう（IDOR） | [02-P0-idor-task-edit-delete.md](./02-P0-idor-task-edit-delete.md) |
| 03 | P0 | 削除がGETメソッドで実装されており、CSRF対策もない | [03-P0-delete-via-get-and-no-csrf.md](./03-P0-delete-via-get-and-no-csrf.md) |
| 04 | P0 | DB接続情報がリポジトリにハードコードされ、スーパーユーザー接続 | [04-P0-db-credentials-and-superuser.md](./04-P0-db-credentials-and-superuser.md) |
| 05 | P1 | セッション固定攻撃対策がなく、ログアウト機能もない | [05-P1-session-fixation-and-logout.md](./05-P1-session-fixation-and-logout.md) |
| 06 | P1 | Entity を直接フォームバインディング（Mass Assignment） | [06-P1-mass-assignment-and-form-dto.md](./06-P1-mass-assignment-and-form-dto.md) |
| 07 | P1 | Service 層に `@Transactional` がない | [07-P1-transactional-missing.md](./07-P1-transactional-missing.md) |
| 08 | P1 | 例外設計とグローバルハンドラがない | [08-P1-exception-and-global-handler.md](./08-P1-exception-and-global-handler.md) |
| 09 | P1 | バリデーション不足・ページネーション入力のクランプ不足 | [09-P1-input-validation-and-page-clamp.md](./09-P1-input-validation-and-page-clamp.md) |
| 10 | P1 | ログイン判定が各メソッドにベタ書き（横断的関心事） | [10-P1-auth-interceptor-or-filter.md](./10-P1-auth-interceptor-or-filter.md) |
| 11 | P1 | .gitignore が不十分でIDE/OS固有ファイルがコミットされている | [11-P1-gitignore-and-committed-artifacts.md](./11-P1-gitignore-and-committed-artifacts.md) |
| 13 | P2 | 未使用メソッド・マジックナンバー・無意味な設定の整理 | [13-P2-dead-code-and-magic-numbers.md](./13-P2-dead-code-and-magic-numbers.md) |
| 14 | P2 | `Task.name` の二重管理、タイムスタンプの不整合 | [14-P2-task-entity-name-and-timestamps.md](./14-P2-task-entity-name-and-timestamps.md) |
| 15 | P2 | Thymeleafテンプレートの共通化・アクセシビリティ・整形 | [15-P2-thymeleaf-template-cleanup.md](./15-P2-thymeleaf-template-cleanup.md) |
| 16 | P2 | テストが1本もない / README が無い | [16-P2-no-tests-and-readme.md](./16-P2-no-tests-and-readme.md) |

## 推奨着手順
1. Issue 01 / 02 / 03 / 04（P0、いずれもセキュリティ直結）
2. Issue 11（公開リポジトリ前提なら無視できない）
3. Issue 05 / 06 / 10（Issue 02 と一緒に直すと整合が取りやすい）
4. Issue 07 / 08 / 09（設計・例外・入力検証）
5. Issue 13〜16（仕上げ / コード品質）
