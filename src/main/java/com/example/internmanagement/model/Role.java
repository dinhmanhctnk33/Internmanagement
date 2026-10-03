package com.example.internmanagement.model;

public class Role{
    private int id;
    private String role_code;
    private String role_name;
    private String description;
    private boolean is_system;

    public Role(){

    }

    public Role(int id, String role_code, String role_name, String desription, boolean is_system){
        this.id = id;
        this.role_code = role_code;
        this.role_name = role_name;
        this.description = desription;
        this.is_system = is_system;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getRole_code() {
        return role_code;
    }

    public void setRole_code(String role_code) {
        this.role_code = role_code;
    }

    public String getRole_name() {
        return role_name;
    }

    public void setRole_name(String role_name) {
        this.role_name = role_name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isIs_system() {
        return is_system;
    }

    public void setIs_system(boolean is_system) {
        this.is_system = is_system;
    }
}