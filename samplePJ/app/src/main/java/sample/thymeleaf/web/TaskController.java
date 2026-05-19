package sample.thymeleaf.web;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import sample.common.dao.entity.Task;
import sample.common.service.TaskService;
import sample.thymeleaf.form.TaskForm;

@Controller
public class TaskController {

    @Autowired
    private TaskService taskService;

    // --- 一覧表示 ---
    @GetMapping("/tasks")
    public String list(@RequestParam(name = "page", defaultValue = "1") int page,
                       Model model, HttpSession session) {
        // 認証チェックは Interceptor が行うので削除済み！
        Long userId = (Long) session.getAttribute("userId");

        int pageSize = 10;
        model.addAttribute("tasks", taskService.findPageByUserId(userId, page, pageSize));
        model.addAttribute("totalPages", taskService.getTotalPages(userId, pageSize));
        model.addAttribute("currentPage", page);
        return "tasks/list";
    }

    // --- 新規登録（表示） ---
    @GetMapping("/tasks/new")
    public String showNew(Model model) {
        model.addAttribute("task", new TaskForm());
        return "tasks/new";
    }

    // --- 新規登録（保存） ---
    @PostMapping("/tasks/insert")
    public String insert(@Valid @ModelAttribute("task") TaskForm form, 
                         BindingResult bindingResult, 
                         HttpSession session) {
        
        if (bindingResult.hasErrors()) {
            return "tasks/new";
        }

        Long userId = (Long) session.getAttribute("userId");
        taskService.create(form, userId);
        return "redirect:/tasks";
    }

    // --- 編集画面 ---
    @GetMapping("/tasks/edit/{id}")
    public String edit(@PathVariable("id") Long id, Model model, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        Task task = taskService.findByIdForUser(id, userId);
        
        TaskForm form = new TaskForm();
        form.setId(task.getId());
        form.setTitle(task.getTitle());
        form.setContent(task.getContent());
        form.setStartDate(task.getStartDate());
        form.setEndDate(task.getEndDate());
        
        model.addAttribute("task", form);
        return "tasks/edit";
    }

    // --- 編集実行（保存） ---
    @PostMapping("/tasks/update")
    public String update(@Valid @ModelAttribute("task") TaskForm form, 
                         BindingResult bindingResult, 
                         HttpSession session) {
        
        if (bindingResult.hasErrors()) {
            return "tasks/edit";
        }

        Long userId = (Long) session.getAttribute("userId");
        taskService.updateForUser(form, userId);
        return "redirect:/tasks";
    }

    // --- 削除 ---
    @PostMapping("/tasks/delete/{id}")
    public String delete(@PathVariable("id") Long id, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        taskService.deleteForUser(id, userId);
        return "redirect:/tasks";
    }
}