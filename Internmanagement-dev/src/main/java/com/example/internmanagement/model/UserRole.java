package com.example.internmanagement.model;
import java.time.LocalDateTime;
 
public class UserRole {
 
    private int userId;
    private int roleId;
    private LocalDateTime assignedAt;
 
    public UserRole() {
    }
 
    public UserRole(
            int userId,
            int roleId,
            LocalDateTime assignedAt
    ) {
        this.userId = userId;
        this.roleId = roleId;
        this.assignedAt = assignedAt;
    }
 
    public int getUserid() {
        return userId;
    }
 
    public void setUserid(int userId) {
        this.userId = userId;
    }
 
    public int getRoleid() {
        return roleId;
    }
 
    public void setRoleid(int roleId) {
        this.roleId = roleId;
    }
 
    public LocalDateTime getAssignedat() {
        return assignedAt;
    }
 
    public void setAssignedat(LocalDateTime assignedAt) {
        this.assignedAt = assignedAt;
    }
}
