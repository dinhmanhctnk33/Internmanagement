<%-- SCRUM-104 / WF13: Thực tập sinh check-in, check-out và xem lịch sử chấm công. Giờ lấy từ server, giao diện không tự tính giờ.
     Servlet cần đặt attribute: serverTime (chuỗi HH:mm:ss), today (checkIn, checkOut, status) hoặc null, periodStart, periodEnd,
     month, monthTotal (vd "86 giờ 30 phút"), records (id, workDate, checkIn, checkOut, total, status, edited, editable),
     message, error. status: IN_PROGRESS | COMPLETED | MISSING_CHECKOUT | CANCELLED.
     Form POST tới /intern/attendance với action=checkin|checkout|edit|cancel. --%>
<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<link rel="stylesheet" href="${ctx}/assets/css/intern-pages.css">
<div class="ip">
  <h1>Chấm công hôm nay</h1>
  <p class="sub">Giờ máy chủ: <b><c:out value="${serverTime}"/></b></p>
  <c:if test="${not empty message}"><div class="ok" role="status"><c:out value="${message}"/></div></c:if>
  <c:if test="${not empty error}"><div class="er" role="alert"><c:out value="${error}"/></div></c:if>

  <section class="cd" style="text-align:center">
    <p class="mu">Kỳ thực tập <c:out value="${periodStart}"/> – <c:out value="${periodEnd}"/></p>
    <c:choose>
      <c:when test="${empty today}">
        <p style="margin:8px 0 18px"><span class="b n">Chưa check-in</span></p>
        <form method="post" action="${ctx}/intern/attendance" class="js-once"><input type="hidden" name="action" value="checkin">
          <button class="bt p big" type="submit">CHECK IN</button></form>
      </c:when>
      <c:when test="${today.status == 'IN_PROGRESS'}">
        <p style="margin:8px 0 6px"><span class="b a">Đang làm việc</span></p>
        <p class="mu" style="margin-bottom:18px">Check-in lúc <c:out value="${today.checkIn}"/></p>
        <form method="post" action="${ctx}/intern/attendance" class="js-once"><input type="hidden" name="action" value="checkout">
          <button class="bt p big" type="submit">CHECK OUT</button></form>
      </c:when>
      <c:otherwise>
        <p style="margin:8px 0 6px"><span class="b g">Đã hoàn tất hôm nay</span></p>
        <p class="mu"><c:out value="${today.checkIn}"/> – <c:out value="${today.checkOut}"/></p>
      </c:otherwise>
    </c:choose>
  </section>

  <section class="cd">
    <div class="row sp" style="margin-bottom:8px">
      <h2 style="margin:0">Lịch sử tháng <c:out value="${month}"/></h2>
      <b>Tổng giờ: <c:out value="${monthTotal}"/></b>
    </div>
    <c:if test="${empty records}"><p class="mu">Chưa có bản ghi chấm công trong tháng này.</p></c:if>
    <c:if test="${not empty records}">
    <div class="tw"><table>
      <thead><tr><th>Ngày</th><th>Giờ vào</th><th>Giờ ra</th><th>Tổng</th><th>Trạng thái</th><th>Nhãn</th><th></th></tr></thead>
      <tbody>
      <c:forEach var="r" items="${records}">
        <tr>
          <td><c:out value="${r.workDate}"/></td>
          <td><c:out value="${empty r.checkIn ? '–' : r.checkIn}"/></td>
          <td><c:out value="${empty r.checkOut ? '–' : r.checkOut}"/></td>
          <td><c:out value="${empty r.total ? '–' : r.total}"/></td>
          <td><c:choose>
            <c:when test="${r.status == 'COMPLETED'}"><span class="b g">Hoàn tất</span></c:when>
            <c:when test="${r.status == 'MISSING_CHECKOUT'}"><span class="b a">Thiếu check-out</span></c:when>
            <c:when test="${r.status == 'CANCELLED'}"><span class="b n">Đã hủy</span></c:when>
            <c:otherwise><span class="b u">Đang làm</span></c:otherwise></c:choose></td>
          <td><c:if test="${r.edited}"><span class="b u">Đã chỉnh sửa</span></c:if><c:if test="${!r.edited}">–</c:if></td>
          <td><c:if test="${r.editable}">
            <button type="button" class="bt js-edit" data-id="${r.id}" data-date="<c:out value='${r.workDate}'/>"
              data-in="<c:out value='${r.checkIn}'/>" data-out="<c:out value='${r.checkOut}'/>">
              <c:out value="${r.status == 'MISSING_CHECKOUT' ? 'Bổ sung' : 'Sửa'}"/></button></c:if></td>
        </tr>
      </c:forEach>
      </tbody></table></div>
    </c:if>
  </section>

  <dialog id="editDlg" aria-labelledby="edTitle">
    <form method="post" action="${ctx}/intern/attendance">
      <h2 id="edTitle">Sửa hoặc bổ sung chấm công</h2>
      <input type="hidden" name="action" value="edit"><input type="hidden" name="id" id="edId">
      <p class="mu" id="edDate"></p>
      <label class="f">Giờ vào<input type="time" name="checkIn" id="edIn" required></label>
      <label class="f">Giờ ra<input type="time" name="checkOut" id="edOut" required></label>
      <label class="f">Lý do (bắt buộc)<textarea name="reason" rows="3" required maxlength="500"></textarea></label>
      <div class="ac"><button type="button" class="bt" id="edClose">Quay lại</button><button type="submit" class="bt p">Lưu thay đổi</button></div>
    </form>
  </dialog>
  <script>
    (function () {
      var dlg = document.getElementById('editDlg');
      document.querySelectorAll('.js-edit').forEach(function (b) {
        b.addEventListener('click', function () {
          document.getElementById('edId').value = b.dataset.id;
          document.getElementById('edDate').textContent = 'Ngày ' + b.dataset.date;
          document.getElementById('edIn').value = b.dataset.in || '';
          document.getElementById('edOut').value = b.dataset.out || '';
          dlg.showModal();
        });
      });
      document.getElementById('edClose').addEventListener('click', function () { dlg.close(); });
      document.querySelectorAll('.js-once').forEach(function (f) {   // chống bấm hai lần
        f.addEventListener('submit', function () { f.querySelector('button').disabled = true; });
      });
    })();
  </script>
</div>
