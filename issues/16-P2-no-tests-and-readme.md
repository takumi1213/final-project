# [P2] テストが1本もない / READMEが無い

## 概要
`src/test` 配下にテストクラスがなく、`README.md` も存在しません。実務PRの観点では「ビルド・実行手順が分からない」「壊れていないことの保証がない」という減点項目になります。

## 該当箇所
- `samplePJ/app/src/test/` 配下にテストファイル無し（`build.gradle` には `spring-boot-starter-test` が入っているのに使われていない）
- リポジトリ直下に `README.md` 無し

## 何が問題か
1. リファクタリング時の安全網がない。今後 Issue 01〜10 を直すたびに手動でリグレッション確認が必要になります。
2. 別の人がリポジトリを clone した時に「どうやって動かす？」が分からず、PostgreSQLの起動方法・初期DDL・必要な環境変数が伝わりません。
3. 業務だと「READMEに書いてないとレビューを始めてもらえない」レベルで前提扱いされます。

## 修正方針
**最低限のテスト**

`samplePJ/app/src/test/java/sample/common/service/impl/TaskServiceImplTest.java`
```java
package sample.common.service.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sample.common.dao.mapper.TaskMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TaskServiceImplTest {

    @Mock TaskMapper taskMapper;
    @InjectMocks TaskServiceImpl sut;

    @Test
    void getTotalPages_25件_size10なら3ページ() {
        when(taskMapper.countByUserId(1L)).thenReturn(25);
        assertThat(sut.getTotalPages(1L, 10)).isEqualTo(3);
    }

    @Test
    void getTotalPages_0件なら0ページ() {
        when(taskMapper.countByUserId(1L)).thenReturn(0);
        assertThat(sut.getTotalPages(1L, 10)).isEqualTo(0);
    }
}
```

**README.md（リポジトリ直下）**

最低でも下記を書いておくと印象が大きく変わります。
```markdown
# samplePJ — シンプルTODOアプリ

Spring Boot + Thymeleaf + MyBatis で作ったログイン付きTODOアプリです。

## 必要環境
- Java 17
- PostgreSQL 15+
- Gradle Wrapper 同梱

## セットアップ
1. PostgreSQL でDB `todo_app` を作る
2. 下記DDLを流す（`docs/schema.sql`）
3. 環境変数を設定
   - `DB_URL=jdbc:postgresql://localhost:5433/todo_app`
   - `DB_USER=...`
   - `DB_PASSWORD=...`
4. `./gradlew :app:bootRun`

## DDL（抜粋）
... CREATE TABLE login / tasks の内容 ...

## 既知の課題
- 認証はHTTPセッション直叩きで簡易実装（Spring Security未導入）
- ...
```

## 検証
- `./gradlew :app:test` でテストが通ること
- READMEの手順通りでアプリが起動できること

## 関連
- Issue 04（DB接続情報）
