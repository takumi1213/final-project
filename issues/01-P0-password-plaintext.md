# [P0] パスワードが平文のままDBに保存・比較されている

## 概要
ユーザー登録時、パスワードがそのままDBに保存され、ログイン時も `String.equals` で平文比較されています。`application.properties` には `password.hash.code=SHA-256` という設定があるものの、コード側で使われていません。実務では「DBが流出した瞬間に全ユーザーの素のパスワードが漏れる」状態であり、最も重い指摘になります。

## 該当箇所
`samplePJ/app/src/main/java/sample/common/service/impl/LoginServiceImpl.java:14-28`

```java
@Override
public Login authenticate(String username, String password) {
    Login user = loginMapper.findByUsername(username);
    if (user != null && user.getPassword().equals(password)) {
        return user;
    }
    return null;
}

@Override
public void register(String username, String password) {
    Login login = new Login();
    login.setUsername(username);
    login.setPassword(password);  // ← 平文のままDBへ
    loginMapper.insert(login);
}
```

## 何が問題か
1. パスワードが平文で永続化されており、DB漏洩時の被害が致命的になります。
2. `equals` での平文比較はタイミング攻撃の対象にもなり得ます（実務上の優先度は1番より低いですが）。
3. ハッシュ化方式を `application.properties` に書いてある以上、コード側がそれを参照していないのは設定と実装の不整合です。
4. SHA-256 単体はソルトもストレッチもないため、本来パスワード用のハッシュアルゴリズムとしては適切ではありません（レインボーテーブル耐性が低い）。Spring Security 標準の `BCryptPasswordEncoder` を使うのがベストプラクティスです。
5. このまま運用に乗せると、IPAやOWASPの基準で言う「重大な脆弱性」を抱えたまま出すことになります。

## 修正方針
Spring Security の `BCryptPasswordEncoder` を使うのが最短かつ実務的です。Spring Security 全部入れたくない場合でも `spring-security-crypto` だけ依存に追加できます。

`build.gradle`
```gradle
implementation 'org.springframework.security:spring-security-crypto:6.2.4'
```

`PasswordEncoderConfig.java`（新規）
```java
package sample.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class PasswordEncoderConfig {
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
```

`LoginServiceImpl.java`
```java
@Service
public class LoginServiceImpl implements LoginService {

    private final LoginMapper loginMapper;
    private final PasswordEncoder passwordEncoder;

    public LoginServiceImpl(LoginMapper loginMapper, PasswordEncoder passwordEncoder) {
        this.loginMapper = loginMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public Login authenticate(String username, String rawPassword) {
        Login user = loginMapper.findByUsername(username);
        if (user == null) return null;
        return passwordEncoder.matches(rawPassword, user.getPassword()) ? user : null;
    }

    @Override
    public void register(String username, String rawPassword) {
        Login login = new Login();
        login.setUsername(username);
        login.setPassword(passwordEncoder.encode(rawPassword));
        loginMapper.insert(login);
    }
    // ...
}
```

なお、`login.password` カラムはBCryptハッシュ（60文字固定）が入る前提なので、DDLで `VARCHAR(60)` 以上を確保しているか合わせて確認してください。
`application.properties` の `password.hash.code=SHA-256` は使われていない設定なので削除でOKです。

## 検証
- 新しく `register` 経由でユーザーを作り、DBの `login.password` がBCryptハッシュ（`$2a$...`形式）になっていること
- 同じパスワードでログインできること
- 既存ユーザー（平文時代のレコード）はマイグレーションが必要 → 学習段階なら一度削除して作り直してOK

## 関連
- Issue 02（DBユーザーがpostgresスーパーユーザー）
- Issue 04（セッション固定攻撃対策）
