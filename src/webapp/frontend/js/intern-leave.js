/* SCRUM148 - Đăng ký nghỉ phép (frontend). Không gọi API; form gửi bằng POST chuẩn tới /leave-requests. */
(function () {
    'use strict';

    var STATUS = {
        PENDING: 'Chờ duyệt', APPROVED: 'Đã duyệt', REJECTED: 'Bị từ chối',
        CANCELLED: 'Đã hủy', EXPIRED: 'Quá ngày bắt đầu'
    };
    var TYPES = { SICK_LEAVE: 'Nghỉ ốm', PERSONAL_LEAVE: 'Nghỉ cá nhân', UNPAID_LEAVE: 'Nghỉ không lương' };
    var PAGE_SIZE = 8;
    var MIN_REASON = 5;
    var DRAFT_KEY = 'internLeaveDraft';
    var STORAGE_KEY = 'ims_intern_leave_demo_items';

    var page = document.getElementById('lvPage');
    if (!page) return;

    var isPreview = page.getAttribute('data-preview') === 'true'
        || window.location.protocol === 'file:'
        || window.location.pathname.indexOf('intern-leave-preview.html') !== -1;

    function $(id) { return document.getElementById(id); }
    function pad(n) { return n < 10 ? '0' + n : String(n); }
    function toISO(d) { return d.getFullYear() + '-' + pad(d.getMonth() + 1) + '-' + pad(d.getDate()); }
    function parseISO(s) {
        var m = /^(\d{4})-(\d{2})-(\d{2})/.exec(s || '');
        if (!m) return null;
        var d = new Date(+m[1], +m[2] - 1, +m[3]);
        return d.getFullYear() === +m[1] && d.getMonth() === +m[2] - 1 && d.getDate() === +m[3] ? d : null;
    }
    function fmt(d) { return d ? pad(d.getDate()) + '/' + pad(d.getMonth() + 1) + '/' + d.getFullYear() : '—'; }
    function today() { var n = new Date(); return new Date(n.getFullYear(), n.getMonth(), n.getDate()); }

    // Đếm ngày làm việc T2-T6 trong [from, to], chưa trừ ngày lễ.
    function workingDays(from, to) {
        if (!from || !to || to < from) return null;
        var count = 0;
        for (var d = new Date(from); d <= to; d.setDate(d.getDate() + 1)) {
            var w = d.getDay();
            if (w !== 0 && w !== 6) count++;
        }
        return count;
    }

    function createRowElement(item) {
        var tr = document.createElement('tr');
        tr.className = 'lv-row';
        tr.setAttribute('data-status', item.status || 'PENDING');
        tr.setAttribute('data-type', item.type || 'PERSONAL_LEAVE');
        tr.setAttribute('data-start', item.start || '');
        tr.setAttribute('data-end', item.end || '');
        tr.setAttribute('data-reason', item.reason || '');

        var start = parseISO(item.start);
        var end = parseISO(item.end);
        var days = workingDays(start, end);

        var td1 = document.createElement('td');
        td1.setAttribute('data-label', 'Từ ngày');
        td1.innerHTML = '<strong class="js-start">' + fmt(start) + '</strong>';

        var td2 = document.createElement('td');
        td2.setAttribute('data-label', 'Đến ngày');
        td2.innerHTML = '<span class="js-end">' + fmt(end) + '</span>';

        var td3 = document.createElement('td');
        td3.setAttribute('data-label', 'Số ngày làm việc');
        td3.innerHTML = '<span class="js-workdays">' + (days === null ? '—' : days) + '</span>';

        var td4 = document.createElement('td');
        td4.setAttribute('data-label', 'Trạng thái');
        var st = item.status || 'PENDING';
        td4.innerHTML = '<span class="lv-status js-status ' + st + '">' + (STATUS[st] || st) + '</span>';

        tr.appendChild(td1);
        tr.appendChild(td2);
        tr.appendChild(td3);
        tr.appendChild(td4);
        return tr;
    }

    /* ---------- Danh sách ---------- */
    var table = $('lvTable');

    // Nạp dữ liệu mô phỏng từ localStorage trong chế độ Preview
    if (isPreview) {
        try {
            var savedItems = JSON.parse(localStorage.getItem(STORAGE_KEY) || '[]');
            if (Array.isArray(savedItems) && savedItems.length > 0) {
                var tbody = $('lvBody');
                for (var i = savedItems.length - 1; i >= 0; i--) {
                    var el = createRowElement(savedItems[i]);
                    if (tbody.firstChild) {
                        tbody.insertBefore(el, tbody.firstChild);
                    } else {
                        tbody.appendChild(el);
                    }
                }
            }
        } catch (e) {
            console.error('Không thể nạp dữ liệu từ localStorage:', e);
        }
    }

    var rows = Array.prototype.slice.call($('lvBody').querySelectorAll('tr.lv-row')).map(function (tr) {
        var start = parseISO(tr.getAttribute('data-start'));
        var end = parseISO(tr.getAttribute('data-end'));
        var status = tr.getAttribute('data-status');
        if (!STATUS[status]) status = 'PENDING';
        // Đơn chờ duyệt đã qua ngày bắt đầu.
        if (status === 'PENDING' && start && start < today()) status = 'EXPIRED';
        var days = workingDays(start, end);
        var startEl = tr.querySelector('.js-start');
        if (startEl) startEl.textContent = fmt(start);
        var endEl = tr.querySelector('.js-end');
        if (endEl) endEl.textContent = fmt(end);
        var workdaysEl = tr.querySelector('.js-workdays');
        if (workdaysEl) workdaysEl.textContent = days === null ? '—' : days;
        var reasonText = tr.getAttribute('data-reason') || '';
        var reasonEl = tr.querySelector('.lv-reason');
        if (reasonEl) reasonText = reasonEl.textContent;
        var badge = tr.querySelector('.js-status');
        if (badge) {
            badge.className = 'lv-status js-status ' + status;
            badge.textContent = STATUS[status];
        }
        return {
            tr: tr, start: start, end: end, status: status,
            text: ((TYPES[tr.getAttribute('data-type')] || '') + ' ' + reasonText + ' ' + STATUS[status] + ' ' +
                fmt(start) + ' ' + fmt(end)).toLowerCase()
        };
    });

    var currentPage = 1;
    var filtered = rows.slice();
    var loadFailed = page.getAttribute('data-load-error') === 'true';

    function updateCounters() {
        var counts = { PENDING: 0, APPROVED: 0, REJECTED: 0, CANCELLED: 0, EXPIRED: 0 };
        rows.forEach(function (r) { counts[r.status]++; });
        Object.keys(counts).forEach(function (key) {
            page.querySelector('[data-count="' + key + '"]').textContent = counts[key];
        });
    }

    function readFilters() {
        var from = parseISO($('lvFilterFrom').value);
        var to = parseISO($('lvFilterTo').value);
        var error = $('lvFilterError');
        if (from && to && from > to) {
            error.textContent = 'Khoảng ngày lọc không hợp lệ: ngày bắt đầu phải trước hoặc bằng ngày kết thúc.';
            error.hidden = false;
            from = to = null;
        } else {
            error.hidden = true;
        }
        return { q: $('lvSearch').value.trim().toLowerCase(), from: from, to: to, status: $('lvFilterStatus').value };
    }

    function applyFilters() {
        var f = readFilters();
        filtered = rows.filter(function (r) {
            if (f.q && r.text.indexOf(f.q) === -1) return false;
            if (f.status && r.status !== f.status) return false;
            if (f.from && r.end && r.end < f.from) return false;
            if (f.to && r.start && r.start > f.to) return false;
            return true;
        });
        $('lvFilterReset').hidden = !(f.q || f.from || f.to || f.status || $('lvFilterError').hidden === false);
        currentPage = 1;
        render();
    }

    function render() {
        $('lvLoading').hidden = true;
        table.removeAttribute('aria-busy');

        if (loadFailed) {
            table.hidden = true;
            $('lvError').hidden = false;
            $('lvEmpty').hidden = true;
            $('lvPagination').hidden = true;
            return;
        }

        var pages = Math.max(1, Math.ceil(filtered.length / PAGE_SIZE));
        if (currentPage > pages) currentPage = pages;
        var from = (currentPage - 1) * PAGE_SIZE;
        var visible = filtered.slice(from, from + PAGE_SIZE);
        rows.forEach(function (r) { r.tr.hidden = visible.indexOf(r) === -1; });

        var noMatch = filtered.length === 0 && rows.length > 0;
        $('lvEmpty').hidden = filtered.length > 0;
        $('lvEmptyText').textContent = noMatch ? 'Không có đơn phù hợp với bộ lọc.' : 'Chưa có đơn nghỉ phép.';
        $('lvEmptyReset').hidden = !noMatch;
        $('lvError').hidden = true;
        table.hidden = filtered.length === 0;

        $('lvPagination').hidden = filtered.length <= PAGE_SIZE;
        $('lvPageInfo').textContent = 'Hiển thị ' + (from + 1) + '–' + (from + visible.length) + ' / ' + filtered.length;
        renderPages(pages);
    }

    function pageButton(label, target, opts) {
        var b = document.createElement('button');
        b.type = 'button';
        b.innerHTML = label;
        if (opts.aria) b.setAttribute('aria-label', opts.aria);
        if (opts.active) { b.className = 'active'; b.setAttribute('aria-current', 'page'); }
        if (opts.disabled) b.disabled = true;
        else b.addEventListener('click', function () { currentPage = target; render(); });
        return b;
    }

    function renderPages(total) {
        var box = $('lvPages');
        box.innerHTML = '';
        if (total <= 1) return;
        box.appendChild(pageButton('<i class="fa-solid fa-chevron-left"></i>', currentPage - 1, { disabled: currentPage === 1, aria: 'Trang trước' }));
        for (var i = 1; i <= total; i++) {
            if (i === 1 || i === total || Math.abs(i - currentPage) <= 1) {
                box.appendChild(pageButton(String(i), i, { active: i === currentPage, aria: 'Trang ' + i }));
            } else if (box.lastChild.textContent !== '…') {
                box.appendChild(pageButton('…', i, { disabled: true }));
            }
        }
        box.appendChild(pageButton('<i class="fa-solid fa-chevron-right"></i>', currentPage + 1, { disabled: currentPage === total, aria: 'Trang sau' }));
    }

    function resetFilters() {
        ['lvSearch', 'lvFilterFrom', 'lvFilterTo', 'lvFilterStatus'].forEach(function (id) { $(id).value = ''; });
        applyFilters();
    }

    ['lvSearch', 'lvFilterFrom', 'lvFilterTo', 'lvFilterStatus'].forEach(function (id) {
        $(id).addEventListener(id === 'lvSearch' ? 'input' : 'change', applyFilters);
    });
    $('lvFilterReset').addEventListener('click', resetFilters);
    $('lvEmptyReset').addEventListener('click', resetFilters);
    $('lvRetry').addEventListener('click', function () { window.location.reload(); });

    updateCounters();
    applyFilters();

    /* ---------- Form ---------- */
    var form = $('lvForm');
    var formCard = $('lvFormCard');
    var formCloseBtn = $('lvFormClose');
    var confirmModal = $('lvConfirmModal');
    var newRequestBtn = $('lvNewRequest');
    var fields = {
        type: { input: $('lvType'), error: $('lvTypeError') },
        start: { input: $('lvStart'), error: $('lvStartError') },
        end: { input: $('lvEnd'), error: $('lvEndError') },
        reason: { input: $('lvReason'), error: $('lvReasonError') }
    };
    var touched = {};
    var submitBtn = $('lvSubmit');
    var confirmBtn = $('lvConfirm');

    $('lvEarliest').textContent = fmt(today());
    fields.start.input.min = toISO(today());
    fields.end.input.min = toISO(today());

    // Quy tắc khớp LeaveRequestServlet.submit().
    function validate(name) {
        var v = fields[name].input.value;
        if (name === 'type') return TYPES[v] ? '' : 'Loại nghỉ không hợp lệ.';
        if (name === 'start') {
            if (!v) return 'Vui lòng chọn ngày bắt đầu.';
            var s = parseISO(v);
            if (!s) return 'Ngày bắt đầu không hợp lệ.';
            return s < today() ? 'Không thể gửi đơn cho ngày đã qua.' : '';
        }
        if (name === 'end') {
            if (!v) return 'Vui lòng chọn ngày kết thúc.';
            var e = parseISO(v);
            if (!e) return 'Ngày kết thúc không hợp lệ.';
            var st = parseISO(fields.start.input.value);
            return st && e < st ? 'Ngày kết thúc phải bằng hoặc sau ngày bắt đầu.' : '';
        }
        var t = v.trim();
        if (!t) return 'Vui lòng nhập lý do nghỉ.';
        return t.length < MIN_REASON ? 'Vui lòng nhập lý do ít nhất ' + MIN_REASON + ' ký tự.' : '';
    }

    function showError(name, message) {
        var f = fields[name];
        f.error.textContent = message;
        f.error.hidden = !message;
        f.input.closest('.lv-field').classList.toggle('invalid', !!message);
        if (message) f.input.setAttribute('aria-invalid', 'true'); else f.input.removeAttribute('aria-invalid');
    }
    function check(name) { var m = validate(name); showError(name, m); return !m; }

    function updateWorkdays() {
        var s = parseISO(fields.start.input.value);
        var e = parseISO(fields.end.input.value);
        var days = workingDays(s, e);
        $('lvWorkdays').textContent = days === null ? '—' : days;
        $('lvWorkdaysWarning').hidden = days !== 0;
        if (s && s >= today()) fields.end.input.min = toISO(s);
    }

    Object.keys(fields).forEach(function (name) {
        var input = fields[name].input;
        input.addEventListener('input', function () {
            updateWorkdays();
            if (touched[name]) check(name);
            if (name === 'start' && touched.end) check('end');
        });
        input.addEventListener('change', updateWorkdays);
        input.addEventListener('blur', function () { touched[name] = true; check(name); });
    });

    function isFormDirty() {
        if (!form) return false;
        var typeChanged = fields.type.input.value !== 'PERSONAL_LEAVE';
        var hasStart = !!fields.start.input.value;
        var hasEnd = !!fields.end.input.value;
        var hasReason = fields.reason.input.value.trim().length > 0;
        return typeChanged || hasStart || hasEnd || hasReason;
    }

    function clearForm() {
        if (!form) return;
        form.reset();
        touched = {};
        Object.keys(fields).forEach(function (name) { showError(name, ''); });
        var formErr = $('lvFormError');
        if (formErr) {
            formErr.textContent = '';
            formErr.hidden = true;
        }
        var workdaysWarn = $('lvWorkdaysWarning');
        if (workdaysWarn) {
            workdaysWarn.hidden = true;
        }
        $('lvWorkdays').textContent = '—';
        fields.start.input.min = toISO(today());
        fields.end.input.min = toISO(today());
        updateWorkdays();
    }

    function openForm() {
        if (!formCard) return;
        // Nếu form không có dữ liệu dở dang thì xóa sạch mọi thông báo lỗi cũ
        if (!isFormDirty()) {
            clearForm();
        }
        formCard.hidden = false;
        formCard.style.display = '';
        try {
            formCard.scrollIntoView({ behavior: 'smooth', block: 'start' });
        } catch (e) {}
        fields.start.input.focus();
    }

    function forceCloseForm() {
        if (!formCard) return;
        clearForm();
        formCard.hidden = true;
        formCard.style.display = 'none';
        if (newRequestBtn) newRequestBtn.focus();
    }

    function requestCloseForm() {
        if (!formCard || formCard.hidden || formCard.style.display === 'none') return;
        if (isFormDirty()) {
            var confirmed = window.confirm('Dữ liệu đang nhập sẽ không được lưu. Bạn có chắc chắn muốn đóng form không?');
            if (!confirmed) return;
        }
        forceCloseForm();
    }

    function toggleForm() {
        if (!formCard) return;
        if (formCard.hidden || formCard.style.display === 'none') {
            openForm();
        } else {
            requestCloseForm();
        }
    }

    function openConfirm() {
        if (!confirmModal) return;
        confirmModal.hidden = false;
        document.body.classList.add('lv-lock');
        confirmBtn.focus();
    }

    function closeConfirm() {
        if (!confirmModal) return;
        confirmModal.hidden = true;
        document.body.classList.remove('lv-lock');
        submitBtn.focus();
    }

    if (newRequestBtn) {
        newRequestBtn.addEventListener('click', function (e) {
            e.preventDefault();
            toggleForm();
        });
    }
    if (formCloseBtn) {
        formCloseBtn.addEventListener('click', function (e) {
            e.preventDefault();
            requestCloseForm();
        });
    }
    var cancelBtn = $('lvCancel');
    if (cancelBtn) {
        cancelBtn.addEventListener('click', function (e) {
            e.preventDefault();
            requestCloseForm();
        });
    }
    $('lvBack').addEventListener('click', closeConfirm);

    if (confirmModal) {
        confirmModal.addEventListener('click', function (event) {
            if (event.target.closest('[data-dismiss]')) closeConfirm();
        });
    }

    form.addEventListener('submit', function (event) {
        event.preventDefault();
        var firstInvalid = null;
        ['type', 'start', 'end', 'reason'].forEach(function (name) {
            touched[name] = true;
            if (!check(name) && !firstInvalid) firstInvalid = fields[name].input;
        });
        if (firstInvalid) { firstInvalid.focus(); return; }
        var s = parseISO(fields.start.input.value);
        var e = parseISO(fields.end.input.value);
        $('lvSumType').textContent = TYPES[fields.type.input.value];
        $('lvSumRange').textContent = fmt(s) + ' – ' + fmt(e);
        $('lvSumDays').textContent = workingDays(s, e);
        $('lvSumReason').textContent = fields.reason.input.value.trim();
        openConfirm();
    });

    confirmBtn.addEventListener('click', function () {
        if (!isPreview) {
            confirmBtn.disabled = true;
            submitBtn.disabled = true;
            confirmBtn.innerHTML = '<i class="fa-solid fa-spinner fa-spin"></i> Đang gửi...';
            // Giữ bản nháp để khôi phục nếu máy chủ trả lỗi và người dùng quay lại trang này.
            try {
                sessionStorage.setItem(DRAFT_KEY, JSON.stringify({
                    type: fields.type.input.value, start: fields.start.input.value,
                    end: fields.end.input.value, reason: fields.reason.input.value
                }));
            } catch (ignored) { /* sessionStorage không khả dụng */ }
            form.submit();
            return;
        }

        // --- Chế độ xem trước (Preview): mô phỏng thêm đơn mới ---
        var newItem = {
            id: 'mock-' + Date.now(),
            type: fields.type.input.value,
            start: fields.start.input.value,
            end: fields.end.input.value,
            reason: fields.reason.input.value.trim(),
            status: 'PENDING'
        };

        // Lưu vào localStorage
        try {
            var demoList = JSON.parse(localStorage.getItem(STORAGE_KEY) || '[]');
            if (!Array.isArray(demoList)) demoList = [];
            demoList.unshift(newItem);
            localStorage.setItem(STORAGE_KEY, JSON.stringify(demoList));
        } catch (e) {
            console.error('Không thể lưu vào localStorage:', e);
        }

        // Thêm DOM vào đầu bảng
        var tr = createRowElement(newItem);
        var tbody = $('lvBody');
        if (tbody.firstChild) {
            tbody.insertBefore(tr, tbody.firstChild);
        } else {
            tbody.appendChild(tr);
        }

        // Thêm vào danh sách rows
        var start = parseISO(newItem.start);
        var end = parseISO(newItem.end);
        var rowRecord = {
            tr: tr, start: start, end: end, status: 'PENDING',
            text: ((TYPES[newItem.type] || '') + ' ' + newItem.reason + ' ' + STATUS['PENDING'] + ' ' +
                fmt(start) + ' ' + fmt(end)).toLowerCase()
        };
        rows.unshift(rowRecord);

        // Cập nhật thống kê số lượng và hiển thị bảng (quay về trang 1, xóa bộ lọc)
        updateCounters();
        resetFilters();

        // Đóng hộp thoại xác nhận và tự động đóng form inline mà không hỏi lại
        closeConfirm();
        forceCloseForm();

        // Hiển thị thông báo kết quả
        var fb = $('lvFeedback');
        var fbText = $('lvFeedbackText');
        if (fb && fbText) {
            fbText.textContent = 'Đơn nghỉ phép đã được đăng ký thành công với trạng thái Chờ duyệt.';
            fb.hidden = false;
            try {
                fb.scrollIntoView({ behavior: 'smooth', block: 'nearest' });
            } catch (e) {}
        }
    });

    document.addEventListener('keydown', function (event) {
        if (confirmModal && !confirmModal.hidden) {
            if (event.key === 'Escape') { closeConfirm(); return; }
            if (event.key === 'Tab') {
                var items = Array.prototype.filter.call(confirmModal.querySelectorAll('button,input,select,textarea'), function (el) {
                    return !el.disabled && el.offsetParent !== null;
                });
                if (!items.length) return;
                var first = items[0], last = items[items.length - 1];
                if (event.shiftKey && document.activeElement === first) { event.preventDefault(); last.focus(); }
                else if (!event.shiftKey && document.activeElement === last) { event.preventDefault(); first.focus(); }
            }
            return;
        }

        if (event.key === 'Escape' && formCard && !formCard.hidden && formCard.style.display !== 'none') {
            requestCloseForm();
        }
    });

    // Quay lại bằng nút Back: mở lại các nút đã khóa.
    window.addEventListener('pageshow', function () {
        submitBtn.disabled = false;
        confirmBtn.disabled = false;
        confirmBtn.innerHTML = '<i class="fa-solid fa-check"></i> Xác nhận gửi';
    });

    // Khôi phục bản nháp khi có lỗi từ máy chủ; xóa khi gửi thành công hoặc truy cập bình thường.
    var query = new URLSearchParams(window.location.search);
    var draft = null;
    try { draft = JSON.parse(sessionStorage.getItem(DRAFT_KEY) || 'null'); } catch (ignored) { draft = null; }
    if (draft && query.has('error')) {
        fields.type.input.value = TYPES[draft.type] ? draft.type : fields.type.input.value;
        fields.start.input.value = draft.start || '';
        fields.end.input.value = draft.end || '';
        fields.reason.input.value = draft.reason || '';
        var err = $('lvFormError');
        err.textContent = query.get('error');
        err.hidden = false;
        updateWorkdays();
        openForm();
    } else {
        try { sessionStorage.removeItem(DRAFT_KEY); } catch (ignored) { /* bỏ qua */ }
    }

    updateWorkdays();
})();
