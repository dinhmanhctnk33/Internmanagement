package com.example.internmanagement.filter;
import com.example.internmanagement.model.User;
import com.example.internmanagement.util.DBConnection;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter; 
import jakarta.servlet.http.*; 
import java.io.*; 
import java.sql.*; 
import java.util.*;
@WebFilter(urlPatterns={"/interns/new","/interns/edit","/interns/import","/documents/review","/review-emails","/admin/*"})
public class AuthorizationFilter implements Filter {
 public void doFilter(ServletRequest request,ServletResponse response,FilterChain chain)throws IOException,ServletException {
    HttpServletRequest req=(HttpServletRequest)request;HttpServletResponse res=(HttpServletResponse)response;
    User u=(User)req.getSession().getAttribute("user");
    String path=req.getServletPath();
    Set<String> allowed=path.startsWith("/admin/")?Set.of("ADMIN"):path.startsWith("/documents/review")?Set.of("HR","ADMIN"):Set.of("HR","ADMIN");try{if(!roles(u.getId()).stream().anyMatch(allowed::contains)){res.sendError(HttpServletResponse.SC_FORBIDDEN,"Bạn không có quyền sử dụng chức năng này.");
    return;
}
}catch(SQLException e){
    throw new ServletException("Không thể kiểm tra quyền",e);
}chain.doFilter(request,response);
}
 private Set<String> roles(int userId)throws SQLException{
    Set<String> result=new HashSet<>();
    try(Connection c=DBConnection.getConnection();
    PreparedStatement p=c.prepareStatement("SELECT r.role_code FROM roles r JOIN user_roles ur ON ur.role_id=r.id WHERE ur.user_id=?")){p.setInt(1,userId);try(ResultSet r=p.executeQuery()){
        while(r.next())result.add(r.getString(1));
    }
}
return result;
}
}
