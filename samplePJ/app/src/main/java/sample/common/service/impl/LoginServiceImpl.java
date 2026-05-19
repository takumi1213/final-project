package sample.common.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // ★追加：トランザクションをインポート
import sample.common.dao.entity.Login;
import sample.common.dao.mapper.LoginMapper;
import sample.common.service.LoginService;
import org.springframework.security.crypto.password.PasswordEncoder;

@Service
@Transactional // ★P1-07：クラス全体にトランザクションをかけるぜ！更新系（register）は自動で保護されるぞ！
public class LoginServiceImpl implements LoginService {

    private final LoginMapper loginMapper;
    private final PasswordEncoder passwordEncoder;

    public LoginServiceImpl(LoginMapper loginMapper, PasswordEncoder passwordEncoder) {
        this.loginMapper = loginMapper;
        this.passwordEncoder = passwordEncoder;
    }

    // ★P1-07：データを読み取るだけのメソッドには (readOnly = true) で負荷軽減＆高速化！
    @Override
    @Transactional(readOnly = true)
    public Login authenticate(String username, String rawPassword) {
        Login user = loginMapper.findByUsername(username);
        if (user != null && passwordEncoder.matches(rawPassword, user.getPassword())) {
            return user;
        }
        return null;
    }

    // 🛠️ ユーザーの新規登録（更新系）は、クラスに付いている通常の @Transactional が効くからこのままで安全だぜ！
    @Override
    public void register(String username, String rawPassword) {
        Login login = new Login();
        login.setUsername(username);
        login.setPassword(passwordEncoder.encode(rawPassword));
        loginMapper.insert(login);
    }

    // ★P1-07：ここも読み取り専用に設定してパフォーマンスUP！
    @Override
    @Transactional(readOnly = true)
    public boolean existsUsername(String username) {
        return loginMapper.findByUsername(username) != null;
    }
}