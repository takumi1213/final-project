package sample.common.service;

import java.util.List;
import sample.common.dao.entity.Task;
import sample.thymeleaf.form.TaskForm; // ★追加：Formクラスをインポート

public interface TaskService {
    
    // 特定ユーザーのタスクを「ページ指定」で取得する
    List<Task> findPageByUserId(Long userId, int page, int size);
    
    // 全タスク数から、最大で何ページあるかを計算して返す
    int getTotalPages(Long userId, int size);

    // ★P1-06：新規登録する（引数を TaskForm に変更した新メソッド）
    void create(TaskForm form, Long userId);
    
    // ログインユーザーのIDも一緒に渡すようにする
    Task findByIdForUser(Long id, Long userId);
	
    // ★P1-06：更新を実行する（引数を TaskForm に変更した新メソッド）
    void updateForUser(TaskForm form, Long userId);
	
    // ログインユーザーのIDを渡して他人のタスクを消せないようにする
    void deleteForUser(Long id, Long userId);

    // --- 古い互換性のために残すメソッド（必要に応じて） ---
    void save(Task task);
    List<Task> findAll();
    List<Task> findByUserId(Long userId);
}