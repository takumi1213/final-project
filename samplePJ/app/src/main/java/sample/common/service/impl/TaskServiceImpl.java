package sample.common.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import sample.common.dao.entity.Task;
import sample.common.dao.mapper.TaskMapper;
import sample.common.service.TaskService;
import sample.thymeleaf.form.TaskForm;

@Service
@Transactional
public class TaskServiceImpl implements TaskService {

    @Autowired
    private TaskMapper taskMapper;

    // ★P1-09：ページ番号がどんな値でも絶対に落ちない「クランプ」処理を実装！
    @Override
    @Transactional(readOnly = true)
    public List<Task> findPageByUserId(Long userId, int page, int size) {
        // 1. 最大ページ数を取得（最低でも1ページはある状態にする）
        int totalPages = Math.max(1, getTotalPages(userId, size));
        
        // 2. pageを [1, totalPages] の範囲内に強制的に収める（クランプ）
        int safePage = Math.min(Math.max(page, 1), totalPages);
        
        // 3. 安全なページ数から OFFSET を計算
        int offset = (safePage - 1) * size;
        return taskMapper.findPageByUserId(userId, offset, size);
    }

    @Override
    @Transactional(readOnly = true)
    public int getTotalPages(Long userId, int size) {
        int totalTasks = taskMapper.countByUserId(userId);
        return (int) Math.ceil((double) totalTasks / size);
    }

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

    @Override
    @Transactional(readOnly = true)
    public Task findByIdForUser(Long id, Long userId) {
        Task task = taskMapper.findById(id);
        
        if (task == null) {
            throw new sample.common.exception.TaskNotFoundException(id);
        }
        
        if (!task.getUserId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "このタスクへのアクセス権限がありません。");
        }
        return task;
    }

    @Override
    public void updateForUser(TaskForm form, Long userId) {
        Task current = findByIdForUser(form.getId(), userId);
        
        current.setTitle(form.getTitle());
        current.setContent(form.getContent());
        current.setStartDate(form.getStartDate());
        current.setEndDate(form.getEndDate());
        
        taskMapper.update(current);
    }

    @Override
    public void deleteForUser(Long id, Long userId) {
        findByIdForUser(id, userId);
        taskMapper.delete(id);
    }

    @Override
    public void save(Task task) { taskMapper.insert(task); }
    
    @Override
    @Transactional(readOnly = true)
    public List<Task> findAll() { return taskMapper.findAll(); }
    
    @Override
    @Transactional(readOnly = true)
    public List<Task> findByUserId(Long userId) { return taskMapper.findByUserId(userId); }
}