package sample.common.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import sample.common.dao.entity.Task;
import sample.common.dao.mapper.TaskMapper;
import sample.common.service.TaskService;

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

    @Override
    public void save(Task task) {
        taskMapper.insert(task);
    }

    @Override
    public Task findByIdForUser(Long id, Long userId) {
        Task task = taskMapper.findById(id); // 元々あるMapperのメソッドを呼ぶ
        
        // ★ここが鉄壁の防御！タスクが存在しない、またはタスクの所有者とログインIDが違ったら即エラー！
        if (task == null || !task.getUserId().equals(userId)) {
            throw new IllegalArgumentException("対象のタスクにアクセスする権限がありません。");
        }
        return task;
    }

    @Override
    public void updateForUser(Task task, Long userId) {
        // 1. まず上のメソッドを使って、本当に本人のタスクかチェックする
        Task current = findByIdForUser(task.getId(), userId);
        
        // 2. 画面から悪意あるuserIdが送られてきても大丈夫なように、サーバー側で本人のIDを上書きする（Mass Assignment対策）
        task.setUserId(current.getUserId());
        
        taskMapper.update(task);
    }

    @Override
    public void deleteForUser(Long id, Long userId) {
        // 所有者チェックをついでに行い、問題なければ削除する
        findByIdForUser(id, userId);
        taskMapper.delete(id);
    }

    @Override
    public List<Task> findAll() { return taskMapper.findAll(); }
    
    @Override
    public List<Task> findByUserId(Long userId) { return taskMapper.findByUserId(userId); }
}