package sample.common.service;

import sample.common.dao.entity.Login;

public interface LoginService {
    // ログインチェックをして、成功したらユーザー情報を返す命令
    Login authenticate(String username, String password);
    
    // 新しいユーザーをDBに保存する命令
    void register(String username, String password);
    
    // ユーザー名が既に存在するか確認する命令
    boolean existsUsername(String username);
}