package com.example.internmanagement.controller;

import com.example.internmanagement.model.User;
import com.example.internmanagement.util.DBConnection;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.*;
import java.nio.file.*;
import java.sql.*;
import java.util.*;
import java.time.LocalDate;

@WebServlet({"/contracts","/contract-files/*"})
@MultipartConfig(maxFileSize=10*1024*1024,maxRequestSize=11*1024*1024)
public class ContractServlet extends HttpServlet {
    @Override protected void doGet(HttpServletRequest req,HttpServletResponse resp)throws ServletException,IOException{
        if(req.getServletPath().startsWith("/contract-files")){serveFile(req,resp);return;}
        try{loadPage(req);req.getRequestDispatcher("/WEB-INF/views/contract-management.jsp").forward(req,resp);}catch(SQLException e){throw new ServletException("Không thể tải danh sách hợp đồng.",e);}
    }
    @Override protected void doPost(HttpServletRequest req,HttpServletResponse resp)throws ServletException,IOException{
        req.setCharacterEncoding("UTF-8");Path stored=null;
        try{
            int profileId=Integer.parseInt(req.getParameter("profileId"));String number=required(req,"contractNumber");
            LocalDate from=LocalDate.parse(required(req,"effectiveDate")),to=LocalDate.parse(required(req,"expirationDate"));if(to.isBefore(from))throw new IllegalArgumentException("Ngày kết thúc phải sau ngày bắt đầu.");
            Part file=req.getPart("contractFile");if(file==null||file.getSize()==0)throw new IllegalArgumentException("Vui lòng chọn hợp đồng PDF.");
            String original=Paths.get(file.getSubmittedFileName()).getFileName().toString();if(!original.toLowerCase().endsWith(".pdf"))throw new IllegalArgumentException("Hợp đồng phải là tệp PDF.");
            Path dir=uploadDir();Files.createDirectories(dir);stored=dir.resolve(UUID.randomUUID()+".pdf");try(InputStream in=file.getInputStream()){Files.copy(in,stored,StandardCopyOption.REPLACE_EXISTING);}
            try(Connection c=DBConnection.getConnection();PreparedStatement s=c.prepareStatement("INSERT INTO contracts(contract_number,intern_profile_id,effective_date,expiration_date,contract_pdf_url,signature_status) VALUES(?,?,?,?,?,'SENT')")){
                s.setString(1,number);s.setInt(2,profileId);s.setDate(3,java.sql.Date.valueOf(from));s.setDate(4,java.sql.Date.valueOf(to));s.setString(5,"/contract-files/"+stored.getFileName());s.executeUpdate();
            }resp.sendRedirect(req.getContextPath()+"/contracts?uploaded=1");
        }catch(Exception e){if(stored!=null)Files.deleteIfExists(stored);req.setAttribute("error",e instanceof SQLIntegrityConstraintViolationException?"Số hợp đồng đã tồn tại.":e.getMessage());try{loadPage(req);}catch(SQLException ignored){}req.getRequestDispatcher("/WEB-INF/views/contract-management.jsp").forward(req,resp);}
    }
    private List<Map<String,Object>> contracts()throws SQLException{return query("SELECT c.id,c.contract_number,c.effective_date,c.expiration_date,CONCAT('/contract-files/',SUBSTRING_INDEX(c.contract_pdf_url,'/',-1)) AS contract_pdf_url,c.signature_status,u.full_name,ip.intern_code,p.program_name FROM contracts c JOIN intern_profiles ip ON ip.id=c.intern_profile_id JOIN users u ON u.id=ip.user_id LEFT JOIN internship_programs p ON p.id=ip.program_id ORDER BY c.id DESC");}
    private List<Map<String,Object>> interns()throws SQLException{return query("SELECT ip.id,ip.intern_code,u.full_name,p.program_name FROM intern_profiles ip JOIN users u ON u.id=ip.user_id LEFT JOIN internship_programs p ON p.id=ip.program_id WHERE ip.internship_status='ACTIVE' ORDER BY u.full_name");}
    private void loadPage(HttpServletRequest req)throws SQLException{List<Map<String,Object>> contractRows=contracts();List<Map<String,Object>> internRows=interns();int pending=0,signed=0;for(Map<String,Object> row:contractRows){if("SIGNED".equals(row.get("signature_status")))signed++;else pending++;}req.setAttribute("contracts",contractRows);req.setAttribute("interns",internRows);req.setAttribute("totalInterns",internRows.size());req.setAttribute("pendingContracts",pending);req.setAttribute("signedContracts",signed);}
    private List<Map<String,Object>> query(String sql)throws SQLException{List<Map<String,Object>> rows=new ArrayList<>();try(Connection c=DBConnection.getConnection();PreparedStatement s=c.prepareStatement(sql);ResultSet rs=s.executeQuery()){int n=rs.getMetaData().getColumnCount();while(rs.next()){Map<String,Object> row=new LinkedHashMap<>();for(int i=1;i<=n;i++)row.put(rs.getMetaData().getColumnLabel(i),rs.getObject(i));rows.add(row);}}return rows;}
    private void serveFile(HttpServletRequest req,HttpServletResponse resp)throws IOException,ServletException{String info=req.getPathInfo();if(info==null||!info.matches("/[A-Za-z0-9._-]+\\.pdf")){resp.sendError(404);return;}String name=info.substring(1);if(!canViewContract(req,name)){resp.sendError(HttpServletResponse.SC_FORBIDDEN);return;}Path dir=uploadDir();Path file=dir.resolve(name).normalize();if(!file.startsWith(dir)||!Files.isRegularFile(file)){Path legacy=Paths.get(System.getProperty("catalina.base",System.getProperty("java.io.tmpdir")),"temp","ims-uploads","contracts");file=legacy.resolve(name).normalize();if(!file.startsWith(legacy)||!Files.isRegularFile(file)){resp.sendError(404);return;}}resp.setContentType("application/pdf");resp.setHeader("Content-Disposition","inline; filename=contract.pdf");resp.setContentLengthLong(Files.size(file));Files.copy(file,resp.getOutputStream());}
    private boolean canViewContract(HttpServletRequest req,String name)throws ServletException{String role=String.valueOf(req.getSession().getAttribute("userRole"));if("HR".equalsIgnoreCase(role)||"ADMIN".equalsIgnoreCase(role))return true;User user=(User)req.getSession().getAttribute("user");if(user==null||!"INTERN".equalsIgnoreCase(role))return false;String sql="SELECT 1 FROM contracts c JOIN intern_profiles ip ON ip.id=c.intern_profile_id WHERE ip.user_id=? AND SUBSTRING_INDEX(c.contract_pdf_url,'/',-1)=?";try(Connection c=DBConnection.getConnection();PreparedStatement s=c.prepareStatement(sql)){s.setInt(1,user.getId());s.setString(2,name);try(ResultSet rs=s.executeQuery()){return rs.next();}}catch(SQLException e){throw new ServletException("Không thể kiểm tra quyền xem hợp đồng.",e);}}
    private Path uploadDir(){return Paths.get(System.getProperty("catalina.base",System.getProperty("java.io.tmpdir")),"contract-uploads");}
    private String required(HttpServletRequest r,String n){String v=r.getParameter(n);if(v==null||v.isBlank())throw new IllegalArgumentException("Vui lòng nhập đầy đủ thông tin.");return v.trim();}
}
