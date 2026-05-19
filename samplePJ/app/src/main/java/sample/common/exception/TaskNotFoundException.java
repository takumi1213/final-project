package sample.common.exception;

/**
 * ★P1-08：タスクが見つからなかったときに発生させる専用のエラー（業務例外）
 */
public class TaskNotFoundException extends RuntimeException {
    
    public TaskNotFoundException(Long id) {
        super("指定されたタスク（ID: " + id + "）が見つかりません。");
    }
}
