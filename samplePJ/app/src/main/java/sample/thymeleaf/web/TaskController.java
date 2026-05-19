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
            @RequestParam(name = "page", defaultValue = "1") int page, 
            Model model, 
            HttpSession session) {
        
        Long loginUserId = (Long) session.getAttribute("userId");
        if (loginUserId == null) {
            return "redirect:/login";
        }
        
        int pageSize = 10;
        List<Task> tasks = taskService.findPageByUserId(loginUserId, page, pageSize); 
        int totalPages = taskService.getTotalPages(loginUserId, pageSize);
        
        model.addAttribute("tasks", tasks);
        model.addAttribute("currentPage", page); 
        model.addAttribute("totalPages", totalPages); 
        
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
        Long loginUserId = (Long) session.getAttribute("userId");
        if (loginUserId == null) return "redirect:/login";

        // P0-02：他人のIDが来たらサービス層で403を投げる
        Task task = taskService.findByIdForUser(id, loginUserId);
        model.addAttribute("task", task);
        return "tasks/edit"; 
    }

    /**
     * 更新を実行する
     */
    @PostMapping("/tasks/update")
    public String update(Task task, HttpSession session) {
        Long loginUserId = (Long) session.getAttribute("userId");
        if (loginUserId == null) return "redirect:/login";
        
        // P0-02：不正な書き換えを防止
        taskService.updateForUser(task, loginUserId);
        return "redirect:/tasks";
    }
    
    /**
     * 削除を実行する（★P0-03：GETからPOSTに変更、URLもすっきり！）
     */
    @PostMapping("/tasks/delete")
    public String delete(@RequestParam("id") Long id, HttpSession session) {
        Long loginUserId = (Long) session.getAttribute("userId");
        if (loginUserId == null) return "redirect:/login";
        
        // 安全な削除処理を呼び出す
        taskService.deleteForUser(id, loginUserId);
        return "redirect:/tasks"; 
    }
}