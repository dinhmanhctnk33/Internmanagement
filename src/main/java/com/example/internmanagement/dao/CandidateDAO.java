package com.example.internmanagement.dao;

import com.example.internmanagement.util.DBConnection;
import java.sql.*;
import java.util.*;

public class CandidateDAO {
    public List<Map<String,Object>> findAll(String status,String keyword)throws SQLException{
        StringBuilder sql=new StringBuilder("SELECT id,full_name,email,phone,university_name,major_name,desired_position,application_status,hr_notes,applied_at FROM candidates WHERE 1=1");
        List<Object> params=new ArrayList<>();
        if(status!=null&&!status.isBlank()){sql.append(" AND application_status=?");params.add(status);}
        if(keyword!=null&&!keyword.isBlank()){sql.append(" AND (full_name LIKE ? OR email LIKE ?)");String q="%"+keyword.trim()+"%";params.add(q);params.add(q);}
        sql.append(" ORDER BY applied_at DESC,id DESC");
        List<Map<String,Object>> rows=new ArrayList<>();
        try(Connection c=DBConnection.getConnection();PreparedStatement s=c.prepareStatement(sql.toString())){
            for(int i=0;i<params.size();i++)s.setObject(i+1,params.get(i));
            try(ResultSet rs=s.executeQuery()){while(rs.next()){Map<String,Object> r=new LinkedHashMap<>();for(String col:List.of("id","full_name","email","phone","university_name","major_name","desired_position","application_status","hr_notes","applied_at"))r.put(col,rs.getObject(col));rows.add(r);}}
        }return rows;
    }

    public boolean decide(Connection c,int candidateId,String status,String reason)throws SQLException{
        String sql="UPDATE candidates SET application_status=?,hr_notes=? WHERE id=? AND application_status IN ('NEW','INTERVIEWING')";
        try(PreparedStatement s=c.prepareStatement(sql)){s.setString(1,status);s.setString(2,reason);s.setInt(3,candidateId);return s.executeUpdate()==1;}
    }
}
