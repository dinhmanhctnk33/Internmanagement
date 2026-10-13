package com.example.internmanagement.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class User{
    private int id;
    private String username;
    private String password;
    private String email;
    private String full_name;
    private String status;
    private LocalDateTime last_login_at;
    private LocalDate create_at;
    private LocalDate update_at;
    private boolean c_password;
    private String act_token;
    private LocalDate ex_date_at;
    private String phone_number;

    public User(){}

    public User(int id, String username, String password, String email,
                String full_name, String status, LocalDateTime last_login_at,
                LocalDate create_at, LocalDate update_at, boolean c_password,
                String act_token, LocalDate ex_date_at, String phone_number) {

        this.id = id;
        this.username = username;
        this.password = password;
        this.email = email;
        this.full_name = full_name;
        this.status = status;
        this.last_login_at = last_login_at;
        this.create_at = create_at;
        this.update_at = update_at;
        this.c_password = c_password;
        this.act_token = act_token;
        this.ex_date_at = ex_date_at;
        this.phone_number = phone_number;
    }

        public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFull_name() {
        return full_name;
    }

    public void setFull_name(String full_name) {
        this.full_name = full_name;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getLast_login_at() {
        return last_login_at;
    }

    public void setLast_login_at(LocalDateTime last_login_at) {
        this.last_login_at = last_login_at;
    }

    public LocalDate getCreate_at() {
        return create_at;
    }

    public void setCreate_at(LocalDate create_at) {
        this.create_at = create_at;
    }

    public LocalDate getUpdate_at() {
        return update_at;
    }

    public void setUpdate_at(LocalDate update_at) {
        this.update_at = update_at;
    }

    public boolean isC_password() {
        return c_password;
    }

    public void setC_password(boolean c_password) {
        this.c_password = c_password;
    }

    public String getAct_token() {
        return act_token;
    }

    public void setAct_token(String act_token) {
        this.act_token = act_token;
    }

    public LocalDate getEx_date_at() {
        return ex_date_at;
    }

    public void setEx_date_at(LocalDate ex_date_at) {
        this.ex_date_at = ex_date_at;
    }

    public String getPhone_number() {
        return phone_number;
    }

    public void setPhone_number(String phone_number) {
        this.phone_number = phone_number;
    }

    private String role;

    public String getRole() {
        return role != null ? role : "Quản trị viên";
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getFullName() {
        return full_name;
    }

    public void setFullName(String fullName) {
        this.full_name = fullName;
    }

    public String getShortName() {
        if (full_name != null && !full_name.isBlank()) {
            String[] parts = full_name.trim().split("\\s+");
            return parts[parts.length - 1];
        }
        if (username != null && !username.isBlank()) {
            return username;
        }
        return "Q";
    }

    // roleCode alias – used by admin JSP (${u.roleCode})
    private String roleCode;

    public String getRoleCode() {
        return roleCode != null ? roleCode : role;
    }

    public void setRoleCode(String roleCode) {
        this.roleCode = roleCode;
        this.role = roleCode;
    }
}