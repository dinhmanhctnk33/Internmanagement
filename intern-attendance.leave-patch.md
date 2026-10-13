# Chỉnh `intern-attendance.jsp` (phần "Đơn nghỉ phép")

## 1. Thông báo kết quả (đặt cạnh các `feedback` hiện có)
```jsp
<c:if test="${param.leave=='created'}"><div class="feedback success"><i class="fa-solid fa-circle-check"></i> Đã gửi đơn nghỉ. HR đã nhận được thông báo.</div></c:if>
<c:if test="${param.leave=='cancelled'}"><div class="feedback success"><i class="fa-solid fa-circle-check"></i> Đã hủy đơn nghỉ.</div></c:if>
```

## 2. Form gửi đơn: thêm `min` cho ngày và gợi ý quy tắc
```jsp
<jsp:useBean id="now" class="java.util.Date"/>
<fmt:formatDate value="${now}" pattern="yyyy-MM-dd" var="todayStr"/>
...
<input type="date" name="startDate" min="${todayStr}" required>
<input type="date" name="endDate" min="${todayStr}" required>
```
(thêm `<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>` ở đầu file)
Gợi ý dưới form: "Nghỉ cá nhân/không lương phải báo trước ít nhất 1 ngày; tối đa 30 ngày làm việc/đơn."

## 3. Bảng đơn nghỉ: thêm cột THAO TÁC + ghi chú HR
```jsp
<th>GHI CHÚ HR</th><th>THAO TÁC</th>
...
<td><c:out value="${l.approver_notes}" default="—"/></td>
<td>
  <c:if test="${l.approval_status=='PENDING'}">
    <form action="${pageContext.request.contextPath}/leave-requests" method="post"
          onsubmit="return confirm('Hủy đơn nghỉ này?')">
      <input type="hidden" name="action" value="cancel">
      <input type="hidden" name="leaveId" value="${l.id}">
      <button class="reject-action" type="submit">Hủy đơn</button>
    </form>
  </c:if>
</td>
```
Nhớ đổi `colspan="4"` của dòng rỗng thành `6`.

## 4. `attendance-report.jsp`
Ô "Ghi chú" của form duyệt nên `required` khi bấm "Từ chối" (servlet đã bắt buộc ≥ 5 ký tự).
