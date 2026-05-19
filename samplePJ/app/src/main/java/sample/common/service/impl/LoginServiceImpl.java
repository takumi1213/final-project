package sample.common.service.impl;

import org.springframework.stereotype.Service;
import sample.common.dao.entity.Login;
import sample.common.dao.mapper.LoginMapper;
import sample.common.service.LoginService;
// ★暗号化のためのライブラリをインポート
import org.springframework.security.crypto.password.PasswordEncoder;

@Service
public class LoginServiceImpl implements LoginService {

    private final LoginMapper loginMapper;
    // ★さっき作った PasswordEncoderConfig からこの道具（Bean）が呼び出されるぜ
    private final PasswordEncoder passwordEncoder;

    // ★実務で超推奨される「コンストラクタ注入」というスマートな合体技だ
    public LoginServiceImpl(LoginMapper loginMapper, PasswordEncoder passwordEncoder) {
        this.loginMapper = loginMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public Login authenticate(String username, String rawPassword) {
        Login user = loginMapper.findByUsername(username);
        
        // ★ user.getPassword().equals(password) からの卒業！
        // 画面からの生パスワード（rawPassword）と、DBの暗号化されたパスワード（ハッシュ値）を安全に比較するぜ
        if (user != null && passwordEncoder.matches(rawPassword, user.getPassword())) {
            return user;
        }
        return null;
    }

    @Override
    public void register(String username, String rawPassword) {
        Login login = new Login();
        login.setUsername(username);
        
        // ★パスワードを生のまま入れず、ガチガチに暗号化（ハッシュ化）してDBに送る！
        login.setPassword(passwordEncoder.encode(rawPassword));
        
        loginMapper.insert(login);
    }

    @Override
    public boolean existsUsername(String username) {
        return loginMapper.findByUsername(username) != null;
    }
}