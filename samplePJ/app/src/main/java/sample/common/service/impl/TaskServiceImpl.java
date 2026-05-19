package sample.common.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // ★追加：トランザクションのインポート
import sample.common.dao.entity.Task;
import sample.common.dao.mapper.TaskMapper;
import sample.common.service.TaskService;
import sample.thymeleaf.form.TaskForm;

@Service
@Transactional // ★P1-07：クラス全体にトランザクションをかける！これで更新系（登録・修正・削除）は自動で保護されるぜ！
public class TaskServiceImpl implements TaskService {

    @Autowired
    private TaskMapper taskMapper;

    // ★P1-07：データを取ってくるだけのメソッドには (readOnly = true) をつけて、DBの負荷を軽くするぜ！
    @Override
    @Transactional(readOnly = true)
    public List<Task> findPageByUserId(Long userId, int page, int size) {
        int offset = (page - 1) * size;
        return taskMapper.findPageByUserId(userId, offset, size);
    }

    // ★P1-07：ここも読み取り専用に設定！
    @Override
    @Transactional(readOnly = true)
    public int getTotalPages(Long userId, int size) {
        int totalTasks = taskMapper.countByUserId(userId);
        return (int) Math.ceil((double) totalTasks / size);
    }

    // 🛠️ createメソッドはデータの追加（更新系）だから、クラスに付いている通常の @Transactional が自動で効くぜ！
    @Override
    public void create(TaskForm form, Long userId) {
        Task task = new Task();
        task.setUserId(userId);
        task.setTitle(form.getTitle());
        task.setContent(form.getContent());
        task.setStartDate(form.getStartDate());
        task.setEndDate(form.getEndDate());
        
        taskMapper.insert(task);
    }

    // ★P1-07：ここも読み取り専用！
    @Override
    @Transactional(readOnly = true)
    public Task findByIdForUser(Long id, Long userId) {
        Task task = taskMapper.findById(id);
        if (task == null || !task.getUserId().equals(userId)) {
            throw new IllegalArgumentException("対象のタスクにアクセスする権限がありません。");
        }
        return task;
    }

    // 🛠️ 更新系メソッド。自動でトランザクション管理されるぜ！
    @Override
    public void updateForUser(TaskForm form, Long userId) {
        Task current = findByIdForUser(form.getId(), userId);
        
        current.setTitle(form.getTitle());
        current.setContent(form.getContent());
        current.setStartDate(form.getStartDate());
        current.setEndDate(form.getEndDate());
        
        taskMapper.update(current);
    }

    // 🛠️ 削除系メソッド。これも自動で安全に保護されるぜ！
    @Override
    public void deleteForUser(Long id, Long userId) {
        findByIdForUser(id, userId);
        taskMapper.delete(id);
    }

    // --- 古い互換性のための残し ---
    @Override
    public void save(Task task) { taskMapper.insert(task); }
    
    @Override
    @Transactional(readOnly = true) // ★一応ここも読み取り専用に！
    public List<Task> findAll() { return taskMapper.findAll(); }
    
    @Override
    @Transactional(readOnly = true) // ★ここも！
    public List<Task> findByUserId(Long userId) { return taskMapper.findByUserId(userId); }
}