package com.example.internmanagement.dao;

import com.example.internmanagement.util.DBConnection;

import java.sql.*;
import java.util.*;

public class MentorManagementDAO {
    public List<Map<String,Object>> findAll(String keyword, Long departmentId, String status) throws SQLException {
        String sql="SELECT m.id,m.mentor_code,COALESCE(u.full_name,m.full_name) full_name,"+
                "COALESCE(u.email,m.email) email,m.department_id,d.dept_name,m.status,m.user_id,"+
                "COUNT(CASE WHEN ip.internship_status='ACTIVE' THEN 1 END) intern_count "+
                "FROM mentors m LEFT JOIN users u ON u.id=m.user_id LEFT JOIN departments d ON d.id=m.department_id "+
                "LEFT JOIN intern_profiles ip ON ip.mentor_id=m.id WHERE (?='' OR COALESCE(u.full_name,m.full_name) LIKE CONCAT('%',?,'%') OR COALESCE(u.email,m.email) LIKE CONCAT('%',?,'%')) "+
                "AND (? IS NULL OR m.department_id=?) AND (?='ALL' OR m.status=?) "+
                "GROUP BY m.id,m.mentor_code,u.full_name,m.full_name,u.email,m.email,m.department_id,d.dept_name,m.status,m.user_id ORDER BY full_name";
        try(Connection c=DBConnection.getConnection();PreparedStatement p=c.prepareStatement(sql)){
            p.setString(1,keyword);p.setString(2,keyword);p.setString(3,keyword);setLong(p,4,departmentId);setLong(p,5,departmentId);p.setString(6,status);p.setString(7,status);
            try(ResultSet r=p.executeQuery()){return rows(r);}
        }
    }

    public List<Map<String,Object>> departments() throws SQLException {
        try(Connection c=DBConnection.getConnection();PreparedStatement p=c.prepareStatement("SELECT id,dept_code,dept_name FROM departments WHERE is_active=1 ORDER BY dept_name");ResultSet r=p.executeQuery()){return rows(r);}
    }

    public long create(String fullName,String email,long departmentId,long actorId,String ip) throws SQLException {
        try(Connection c=DBConnection.getConnection()){
            c.setAutoCommit(false);
            try{
                if(!departmentExists(c,departmentId)) throw new IllegalArgumentException("Phòng ban không hợp lệ.");
                if(mentorEmailExists(c,email)) throw new IllegalArgumentException("Email này đã có hồ sơ mentor.");
                Long userId=findMentorUser(c,email);
                String code="MTR-"+UUID.randomUUID().toString().substring(0,8).toUpperCase(Locale.ROOT);
                long id;
                try(PreparedStatement p=c.prepareStatement("INSERT INTO mentors(user_id,full_name,email,mentor_code,department_id,status) VALUES(?,?,?,?,?,'ACTIVE')",Statement.RETURN_GENERATED_KEYS)){
                    setLong(p,1,userId);p.setString(2,fullName);p.setString(3,email);p.setString(4,code);p.setLong(5,departmentId);p.executeUpdate();
                    try(ResultSet k=p.getGeneratedKeys()){if(!k.next())throw new SQLException("Không nhận được ID mentor.");id=k.getLong(1);}
                }
                try(PreparedStatement p=c.prepareStatement("INSERT INTO audit_logs(user_id,action_code,table_name,record_id,new_values_json,ip_address) VALUES(?,'CREATE_MENTOR','mentors',?,JSON_OBJECT('email',?,'department_id',?),?)")){
                    p.setLong(1,actorId);p.setLong(2,id);p.setString(3,email);p.setLong(4,departmentId);p.setString(5,ip);p.executeUpdate();
                }
                c.commit();return id;
            }catch(Exception e){c.rollback();throw e;}
        }
    }

    private Long findMentorUser(Connection c,String email)throws SQLException{
        String sql="SELECT u.id,GROUP_CONCAT(r.role_code) roles FROM users u LEFT JOIN user_roles ur ON ur.user_id=u.id LEFT JOIN roles r ON r.id=ur.role_id WHERE LOWER(u.email)=? GROUP BY u.id";
        try(PreparedStatement p=c.prepareStatement(sql)){p.setString(1,email);try(ResultSet r=p.executeQuery()){if(!r.next())return null;String roles=r.getString("roles");if(roles==null||!Arrays.asList(roles.split(",")).contains("MENTOR"))throw new IllegalArgumentException("Email này đã được dùng cho tài khoản không phải mentor.");return r.getLong("id");}}
    }
    private boolean mentorEmailExists(Connection c,String email)throws SQLException{try(PreparedStatement p=c.prepareStatement("SELECT 1 FROM mentors WHERE LOWER(email)=?")){p.setString(1,email);try(ResultSet r=p.executeQuery()){return r.next();}}}
    private boolean departmentExists(Connection c,long id)throws SQLException{try(PreparedStatement p=c.prepareStatement("SELECT 1 FROM departments WHERE id=? AND is_active=1")){p.setLong(1,id);try(ResultSet r=p.executeQuery()){return r.next();}}}
    private void setLong(PreparedStatement p,int i,Long value)throws SQLException{if(value==null)p.setNull(i,Types.BIGINT);else p.setLong(i,value);}
    private List<Map<String,Object>> rows(ResultSet r)throws SQLException{List<Map<String,Object>> out=new ArrayList<>();ResultSetMetaData md=r.getMetaData();while(r.next()){Map<String,Object> row=new LinkedHashMap<>();for(int i=1;i<=md.getColumnCount();i++)row.put(md.getColumnLabel(i),r.getObject(i));out.add(row);}return out;}
}
