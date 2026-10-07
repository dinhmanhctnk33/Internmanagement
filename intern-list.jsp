<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
\<!DOCTYPE html>
\<html lang="vi">
\<head>
    \<meta charset="UTF-8">
    \<meta name="viewport" content="width=device-width, initial-scale=1.0">
    \<title>Danh sách Thực tập sinh - HR Portal\</title>
    \<link rel="stylesheet" href="${pageContext.request.contextPath}/frontend/css/dashboard.css?v=20261004-1">
    \<link rel="stylesheet" href="${pageContext.request.contextPath}/frontend/css/style.css">
    \<link rel="stylesheet" href="[https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.2/css/all.min.css](https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.2/css/all.min.css)">
\</head>
\<body>
\<div class="layout">

    \<!-- SIDEBAR HR -->
    \<aside class="sidebar">
        \<div class="logo">
            \<div class="logo-icon">\<i class="fa-solid fa-user-tie">\</i>\</div>
            \<div>\<h2>IMS Portal\</h2>\<span>Phòng Nhân sự (HR)\</span>\</div>
        \</div>
        \<nav class="menu">
            \<p class="menu-title">NGHIỆP VỤ HR\</p>
            \<a href="${pageContext.request.contextPath}/home" class="menu-item">\<i class="fa-solid fa-chart-line">\</i>\<span>Dashboard HR\</span>\</a>
            \<a href="${pageContext.request.contextPath}/interns" class="menu-item active">\<i class="fa-solid fa-users">\</i>\<span>Thực tập sinh & Lọc\</span>\</a>
            \<a href="${pageContext.request.contextPath}/interns/new" class="menu-item">\<i class="fa-solid fa-user-plus">\</i>\<span>Thêm hồ sơ TTS\</span>\</a>
            \<a href="${pageContext.request.contextPath}/documents/review" class="menu-item">\<i class="fa-solid fa-file-circle-check">\</i>\<span>Duyệt tài liệu & CV\</span>\</a>
        \</nav>
        \<div class="sidebar-bottom">
            \<a href="${pageContext.request.contextPath}/logout" class="menu-item logout">
                \<i class="fa-solid fa-right-from-bracket">\</i>\<span>Đăng xuất\</span>
            \</a>
        \</div>
    \</aside>

    \<!-- MAIN -->
    \<main class="main">
        \<header class="header">
            \<form action="${pageContext.request.contextPath}/interns" method="get" style="display\:flex;align-items\:center;gap:10px;flex:1;">
                \<div class="search-box" style="flex:1;">
                    \<i class="fa-solid fa-magnifying-glass">\</i>
                    \<input type="text" name="q" value="${param.q}" placeholder="Tìm tên, mã TTS, email, trường, ngành...">
                \</div>
                \<select name="university" aria-label="Lọc theo trường đại học" style="width:250px;flex:0 0 250px;padding:9px 12px;border:1px solid var(--border-color);border-radius:8px;font-size:13px;background:#fff;color\:var(--primary-dark);">
                    \<c\:if test="${false}">
                    \<option value="">-- Tất cả trường --\</option>
                    \<option value="ĐH Bách Khoa" ${param.university == 'ĐH Bách Khoa' ? 'selected' : ''}>ĐH Bách Khoa\</option>
                    \<option value="ĐH Công Nghệ" ${param.university == 'ĐH Công Nghệ' ? 'selected' : ''}>ĐH Công Nghệ\</option>
                    \<option value="ĐH FPT" ${param.university == 'ĐH FPT' ? 'selected' : ''}>ĐH FPT\</option>
                    \<option value="ĐH Kinh Tế Quốc Dân" ${param.university == 'ĐH Kinh Tế Quốc Dân' ? 'selected' : ''}>ĐH Kinh Tế Quốc Dân\</option>
                    \</c\:if>
                    \<option value="">-- Tất cả trường --\</option>
                    \<c\:forEach var="school" items="${universities}">
                        \<option value="${school}" ${param.university == school ? 'selected' : ''}>${school}\</option>
                    \</c\:forEach>
                \</select>
                \<button type="submit" class="primary-btn" style="padding:9px 18px;">\<i class="fa-solid fa-filter">\</i> Lọc\</button>
            \</form>
            \<div class="header-right">
                \<div class="user">
                    \<div class="avatar">${currentUser.shortName}\</div>
                    \<div class="user-info">\<strong>${currentUser.fullName}\</strong>\<small>HR Manager\</small>\</div>
                    \<i class="fa-solid fa-chevron-down">\</i>
                \</div>
            \</div>
        \</header>

        \<section class="content">
            \<div class="page-title">
                \<div>
                    \<h1>\<i class="fa-solid fa-users" style="color\:var(--action-accent);margin-right:10px;">\</i>Danh sách Thực tập sinh\</h1>
                    \<p>Tìm kiếm, lọc và quản lý hồ sơ thực tập sinh theo trường / ngành\</p>
                \</div>
                \<a href="${pageContext.request.contextPath}/interns/new" class="primary-btn">
                    \<i class="fa-solid fa-plus">\</i> Thêm hồ sơ mới
                \</a>
            \</div>

            \<c\:if test="${param.success == '1'}">
                \<div style="background:#F0FFF4;color\:var(--success-badge);border:1px solid #C6F6D5;padding:12px 16px;border-radius:8px;margin-bottom:20px;font-weight:600;">
                    \<i class="fa-solid fa-circle-check">\</i> Lưu hồ sơ thực tập sinh thành công!
                \</div>
            \</c\:if>
            \<c\:if test="${not empty importResult}">
                \<div style="background:#F0FFF4;color\:var(--success-badge);border:1px solid #C6F6D5;padding:12px 16px;border-radius:8px;margin-bottom:20px;font-weight:600;">
                    \<i class="fa-solid fa-circle-check">\</i> \<c\:out value="${importResult}"/>
                \</div>
                \<c\:if test="${not empty importDetails}">
                    \<ul style="color\:var(--danger-badge);margin:0 0 20px;padding-left:24px;">
                        \<c\:forEach var="detail" items="${importDetails}">\<li>\<c\:out value="${detail}"/>\</li>\</c\:forEach>
                    \</ul>
                \</c\:if>
            \</c\:if>

            \<div class="card">
                \<div class="table-container">
                    \<table>
                        \<thead>
                            \<tr>
                                \<th>Thực tập sinh\</th>
                                \<th>Mã TTS\</th>
                                \<th>Trường / Ngành\</th>
                                \<th>Mentor\</th>
                                \<th>Trạng thái\</th>
                                \<th>Thao tác\</th>
                            \</tr>
                        \</thead>
                        \<tbody>
                            \<c\:choose>
                                \<c\:when test="${not empty interns}">
                                    \<c\:forEach var="item" items="${interns}">
                                        \<tr>
                                            \<td>
                                                \<div class="person">
                                                    \<div class="person-avatar">
                                                        \<c\:choose>
                                                            \<c\:when test="${not empty item.userFullName}">${item.userFullName.substring(0,1)}\</c\:when>
                                                            \<c\:otherwise>T\</c\:otherwise>
                                                        \</c\:choose>
                                                    \</div>
                                                    \<div>
                                                        \<strong>${item.userFullName}\</strong>
                                                        \<small>${item.userEmail}\</small>
                                                    \</div>
                                                \</div>
                                            \</td>
                                            \<td>\<strong>${item.internCode}\</strong>\</td>
                                            \<td>${item.universityName}\<br>\<small style="color\:var(--text-muted);">${item.majorName}\</small>\</td>
                                            \<td>${not empty item.mentorName ? item.mentorName : 'Chưa phân công'}\</td>
                                            \<td>
                                                \<c\:choose>
                                                    \<c\:when test="${item.internshipStatus == 'ACTIVE'}">\<span class="badge active">\<i class="fa-solid fa-check">\</i> Đang TT\</span>\</c\:when>
                                                    \<c\:when test="${item.internshipStatus == 'COMPLETED'}">\<span class="badge active">Hoàn thành\</span>\</c\:when>
                                                    \<c\:when test="${item.internshipStatus == 'PAUSED'}">\<span class="badge pending">Tạm dừng\</span>\</c\:when>
                                                    \<c\:otherwise>\<span class="badge inactive">${item.internshipStatus}\</span>\</c\:otherwise>
                                                \</c\:choose>
                                            \</td>
                                            \<td>
                                                \<div style="display\:flex;gap:12px;align-items\:center;white-space\:nowrap;">
                                                \<a href="${pageContext.request.contextPath}/interns/profile?id=${item.id}" style="color\:var(--action-accent);font-weight:600;text-decoration\:none;">
                                                    \<i class="fa-solid fa-eye">\</i> Xem
                                                \</a>
                                                \<a href="${pageContext.request.contextPath}/interns/edit?id=${item.id}" style="color\:var(--warning-badge);font-weight:600;text-decoration\:none;">
                                                    \<i class="fa-solid fa-pen-to-square">\</i> Sửa
                                                \</a>
                                                \</div>
                                            \</td>
                                        \</tr>
                                    \</c\:forEach>
                                \</c\:when>
                                \<c\:otherwise>
                                    \<tr>
                                        \<td colspan="6" style="text-align\:center;padding:40px;color\:var(--text-muted);">
                                            \<i class="fa-solid fa-users" style="font-size:2rem;margin-bottom:10px;display\:block;opacity:0.3;">\</i>
                                            Không tìm thấy thực tập sinh nào.\<br>
                                            \<a href="${pageContext.request.contextPath}/interns/new" style="color\:var(--action-accent);font-weight:600;">+ Thêm hồ sơ mới\</a>
                                        \</td>
                                    \</tr>
                                \</c\:otherwise>
                            \</c\:choose>
                        \</tbody>
                    \</table>
                \</div>
            \</div>
        \</section>
    \</main>
\</div>
\</body>
\</html>