package com.example.internmanagement.controller;

import com.example.internmanagement.dao.TaskProgressDAO;
import com.example.internmanagement.model.TaskProgress;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

@WebServlet(name = "TaskProgressServlet", urlPatterns = {"/intern/task-progress"})
public class TaskProgressServlet extends HttpServlet {

    private TaskProgressDAO taskProgressDAO;

    @Override
    public void init() {
        taskProgressDAO = new TaskProgressDAO();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        int internId = 1;
        List<TaskProgress> progressList = taskProgressDAO.getProgressByInternId(internId);
        
        request.setAttribute("progressList", progressList);
        request.getRequestDispatcher("/task-progress.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");

        int internId = Integer.parseInt(request.getParameter("internId"));
        int taskId = Integer.parseInt(request.getParameter("taskId"));
        String taskName = request.getParameter("taskName");
        int progressPercent = Integer.parseInt(request.getParameter("progressPercent"));
        String status = request.getParameter("status");
        String note = request.getParameter("note");

        TaskProgress progress = new TaskProgress(0, internId, taskId, taskName, progressPercent, status, note, null);
        taskProgressDAO.saveOrUpdateProgress(progress);

        response.sendRedirect(request.getContextPath() + "/intern/task-progress");
    }
}