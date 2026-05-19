package sample.common.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus; // ★追加
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException; // ★追加：Spring標準のエラークラス
import sample.common.dao.entity.Task;
import sample.common.dao.mapper.TaskMapper;
import sample.common.service.TaskService;
import sample.thymeleaf.form.TaskForm;

@Service
@Transactional // ★P1-07：クラス全体にトランザクションをかける！
public class TaskServiceImpl implements TaskService {

    @Autowired
    private TaskMapper taskMapper;

    // ★P1-07：参照系メソッドには (readOnly = true) を設定！
    @Override
    @Transactional(readOnly = true)
    public List<Task> findPageByUserId(Long userId, int page, int size) {
        int offset = (page - 1) * size;
        return taskMapper.findPageByUserId(userId, offset, size);
    }

    @Override
    @Transactional(readOnly = true)
    public int getTotalPages(Long userId, int size) {
        int totalTasks = taskMapper.countByUserId(userId);
        return (int) Math.ceil((double) totalTasks / size);
    }

    // 🛠️ 更新系メソッド
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

    // ★P1-08：存在しない場合は null を返さずに専用例外を投げるぜ！
    @Override
    @Transactional(readOnly = true)
    public Task findByIdForUser(Long id, Long userId) {
        Task task = taskMapper.findById(id);
        
        // 🛠️ そもそもデータがDBに無かったら「404用エラー（自作例外）」を投げる！
        if (task == null) {
            throw new sample.common.exception.TaskNotFoundException(id);
        }
        
        // 🛠️ データはあるけど、他人のタスクだったら「403用エラー（アクセス拒否）」を投げる！
        if (!task.getUserId().equals(userId)) {
            // ★ライブラリ不要なSpring標準のResponseStatusExceptionで403(FORBIDDEN)をぶん投げる！
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "このタスクへのアクセス権限がありません。");
        }
        return task;
    }

    // 🛠️ 更新系メソッド
    @Override
    public void updateForUser(TaskForm form, Long userId) {
        Task current = findByIdForUser(form.getId(), userId);
        
        current.setTitle(form.getTitle());
        current.setContent(form.getContent());
        current.setStartDate(form.getStartDate());
        current.setEndDate(form.getEndDate());
        
        taskMapper.update(current);
    }

    // 🛠️ 削除系メソッド
    @Override
    public void deleteForUser(Long id, Long userId) {
        findByIdForUser(id, userId);
        taskMapper.delete(id);
    }

    // --- 古い互換性のための残し ---
    @Override
    public void save(Task task) { taskMapper.insert(task); }
    
    @Override
    @Transactional(readOnly = true)
    public List<Task> findAll() { return taskMapper.findAll(); }
    
    @Override
    @Transactional(readOnly = true)
    public List<Task> findByUserId(Long userId) { return taskMapper.findByUserId(userId); }
}