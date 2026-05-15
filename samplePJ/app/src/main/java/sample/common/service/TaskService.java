package sample.common.service;

import java.util.List;
import sample.common.dao.entity.Task;

public interface TaskService {
    
    // 特定ユーザーのタスクを「ページ指定」で取得する
    List<Task> findPageByUserId(Long userId, int page, int size);
    
    // 全タスク数から、最大で何ページあるかを計算して返す
    int getTotalPages(Long userId, int size);

    // 新規登録する
    void save(Task task);
    
    // 編集：IDから1件取得
    Task findById(Long id);
    
    // 編集：更新実行
    void update(Task task);
    
    // 追加：削除（論理削除）を実行する
    void delete(Long id);

    // --- 以下、必要に応じて残すメソッド ---
    List<Task> findAll();
    List<Task> findByUserId(Long userId);
}