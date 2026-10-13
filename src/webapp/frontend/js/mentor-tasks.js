/* US-S3-01 - Frontend demo only. Không gửi dữ liệu đến server. */
(function () {
  'use strict';
  var interns = [
    { id: 'nguyen-an', name: 'Nguyễn An' },
    { id: 'tran-binh', name: 'Trần Bình' },
    { id: 'pham-hoa', name: 'Phạm Hoa' },
    { id: 'le-minh', name: 'Lê Minh' }
  ];
  var statuses = { active: 'Đang hiệu lực', edited: 'Đã chỉnh sửa', overdue: 'Quá hạn', cancelled: 'Đã hủy', reopened: 'Đã mở lại' };
  var today = new Date();
  function offsetDate(days) { var d = new Date(today.getFullYear(), today.getMonth(), today.getDate() + days); return d.getFullYear() + '-' + String(d.getMonth() + 1).padStart(2, '0') + '-' + String(d.getDate()).padStart(2, '0'); }
  var tasks = [
    { id: 1, title: 'Hoàn thiện API báo cáo', description: 'Xây dựng endpoint và tài liệu API cho báo cáo thực tập.', deadline: offsetDate(6), assignees: ['nguyen-an', 'tran-binh'], completed: 2, total: 5, progress: 68, status: 'edited' },
    { id: 2, title: 'Kiểm thử form đăng ký', description: 'Viết checklist và kiểm thử giao diện đăng ký.', deadline: offsetDate(-2), assignees: ['pham-hoa'], completed: 1, total: 3, progress: 40, status: 'overdue' },
    { id: 3, title: 'Thiết kế dashboard', description: 'Thiết kế bố cục thống kê theo hướng dẫn mentor.', deadline: offsetDate(12), assignees: ['le-minh', 'tran-binh'], completed: 0, total: 4, progress: 15, status: 'active' }
  ];
  var nextId = 4;
  var $ = function (id) { return document.getElementById(id); };
  var toastTimer;
  function notify(message) { var el = $('toast'); el.textContent = message; el.classList.add('visible'); clearTimeout(toastTimer); toastTimer = setTimeout(function () { el.classList.remove('visible'); }, 3500); }
  function internName(id) { var found = interns.find(function (p) { return p.id === id; }); return found ? found.name : 'Không xác định'; }
  function displayDate(s) { var p = s.split('-'); return p[2] + '/' + p[1] + '/' + p[0]; }
  function el(tag, className, text) { var n = document.createElement(tag); if (className) n.className = className; if (text !== undefined) n.textContent = String(text); return n; }
  function cell(row, text) { var td = el('td', '', text); row.appendChild(td); return td; }
  function actionButton(label, className, handler) { var b = el('button', 'btn btn-sm ' + className, label); b.type = 'button'; b.addEventListener('click', handler); return b; }
  function render() {
    var q = $('searchInput').value.trim().toLocaleLowerCase('vi');
    var who = $('assigneeFilter').value;
    var status = $('statusFilter').value;
    var filtered = tasks.filter(function (t) { return (t.title + ' ' + t.description).toLocaleLowerCase('vi').includes(q) && (who === 'all' || t.assignees.includes(who)) && (status === 'all' || t.status === status); });
    var body = $('taskRows'); body.replaceChildren();
    filtered.forEach(function (task) {
      var tr = el('tr'); var titleTd = cell(tr, ''); titleTd.appendChild(el('strong', '', task.title));
      cell(tr, displayDate(task.deadline)); cell(tr, task.completed + '/' + task.total);
      var progressTd = cell(tr, ''); progressTd.appendChild(el('div', '', task.progress + '%'));
      var bar = el('div', 'progress'); var fill = el('span'); fill.style.width = Math.max(0, Math.min(100, task.progress)) + '%'; bar.appendChild(fill); progressTd.appendChild(bar);
      cell(tr, task.assignees.map(internName).join(', '));
      var stateTd = cell(tr, ''); stateTd.appendChild(el('span', 'badge badge-' + task.status, statuses[task.status]));
      var actions = cell(tr, '');
      actions.className = 'task-actions-cell';
      var actionMenu = document.createElement('details');
      actionMenu.className = 'task-actions-menu';
      var menuToggle = el('summary', 'btn btn-sm btn-light', '⋯ Thao tác');
      menuToggle.setAttribute('aria-label', 'Thao tác với nhiệm vụ: ' + task.title);
      var menuOptions = el('div', 'task-actions-options');
      function addMenuAction(label, className, handler) {
        menuOptions.appendChild(actionButton(label, className, function () {
          actionMenu.open = false;
          handler();
        }));
      }
      addMenuAction('Sửa', 'btn-light', function () { editTask(task.id); });
      if (task.status === 'cancelled') { addMenuAction('Mở lại', 'btn-light', function () { changeStatus(task.id, 'reopened'); }); }
      else { addMenuAction('Hủy', 'btn-danger', function () { changeStatus(task.id, 'cancelled'); }); }
      actionMenu.append(menuToggle, menuOptions);
      actions.appendChild(actionMenu);
      tr.appendChild(actions); body.appendChild(tr);
    });
    $('tableWrap').hidden = filtered.length === 0; $('emptyState').hidden = filtered.length !== 0;
    $('resultCount').textContent = filtered.length + ' / ' + tasks.length + ' nhiệm vụ';
    $('statTotal').textContent = tasks.length;
    $('statActive').textContent = tasks.filter(function (t) { return ['active', 'edited', 'reopened'].includes(t.status); }).length;
    $('statOverdue').textContent = tasks.filter(function (t) { return t.status === 'overdue'; }).length;
    $('statProgress').textContent = tasks.length ? Math.round(tasks.reduce(function (s, t) { return s + t.progress; }, 0) / tasks.length) + '%' : '0%';
  }
  function changeStatus(id, status) {
    var task = tasks.find(function (t) { return t.id === id; }); if (!task) return;
    if (!window.confirm('Bạn chắc chắn muốn ' + (status === 'cancelled' ? 'hủy' : 'mở lại') + ' nhiệm vụ "' + task.title + '"?')) return;
    task.status = status; render(); notify('Đã cập nhật trạng thái trong dữ liệu mẫu.');
  }
  function clearForm() {
    $('taskForm').reset(); $('editingId').value = ''; $('formError').hidden = true;
    $('formTitle').textContent = 'Tạo nhiệm vụ mới'; $('submitTask').textContent = 'Giao nhiệm vụ';
  }
  function editTask(id) {
    var task = tasks.find(function (t) { return t.id === id; }); if (!task) return;
    $('editingId').value = String(id); $('taskTitle').value = task.title; $('taskDescription').value = task.description; $('taskDeadline').value = task.deadline;
    document.querySelectorAll('input[name="assignee"]').forEach(function (c) { c.checked = task.assignees.includes(c.value); });
    $('formTitle').textContent = 'Chỉnh sửa nhiệm vụ'; $('submitTask').textContent = 'Lưu thay đổi'; $('formError').hidden = true;
    $('taskFormPanel').scrollIntoView({ behavior: 'smooth', block: 'start' }); $('taskTitle').focus();
  }
  function submitForm(event) {
    event.preventDefault();
    var title = $('taskTitle').value.trim(); var description = $('taskDescription').value.trim(); var deadline = $('taskDeadline').value;
    var selected = Array.from(document.querySelectorAll('input[name="assignee"]:checked')).map(function (c) { return c.value; });
    var error = '';
    if (!title || !description || !deadline) error = 'Vui lòng nhập đầy đủ tiêu đề, mô tả và hạn hoàn thành.';
    else if (!selected.length) error = 'Vui lòng chọn ít nhất một thực tập sinh.';
    else if (deadline < offsetDate(0)) error = 'Hạn hoàn thành không được ở trong quá khứ.';
    if (error) { $('formError').textContent = error; $('formError').hidden = false; return; }
    $('formError').hidden = true;
    var id = Number($('editingId').value);
    if (id) {
      var existing = tasks.find(function (t) { return t.id === id; }); if (!existing) return;
      if (!window.confirm('Xác nhận lưu thay đổi nhiệm vụ?')) return;
      existing.title = title; existing.description = description; existing.deadline = deadline; existing.assignees = selected; existing.status = 'edited';
      notify('Đã chỉnh sửa nhiệm vụ trong dữ liệu mẫu.');
    } else {
      if (!window.confirm('Xác nhận giao nhiệm vụ cho ' + selected.length + ' thực tập sinh?')) return;
      tasks.unshift({ id: nextId++, title: title, description: description, deadline: deadline, assignees: selected, completed: 0, total: selected.length, progress: 0, status: 'active' });
      notify('Đã tạo nhiệm vụ mẫu. Chưa lưu vào MySQL.');
    }
    clearForm(); render(); $('taskListTitle').scrollIntoView({ behavior: 'smooth', block: 'start' });
  }
  function init() {
    interns.forEach(function (p) {
      var option = el('option', '', p.name); option.value = p.id; $('assigneeFilter').appendChild(option);
      var label = el('label'); var cb = document.createElement('input'); cb.type = 'checkbox'; cb.name = 'assignee'; cb.value = p.id; label.append(cb, document.createTextNode(p.name)); $('assigneeChecks').appendChild(label);
    });
    ['searchInput', 'assigneeFilter', 'statusFilter'].forEach(function (id) { $(id).addEventListener(id === 'searchInput' ? 'input' : 'change', render); });
    $('globalSearchInput').addEventListener('input', function () {
      $('searchInput').value = this.value;
      render();
    });
    $('searchInput').addEventListener('input', function () {
      $('globalSearchInput').value = this.value;
    });
    $('taskForm').addEventListener('submit', submitForm);
    $('cancelForm').addEventListener('click', function () { clearForm(); notify('Đã hủy thao tác nhập liệu.'); });
    $('openCreate').addEventListener('click', function () { clearForm(); $('taskFormPanel').scrollIntoView({ behavior: 'smooth' }); $('taskTitle').focus(); });
    $('retryButton').addEventListener('click', function () { $('errorState').hidden = true; render(); });
    document.querySelectorAll('[data-demo-nav]').forEach(function (link) { link.addEventListener('click', function (event) { event.preventDefault(); notify('Mục ' + link.dataset.demoNav + ' chưa được nối với hệ thống chính.'); }); });
    render();
  }
  document.addEventListener('DOMContentLoaded', init);
}());
