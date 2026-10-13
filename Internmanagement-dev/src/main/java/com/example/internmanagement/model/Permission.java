package com.example.internmanagement.model;
public class Permission {
 
    private int id;
    private String permissionCode;
    private String permissionName;
    private String moduleGroup;
 
    public Permission() {
    }
 
    public Permission(
            int id,
            String permissionCode,
            String permissionName,
            String moduleGroup
    ) {
        this.id = id;
        this.permissionCode = permissionCode;
        this.permissionName = permissionName;
        this.moduleGroup = moduleGroup;
    }
 
    public int getId() {
        return id;
    }
 
    public void setId(int id) {
        this.id = id;
    }
 
    public String getPermissioncode() {
        return permissionCode;
    }
 
    public void setPermissioncode(String permissionCode) {
        this.permissionCode = permissionCode;
    }
 
    public String getPermissionname() {
        return permissionName;
    }
 
    public void setPermissionname(String permissionName) {
        this.permissionName = permissionName;
    }
 
    public String getModulegroup() {
        return moduleGroup;
    }
 
    public void setModulegroup(String moduleGroup) {
        this.moduleGroup = moduleGroup;
    }
}
