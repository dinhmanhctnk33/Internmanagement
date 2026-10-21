package com.example.internmanagement.dao;

import com.example.internmanagement.util.DBConnection;
import java.sql.*;
import java.time.LocalDate;
import java.util.*;

/** Queries shared by the intern progress and weekly-report workflows. */
public class InternWorkDAO {
    public Long findActiveProfileId(int userId) throws SQLException {
        try (Connection c=DBConnection.getConnection(); PreparedStatement p=c.prepareStatement(
                "SELECT id FROM intern_profiles WHERE user_id=? AND internship_status='ACTIVE' LIMIT 1")) {
            p.setInt(1,userId); try(ResultSet r=p.executeQuery()){ return r.next()?r.getLong(1):null; }
        }
    }

    public List<Map<String,Object>> tasks(long profileId) throws SQLException {
        String sql="SELECT t.id,t.task_title,t.description,t.due_date,t.priority,t.completion_percent,t.status,"+
                "t.intern_note,t.progress_updated_at,m.full_name mentor_name FROM tasks t LEFT JOIN mentors m ON m.id=t.mentor_id "+
                "WHERE t.intern_profile_id=? ORDER BY CASE t.status WHEN 'IN_PROGRESS' THEN 0 WHEN 'TODO' THEN 1 ELSE 2 END,t.due_date,t.id DESC";
        try(Connection c=DBConnection.getConnection();PreparedStatement p=c.prepareStatement(sql)){p.setLong(1,profileId);return rows(p);}
    }

    public void updateProgress(long profileId,long taskId,int percent,String status,String note) throws SQLException {
        try(Connection c=DBConnection.getConnection()){
            c.setAutoCommit(false);
            try{
                long mentorUserId;
                String title;
                try(PreparedStatement p=c.prepareStatement("SELECT m.user_id,t.task_title FROM tasks t JOIN mentors m ON m.id=t.mentor_id WHERE t.id=? AND t.intern_profile_id=? FOR UPDATE")){
                    p.setLong(1,taskId);p.setLong(2,profileId);try(ResultSet r=p.executeQuery()){if(!r.next())throw new IllegalArgumentException("Công việc không tồn tại hoặc không thuộc về bạn.");mentorUserId=r.getLong(1);title=r.getString(2);}
                }
                try(PreparedStatement p=c.prepareStatement("UPDATE tasks SET completion_percent=?,status=?,intern_note=?,progress_updated_at=CURRENT_TIMESTAMP WHERE id=? AND intern_profile_id=?")){
                    p.setInt(1,percent);p.setString(2,status);p.setString(3,note);p.setLong(4,taskId);p.setLong(5,profileId);p.executeUpdate();
                }
                try(PreparedStatement p=c.prepareStatement("INSERT INTO notifications(user_id,title,message,target_url) VALUES(?,?,?,?)")){
                    p.setLong(1,mentorUserId);p.setString(2,"Thực tập sinh cập nhật tiến độ");p.setString(3,title+" đã được cập nhật lên "+percent+"%.");p.setString(4,"/mentor/dashboard");p.executeUpdate();
                }
                c.commit();
            }catch(SQLException|RuntimeException e){c.rollback();throw e;}finally{c.setAutoCommit(true);}
        }
    }

    public List<Map<String,Object>> reports(long profileId) throws SQLException {
        String sql="SELECT id,week_number,start_date,end_date,completed_work,key_learnings,obstacles,attachment_url,mentor_score,mentor_feedback,status FROM weekly_reports WHERE intern_profile_id=? ORDER BY week_number DESC,id DESC";
        try(Connection c=DBConnection.getConnection();PreparedStatement p=c.prepareStatement(sql)){p.setLong(1,profileId);return rows(p);}
    }

    public void saveWeeklyReport(long profileId,int week,LocalDate start,LocalDate end,String completed,String learnings,String obstacles,String attachment) throws SQLException {
        try(Connection c=DBConnection.getConnection()){
            c.setAutoCommit(false);
            try{
                Long existing=null;String currentStatus=null;long mentorUserId=0;
                try(PreparedStatement p=c.prepareStatement("SELECT wr.id,wr.status,m.user_id mentor_user_id FROM intern_profiles ip JOIN mentors m ON m.id=ip.mentor_id LEFT JOIN weekly_reports wr ON wr.intern_profile_id=ip.id AND wr.week_number=? WHERE ip.id=? FOR UPDATE")){
                    p.setInt(1,week);p.setLong(2,profileId);try(ResultSet r=p.executeQuery()){if(!r.next())throw new IllegalArgumentException("Hồ sơ thực tập chưa được phân công mentor.");mentorUserId=r.getLong("mentor_user_id");Number reportId=(Number)r.getObject("id");existing=reportId==null?null:reportId.longValue();currentStatus=r.getString("status");}
                }
                if("APPROVED".equals(currentStatus))throw new IllegalArgumentException("Báo cáo tuần này đã được duyệt nên không thể sửa.");
                if(existing==null){try(PreparedStatement p=c.prepareStatement("INSERT INTO weekly_reports(intern_profile_id,week_number,start_date,end_date,completed_work,key_learnings,obstacles,attachment_url,status) VALUES(?,?,?,?,?,?,?,?,'SUBMITTED')")){bindReport(p,profileId,week,start,end,completed,learnings,obstacles,attachment);p.executeUpdate();}}
                else{try(PreparedStatement p=c.prepareStatement("UPDATE weekly_reports SET start_date=?,end_date=?,completed_work=?,key_learnings=?,obstacles=?,attachment_url=?,status='SUBMITTED',mentor_feedback=NULL,mentor_score=NULL WHERE id=?")){p.setDate(1,java.sql.Date.valueOf(start));p.setDate(2,java.sql.Date.valueOf(end));p.setString(3,completed);p.setString(4,learnings);p.setString(5,obstacles);p.setString(6,attachment);p.setLong(7,existing);p.executeUpdate();}}
                try(PreparedStatement p=c.prepareStatement("INSERT INTO notifications(user_id,title,message,target_url) VALUES(?,?,?,?)")){p.setLong(1,mentorUserId);p.setString(2,"Báo cáo tuần mới");p.setString(3,"Thực tập sinh đã nộp báo cáo tuần "+week+".");p.setString(4,"/mentor/reports");p.executeUpdate();}
                c.commit();
            }catch(SQLException|RuntimeException e){c.rollback();throw e;}finally{c.setAutoCommit(true);}
        }
    }

    private void bindReport(PreparedStatement p,long profileId,int week,LocalDate start,LocalDate end,String completed,String learnings,String obstacles,String attachment)throws SQLException{p.setLong(1,profileId);p.setInt(2,week);p.setDate(3,java.sql.Date.valueOf(start));p.setDate(4,java.sql.Date.valueOf(end));p.setString(5,completed);p.setString(6,learnings);p.setString(7,obstacles);p.setString(8,attachment);}
    private List<Map<String,Object>> rows(PreparedStatement p)throws SQLException{List<Map<String,Object>> out=new ArrayList<>();try(ResultSet r=p.executeQuery()){ResultSetMetaData m=r.getMetaData();while(r.next()){Map<String,Object>x=new LinkedHashMap<>();for(int i=1;i<=m.getColumnCount();i++)x.put(m.getColumnLabel(i),r.getObject(i));out.add(x);}}return out;}
}
