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
import sample.thymeleaf.form.TaskForm; // ★追加：作ったFormをインポート

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
        // ★修正：空のEntityではなく、空のFormを画面に渡すぜ！
        model.addAttribute("task", new TaskForm());
        return "tasks/new";
    }
    
    /**
     * タスク一覧画面を表示する（ページネーション対応）
     */
    @GetMapping("/tasks")
    public String list(
            @RequestParam(name = "page", defaultValue = "1") int page, 
            Model model, 
            HttpSession session,
            jakarta.servlet.http.HttpServletResponse response) {
        
        Long loginUserId = (Long) session.getAttribute("userId");
        if (loginUserId == null) {
            return "redirect:/login";
        }
        
        response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate, max-age=0");
        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Expires", 0);
        
        int pageSize = 10;
        List<Task> tasks = taskService.findPageByUserId(loginUserId, page, pageSize); 
        int totalPages = taskService.getTotalPages(loginUserId, pageSize);
        
        model.addAttribute("tasks", tasks);
        model.addAttribute("currentPage", page); 
        model.addAttribute("totalPages", totalPages); 
        
        return "tasks/list";
    }
    
    /**
     * タスクを登録する（★修正：引数を Task から TaskForm に変更）
     */
    @PostMapping("/tasks/insert")
    public String insert(TaskForm form, HttpSession session) { // ← ★ここを Form に！
        Long loginUserId = (Long) session.getAttribute("userId");
        if (loginUserId == null) {
            return "redirect:/login";
        }
        // ★修正：Service層の新メソッドにFormとユーザーIDを託す！
        taskService.create(form, loginUserId);
        return "redirect:/tasks";
    }
    
    /**
     * 編集画面を表示する
     */
    @GetMapping("/tasks/edit/{id}")
    public String edit(@PathVariable("id") Long id, Model model, HttpSession session) {
        Long loginUserId = (Long) session.getAttribute("userId");
        if (loginUserId == null) return "redirect:/login";

        // DBからEntityを持ってくる
        Task task = taskService.findByIdForUser(id, loginUserId);
        
        // ★修正：Entityのデータを、画面用の Form に安全に詰め替えてから画面に渡す！
        TaskForm form = new TaskForm();
        form.setId(task.getId());
        form.setTitle(task.getTitle());
        form.setContent(task.getContent());
        form.setStartDate(task.getStartDate());
        form.setEndDate(task.getEndDate());
        
        model.addAttribute("task", form); // ← 画面にはFormを渡すぜ
        return "tasks/edit"; 
    }

    /**
     * 更新を実行する（★修正：引数を Task から TaskForm に変更）
     */
    @PostMapping("/tasks/update")
    public String update(TaskForm form, HttpSession session) { // ← ★ここを Form に！
        Long loginUserId = (Long) session.getAttribute("userId");
        if (loginUserId == null) return "redirect:/login";
        
        // ★修正：Service層の新メソッドにFormとユーザーIDを託す！
        taskService.updateForUser(form, loginUserId);
        return "redirect:/tasks";
    }
    
    /**
     * 削除を実行する
     */
    @PostMapping("/tasks/delete")
    public String delete(@RequestParam("id") Long id, HttpSession session) {
        Long loginUserId = (Long) session.getAttribute("userId");
        if (loginUserId == null) return "redirect:/login";
        
        taskService.deleteForUser(id, loginUserId);
        return "redirect:/tasks"; 
    }
}