package com.example.internmanagement.util;

import java.sql.*;

public class MeetingEmailProcessor {
    public int processDue() throws Exception {
        if (!EmailUtility.isConfigured()) return 0;
        int sent=0;
        try(Connection c=DBConnection.getConnection();PreparedStatement q=c.prepareStatement("SELECT q.*,m.title,m.meeting_date,m.start_time,m.end_time,m.meeting_mode,m.location_or_link,m.description FROM meeting_email_queue q JOIN meetings m ON m.id=q.meeting_id WHERE q.status IN ('PENDING','FAILED') AND q.next_attempt_at<=NOW() ORDER BY q.next_attempt_at,q.id LIMIT 50");ResultSet r=q.executeQuery()){
            while(r.next()){long id=r.getLong("id");if(!claim(id))continue;try{String email=r.getString("recipient_email");if(email==null||email.isBlank()||!email.contains("@"))throw new IllegalArgumentException("Email người nhận không hợp lệ.");EmailUtility.sendMeetingEmail(email,r.getString("recipient_name"),r.getString("email_type"),r.getString("title"),r.getDate("meeting_date").toString(),r.getTime("start_time").toString().substring(0,5),r.getTime("end_time").toString().substring(0,5),r.getString("meeting_mode"),r.getString("location_or_link"),r.getString("description"));markSent(id,r.getLong("meeting_id"),r.getLong("intern_profile_id"),r.getString("email_type"));sent++;}catch(Exception e){markFailed(id,e.getMessage());}}
        }return sent;
    }
    private boolean claim(long id)throws SQLException{try(Connection c=DBConnection.getConnection();PreparedStatement s=c.prepareStatement("UPDATE meeting_email_queue SET status='PROCESSING' WHERE id=? AND status IN ('PENDING','FAILED')")){s.setLong(1,id);return s.executeUpdate()==1;}}
    private void markSent(long id,long meeting,long intern,String type)throws SQLException{try(Connection c=DBConnection.getConnection()){c.setAutoCommit(false);try(PreparedStatement s=c.prepareStatement("UPDATE meeting_email_queue SET status='SENT',sent_at=NOW(),last_error=NULL WHERE id=?")){s.setLong(1,id);s.executeUpdate();}try(PreparedStatement s=c.prepareStatement("UPDATE meeting_recipients SET last_email_status='SENT',last_email_type=?,last_email_at=NOW() WHERE meeting_id=? AND intern_profile_id=?")){s.setString(1,type);s.setLong(2,meeting);s.setLong(3,intern);s.executeUpdate();}c.commit();}}
    private void markFailed(long id,String error)throws SQLException{try(Connection c=DBConnection.getConnection();PreparedStatement s=c.prepareStatement("UPDATE meeting_email_queue SET status=IF(attempt_count+1>=3,'FAILED_FINAL','FAILED'),attempt_count=attempt_count+1,last_error=?,next_attempt_at=DATE_ADD(NOW(),INTERVAL 5 MINUTE) WHERE id=?")){s.setString(1,error==null?"Lỗi không xác định":error.substring(0,Math.min(1000,error.length())));s.setLong(2,id);s.executeUpdate();}try(Connection c=DBConnection.getConnection();PreparedStatement s=c.prepareStatement("UPDATE meeting_recipients mr JOIN meeting_email_queue q ON q.meeting_id=mr.meeting_id AND q.intern_profile_id=mr.intern_profile_id SET mr.last_email_status=q.status WHERE q.id=?")){s.setLong(1,id);s.executeUpdate();}}
}
