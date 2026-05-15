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
    public Task findById(Long id) {
        return taskMapper.findById(id);
    }

    @Override
    public void update(Task task) {
        taskMapper.update(task);
    }

    // ここで実際にMapperのdeleteを呼ぶ
    @Override
    public void delete(Long id) {
        taskMapper.delete(id);
    }

    @Override
    public List<Task> findAll() { return taskMapper.findAll(); }
    
    @Override
    public List<Task> findByUserId(Long userId) { return taskMapper.findByUserId(userId); }
}