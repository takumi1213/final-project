package sample.common.dao.entity;

import java.time.LocalDateTime;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Pattern;

public class Login {
    private Long id;

    @NotBlank(message = "ユーザー名を入力してください")
    @Pattern(regexp = "^[a-zA-Z0-9]+$", message = "ユーザー名は半角英数字で入力してください")
    private String username;

    @NotBlank(message = "パスワードを入力してください")
    @Size(min = 8, message = "パスワードは8文字以上で入力してください")
    @Pattern(regexp = "^[a-zA-Z0-9]+$", message = "パスワードは半角英数字で入力してください")
    private String password;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}