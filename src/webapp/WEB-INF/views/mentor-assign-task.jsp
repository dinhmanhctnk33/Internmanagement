<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title>Giao nhiệm vụ cho thực tập sinh</title>
    <style>
        body { font-family: Arial, sans-serif; margin: 30px; background-color: #f4f6f8; }
        .container { max-width: 650px; background: #fff; padding: 25px; border-radius: 8px; box-shadow: 0 2px 8px rgba(0,0,0,0.1); margin: auto; }
        h2 { margin-top: 0; color: #333; }
        .form-group { margin-bottom: 18px; }
        label { display: block; font-weight: bold; margin-bottom: 6px; color: #444; }
        input[type="text"], input[type="date"], textarea {
            width: 100%; padding: 10px; border: 1px solid #ccc; border-radius: 4px; box-sizing: border-box;
        }
        .error-box { background-color: #ffebe8; border: 1px solid #dd4b39; color: #d93025; padding: 12px; border-radius: 4px; margin-bottom: 20px; }
        .success-box { background-color: #e8f5e9; border: 1px solid #4caf50; color: #256029; padding: 12px; border-radius: 4px; margin-bottom: 20px; }
        .intern-list { max-height: 180px; overflow-y: auto; border: 1px solid #ddd; padding: 10px; border-radius: 4px; background: #fafafa; }
        .intern-item { margin-bottom: 8px; display: flex; align-items: center; gap: 8px; }
        .btn-submit { background-color: #007bff; color: white; border: none; padding: 10px 20px; border-radius: 4px; cursor: pointer; font-size: 15px; }
        .btn-submit:hover { background-color: #0056b3; }
        .field-error { color: #d93025; font-size: 13px; margin-top: 4px; }
    </style>
</head>
<body>

<div class="container">
    <h2>Giao nhiệm vụ cho thực tập sinh</h2>

    <c:if test="${not empty errorMessage}">
        <div class="error-box">
            ${errorMessage}
        </div>
    </c:if>
    <c:if test="${not empty successMessage}">
        <div class="success-box">
            ${successMessage}
        </div>
    </c:if>

    <c:choose>
        <c:when test="${empty internList}">
            <div class="error-box">
                Hiện tại bạn chưa được HR phân công thực tập sinh nào để giao nhiệm vụ. Vui lòng liên hệ HR để được phân công trước.
            </div>
        </c:when>
        <c:otherwise>
            <form action="${pageContext.request.contextPath}/mentor/tasks/assign" method="POST" id="assignTaskForm">
                <div class="form-group">
                    <label for="title">Tiêu đề nhiệm vụ <span style="color:red;">*</span></label>
                    <input type="text" id="title" name="title" value="${param.title}" maxlength="200" required placeholder="Nhập tiêu đề nhiệm vụ...">
                    <c:if test="${not empty titleError}">
                        <div class="field-error">${titleError}</div>
                    </c:if>
                </div>

                <div class="form-group">
                    <label for="description">Mô tả công việc</label>
                    <textarea id="description" name="description" rows="4" maxlength="2000" placeholder="Chi tiết yêu cầu, tài liệu tham khảo...">${param.description}</textarea>
                    <c:if test="${not empty descriptionError}">
                        <div class="field-error">${descriptionError}</div>
                    </c:if>
                </div>

                <div class="form-group">
                    <label for="dueDate">Hạn hoàn thành</label>
                    <input type="date" id="dueDate" name="dueDate" min="${todayDate}" value="${param.dueDate}">
                    <c:if test="${not empty dueDateError}">
                        <div class="field-error">${dueDateError}</div>
                    </c:if>
                </div>

                <div class="form-group">
                    <label>Người nhận <span style="color:red;">*</span></label>
                    <div style="margin-bottom: 8px;">
                        <input type="checkbox" id="selectAll" onclick="toggleSelectAll(this)">
                        <label for="selectAll" style="display:inline; font-weight: normal; cursor:pointer;"><strong>Chọn tất cả</strong></label>
                    </div>
                    <div class="intern-list">
                        <c:forEach var="intern" items="${internList}">
                            <div class="intern-item">
                                <input type="checkbox" class="intern-checkbox" name="internIds" value="${intern.id}" id="intern_${intern.id}"
                                    <c:if test="${selectedInterns != null && selectedInterns.contains(intern.id)}">checked</c:if>>
                                <label for="intern_${intern.id}" style="font-weight: normal; margin-bottom: 0; cursor:pointer;">
                                    ${intern.fullName} (${intern.email})
                                </label>
                            </div>
                        </c:forEach>
                    </div>
                    <c:if test="${not empty internIdsError}">
                        <div class="field-error">${internIdsError}</div>
                    </c:if>
                </div>

                <div style="text-align: right; margin-top: 20px;">
                    <button type="submit" class="btn-submit">Tạo và giao nhiệm vụ</button>
                </div>
            </form>
        </c:otherwise>
    </c:choose>
</div>

<script>
    function toggleSelectAll(source) {
        const checkboxes = document.querySelectorAll('.intern-checkbox');
        checkboxes.forEach(cb => cb.checked = source.checked);
    }
</script>
</body>
</html>