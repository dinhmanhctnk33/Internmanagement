package com.example.internmanagement.controller;

import com.example.internmanagement.model.User;
import com.example.internmanagement.util.DBConnection;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.sql.*;
import java.util.*;

@WebServlet("/my-application")
public class CandidatePortalServlet extends HttpServlet {
    @Override protected void doGet(HttpServletRequest req,HttpServletResponse resp)throws ServletException,IOException{
        User user=(User)req.getSession().getAttribute("user");if(user==null||!"CANDIDATE".equalsIgnoreCase(user.getRole())){resp.sendError(403);return;}
        try(Connection c=DBConnection.getConnection();PreparedStatement s=c.prepareStatement("SELECT full_name,email,phone,university_name,major_name,desired_position,cv_file_url,application_status,hr_notes,applied_at FROM candidates WHERE LOWER(email)=LOWER(?) ORDER BY id DESC LIMIT 1")){
            s.setString(1,user.getEmail());try(ResultSet rs=s.executeQuery()){if(rs.next()){Map<String,Object> row=new LinkedHashMap<>();for(String col:List.of("full_name","email","phone","university_name","major_name","desired_position","cv_file_url","application_status","hr_notes","applied_at"))row.put(col,rs.getObject(col));req.setAttribute("application",row);}}
            req.getRequestDispatcher("/WEB-INF/views/candidate-status.jsp").forward(req,resp);
        }catch(SQLException e){throw new ServletException("Không thể tải hồ sơ ứng tuyển.",e);}
    }
}
