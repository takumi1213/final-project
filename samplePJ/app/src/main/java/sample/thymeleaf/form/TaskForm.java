package sample.thymeleaf.form;

import java.time.LocalDate;

/**
 * ★P1-06：画面からの入力値を安全に受け取るための専用クラス (Form DTO)
 * ユーザーに勝手に書き換えられたくない userId や createdAt などは、最初から排除してあるぜ！
 */
public class TaskForm {

    private Long id;          // 更新（edit）の時だけ使う
    private String title;     // 画面から入力されたタイトル
    private String content;   // 画面から入力された内容
    private LocalDate startDate; // 開始日
    private LocalDate endDate;   // 終了日

    // --- Getter & Setter ---

    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }
    public String getContent() {
        return content;
    }
    public void setContent(String content) {
        this.content = content;
    }
    public LocalDate getStartDate() {
        return startDate;
    }
    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }
    public LocalDate getEndDate() {
        return endDate;
    }
    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }
}