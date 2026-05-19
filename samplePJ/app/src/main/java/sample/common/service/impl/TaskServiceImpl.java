package sample.common.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import sample.common.dao.entity.Task;
import sample.common.dao.mapper.TaskMapper;
import sample.common.service.TaskService;
import sample.thymeleaf.form.TaskForm; // ★追加：Formクラスをインポート

@Service
public class TaskServiceImpl implements TaskService {

    @Autowired
    private TaskMapper taskMapper;

    @Override
    public List<Task> findPageByUserId(Long userId, int page, int size) {
        int offset = (page - 1) * size;
        return taskMapper.findPageByUserId(userId, offset, size);
    }

    @Override
    public int getTotalPages(Long userId, int size) {
        int totalTasks = taskMapper.countByUserId(userId);
        return (int) Math.ceil((double) totalTasks / size);
    }

    /**
     * ★P1-06：新規登録処理の実装
     * 画面用の TaskForm から、DB用の Task Entity へ必要な項目だけを詰め替えるぜ！
     */
    @Override
    public void create(TaskForm form, Long userId) {
        Task task = new Task();
        // 🛠️ セッションから取得した安全なログインユーザーIDをここでセットする！
        task.setUserId(userId);
        // 🛠️ 画面から入力された項目だけを厳選して詰め替える！
        task.setTitle(form.getTitle());
        task.setContent(form.getContent());
        task.setStartDate(form.getStartDate());
        task.setEndDate(form.getEndDate());
        
        taskMapper.insert(task);
    }

    @Override
    public Task findByIdForUser(Long id, Long userId) {
        Task task = taskMapper.findById(id);
        if (task == null || !task.getUserId().equals(userId)) {
            throw new IllegalArgumentException("対象のタスクにアクセスする権限がありません。");
        }
        return task;
    }

    /**
     * ★P1-06：更新処理の実装（TaskForm版）
     */
    @Override
    public void updateForUser(TaskForm form, Long userId) {
        // 1. まず、本当に本人のタスクかチェックする（他人のタスクIDだったらここでエラーになる）
        Task current = findByIdForUser(form.getId(), userId);
        
        // 2. 既存のEntityに対して、画面から変更していい項目だけを上書きする
        current.setTitle(form.getTitle());
        current.setContent(form.getContent());
        current.setStartDate(form.getStartDate());
        current.setEndDate(form.getEndDate());
        
        // ※ userId や createdAt は current（DBから取ってきた古いデータ）のまま変わらないから超安全！
        taskMapper.update(current);
    }

    @Override
    public void deleteForUser(Long id, Long userId) {
        findByIdForUser(id, userId);
        taskMapper.delete(id);
    }

    // --- 古い互換性のための残し ---
    @Override
    public void save(Task task) { taskMapper.insert(task); }
    @Override
    public List<Task> findAll() { return taskMapper.findAll(); }
    @Override
    public List<Task> findByUserId(Long userId) { return taskMapper.findByUserId(userId); }
}