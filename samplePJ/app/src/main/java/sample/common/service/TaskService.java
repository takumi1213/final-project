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
    
	 // 旧：Task findById(Long id);
	 // 新：ログインユーザーのIDも一緒に渡すようにする
	 Task findByIdForUser(Long id, Long userId);
	
	 // 旧：void update(Task task);
	 // 新：ログインユーザーのIDを渡して検証＆上書きできるようにする
	 void updateForUser(Task task, Long userId);
	
	 // 旧：void delete(Long id);
	 // 新：ログインユーザーのIDを渡して他人のタスクを消せないようにする
	 void deleteForUser(Long id, Long userId);

    // --- 以下、必要に応じて残すメソッド ---
    List<Task> findAll();
    List<Task> findByUserId(Long userId);
}