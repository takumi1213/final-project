package sample.common.dao.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import sample.common.dao.entity.Task;

@Mapper
public interface TaskMapper {
    
    // 特定のユーザーのタスクを「ページ指定」で取得する（ページネーション用）
    List<Task> findPageByUserId(@Param("userId") Long userId, 
                                @Param("offset") int offset, 
                                @Param("limit") int limit);

    // 全タスク件数を数える
    int countByUserId(@Param("userId") Long userId);

    // 全件取得
    List<Task> findAll();
    
    // 特定ユーザーのタスク全件取得
    List<Task> findByUserId(@Param("userId") Long userId);
    
    // 新規登録
    void insert(Task task);
    
    // 指定したIDのタスクを1件取得
    Task findById(@Param("id") Long id);

    // タスクを更新する
    void update(Task task);

    // タスクを削除（論理削除）する
    void delete(@Param("id") Long id);
}