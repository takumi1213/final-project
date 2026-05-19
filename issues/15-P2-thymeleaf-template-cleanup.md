# [P2] テンプレートの共通化・アクセシビリティ・表示整形

## 概要
3つのフォーム系HTML（`new.html` / `edit.html` / `login.html` / `register.html`）でCSSがほぼコピペになっており、メンテ性が低い状態です。あわせて、`label` と `input` が `for/id` で紐づいていない箇所があり、最低限のアクセシビリティを満たしていません。`createdAt` 等の表示も `LocalDateTime.toString()` 任せで `2025-07-11T00:12:34.567` のような微妙な見た目になります。

## 該当箇所
- `samplePJ/app/src/main/resources/templates/tasks/new.html`
- `samplePJ/app/src/main/resources/templates/tasks/edit.html`
- `samplePJ/app/src/main/resources/templates/tasks/list.html:113-114`
- `samplePJ/app/src/main/resources/templates/login.html:66-69`

## 何が問題か
1. CSSがインラインで完全重複しており、デザインを変えるとき全テンプレートを書き換える必要があります。
2. `<label>タイトル</label> <input ...>` のように `for/id` がない状態。スクリーンリーダー利用者がフォーカスを正しく取れません。
3. 日時は `${#temporals.format(task.createdAt, 'yyyy-MM-dd HH:mm')}` のように整形した方が読みやすく、業務でも標準的です。
4. ボタンが `<a>` で実装されていて、削除のように副作用のあるものまでリンクで表現されています（Issue 03 と関連）。

## 修正方針
**共通CSS化**
`src/main/resources/static/css/common.css` を作り、テンプレートでは
```html
<link rel="stylesheet" th:href="@{/css/common.css}">
```
で読む。`<style>` ブロックは削除。

**Thymeleaf フラグメント化**
ヘッダ・フッタ・共通フォーム部品を `templates/fragments/layout.html` にまとめ、各画面から `th:replace` で取り込むのが定石です（学習レベルなら共通CSS化だけでも十分です）。

**ラベル紐付け**
```html
<label for="title">タイトル</label>
<input type="text" id="title" th:field="*{title}" required>
```
`th:field` を使うと `id` 属性が自動で付与されますが、`label` 側に `for` を明示する方が確実です。

**日付フォーマット**
```html
<td th:text="${#temporals.format(task.createdAt, 'yyyy-MM-dd HH:mm')}">2025-07-11 00:12</td>
<td th:text="${#temporals.format(task.startDate, 'yyyy-MM-dd')}">2025-07-10</td>
```

## 検証
- 画面の見た目が変わっていないこと
- スクリーンリーダー（macOS の VoiceOver でも）でラベル→入力にフォーカスが連動すること
- 日付表示にミリ秒が出ていないこと

## 関連
- Issue 03（削除を `<a>` ではなく `<form>` に）
