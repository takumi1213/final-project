package sample.thymeleaf.web;

import java.util.List;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import sample.common.dao.entity.Task;
import sample.common.service.TaskService;

@Controller
public class TaskController {

    @Autowired
    private TaskService taskService;

    /**
     * 新規作成画面を表示する
     */
    @GetMapping("/tasks/new")
    public String newTask(Model model, HttpSession session) {
        if (session.getAttribute("userId") == null) {
            return "redirect:/login";
        }
        model.addAttribute("task", new Task());
        return "tasks/new";
    }
    
    /**
     * タスク一覧画面を表示する（ページネーション対応）
     */
    @GetMapping("/tasks")
    public String list(
            @RequestParam(name = "page", defaultValue = "1") int page, // ★追加：URLの?page=nを取得
            Model model, 
            HttpSession session) {
        
        Long loginUserId = (Long) session.getAttribute("userId");
        if (loginUserId == null) {
            return "redirect:/login";
        }
        
        // 1ページあたりの表示件数
        int pageSize = 10;
        
        // ★修正：全件取得ではなく、ページ指定で取得する
        List<Task> tasks = taskService.findPageByUserId(loginUserId, page, pageSize); 
        
        // ★追加：全ページ数を計算する
        int totalPages = taskService.getTotalPages(loginUserId, pageSize);
        
        model.addAttribute("tasks", tasks);
        model.addAttribute("currentPage", page); // 今何ページ目か
        model.addAttribute("totalPages", totalPages); // 全部で何ページか
        
        return "tasks/list";
    }
    
    /**
     * タスクを登録する
     */
    @PostMapping("/tasks/insert")
    public String insert(Task task, HttpSession session) {
        Long loginUserId = (Long) session.getAttribute("userId");
        if (loginUserId == null) {
            return "redirect:/login";
        }
        task.setUserId(loginUserId);
        taskService.save(task);
        return "redirect:/tasks";
    }
    
    /**
     * 編集画面を表示する
     */
    @GetMapping("/tasks/edit/{id}")
    public String edit(@PathVariable("id") Long id, Model model, HttpSession session) {
        // ログインチェックと同時に、ログインユーザーのIDを取得
        Long loginUserId = (Long) session.getAttribute("userId");
        if (loginUserId == null) return "redirect:/login";

        // ★修正：新しく作った「ユーザー検証付き」のメソッドを呼ぶ
        // 他人のタスクIDを指定されたら、サービス層で自動的に例外（エラー）が飛ぶぜ！
        Task task = taskService.findByIdForUser(id, loginUserId);
        model.addAttribute("task", task);
        return "tasks/edit"; // 編集用のHTML
    }

    /**
     * 更新を実行する
     */
    @PostMapping("/tasks/update")
    public String update(Task task, HttpSession session) {
        Long loginUserId = (Long) session.getAttribute("userId");
        if (loginUserId == null) return "redirect:/login";
        
        // ★修正：新しく作った「ユーザー検証付き」のメソッドを呼ぶ
        // これで画面の書き換え攻撃（不正なuserIdの送りつけ）も完全に無効化するぜ
        taskService.updateForUser(task, loginUserId);
        return "redirect:/tasks";
    }
    
    /**
     * 削除を実行する（※GETメソッドの危険性はP0-03で直すから、まずはIDOR対策だけ！）
     */
    @GetMapping("/tasks/delete/{id}")
    public String delete(@PathVariable("id") Long id, HttpSession session) {
        Long loginUserId = (Long) session.getAttribute("userId");
        if (loginUserId == null) return "redirect:/login";
        
        // ★修正：新しく作った「ユーザー検証付き」のメソッドを呼ぶ
        // これでURLの数字をテキトーに変えて他人のタスクを消す悪戯は不可能になるぜ
        taskService.deleteForUser(id, loginUserId);
        return "redirect:/tasks"; // 削除後は一覧へ
    }
    
}