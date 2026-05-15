package sample.common.service.impl; // パッケージ名は環境に合わせてくれ

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import sample.common.dao.entity.Login;
import sample.common.dao.mapper.LoginMapper;
import sample.common.service.LoginService;

@Service
public class LoginServiceImpl implements LoginService {
    @Autowired
    private LoginMapper loginMapper;

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
        login.setPassword(password);
        loginMapper.insert(login);
    }

    @Override
    public boolean existsUsername(String username) {
        return loginMapper.findByUsername(username) != null;
    }
}