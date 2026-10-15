package com.example.internmanagement.controller;

import com.example.internmanagement.dao.WorkScheduleDAO;
import com.example.internmanagement.model.WorkSchedule;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

@WebServlet(name = "WorkScheduleServlet", urlPatterns = {"/work-schedule"})
public class WorkScheduleServlet extends HttpServlet {

    private WorkScheduleDAO workScheduleDAO;

    @Override
    public void init() {
        workScheduleDAO = new WorkScheduleDAO();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        String action = request.getParameter("action");
        if (action == null) {
            action = "list";
        }

        switch (action) {
            case "edit":
                showEditForm(request, response);
                break;
            case "delete":
                deleteSchedule(request, response);
                break;
            default:
                listSchedules(request, response);
                break;
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        request.setCharacterEncoding("UTF-8");
        String action = request.getParameter("action");

        if ("add".equals(action)) {
            addSchedule(request, response);
        } else if ("update".equals(action)) {
            updateSchedule(request, response);
        } else {
            listSchedules(request, response);
        }
    }

    private void listSchedules(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        List<WorkSchedule> scheduleList = workScheduleDAO.getAllSchedules();
        request.setAttribute("schedules", scheduleList);
        request.getRequestDispatcher("/work-schedule-list.jsp").forward(request, response);
    }

    private void showEditForm(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        int id = Integer.parseInt(request.getParameter("id"));
        WorkSchedule existingSchedule = workScheduleDAO.getScheduleById(id);
        request.setAttribute("schedule", existingSchedule);
        request.getRequestDispatcher("/work-schedule-form.jsp").forward(request, response);
    }

    private void addSchedule(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        int groupId = Integer.parseInt(request.getParameter("groupId"));
        String shiftName = request.getParameter("shiftName");
        String startTime = request.getParameter("startTime");
        String endTime = request.getParameter("endTime");
        String workDays = request.getParameter("workDays");
        String note = request.getParameter("note");

        WorkSchedule newSchedule = new WorkSchedule(0, groupId, null, shiftName, startTime, endTime, workDays, note);
        workScheduleDAO.insertSchedule(newSchedule);
        response.sendRedirect(request.getContextPath() + "/work-schedule");
    }

    private void updateSchedule(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        int id = Integer.parseInt(request.getParameter("id"));
        int groupId = Integer.parseInt(request.getParameter("groupId"));
        String shiftName = request.getParameter("shiftName");
        String startTime = request.getParameter("startTime");
        String endTime = request.getParameter("endTime");
        String workDays = request.getParameter("workDays");
        String note = request.getParameter("note");

        WorkSchedule schedule = new WorkSchedule(id, groupId, null, shiftName, startTime, endTime, workDays, note);
        workScheduleDAO.updateSchedule(schedule);
        response.sendRedirect(request.getContextPath() + "/work-schedule");
    }

    private void deleteSchedule(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        int id = Integer.parseInt(request.getParameter("id"));
        workScheduleDAO.deleteSchedule(id);
        response.sendRedirect(request.getContextPath() + "/work-schedule");
    }
}