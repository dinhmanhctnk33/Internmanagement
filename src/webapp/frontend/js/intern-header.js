(function () {
    var navigationByRole = {
        HR: [
            ['Thống kê', 'fa-chart-line', [['Dashboard HR', '/home', 'fa-gauge-high']]],
            ['Quản lý thông tin cá nhân', 'fa-address-card', [['Danh sách thực tập sinh', '/interns', 'fa-users'], ['Thêm hồ sơ TTS', '/interns/new', 'fa-user-plus']]],
            ['Quản lý tài liệu', 'fa-folder-open', [['Duyệt tài liệu & CV', '/documents/review', 'fa-file-circle-check']]],
            ['Xét duyệt hồ sơ', 'fa-clipboard-check', [['Xét duyệt ứng viên', '/applications', 'fa-user-check'], ['Email kết quả', '/review-emails', 'fa-envelope-circle-check']]],
            ['Quản lý hợp đồng', 'fa-file-contract', [['Hợp đồng thực tập', '/contracts', 'fa-file-signature']]],
            ['Chương trình & nhóm thực tập', 'fa-people-group', [['Chương trình thực tập', '/programs', 'fa-calendar-days']]],
            ['Lịch thực tập', 'fa-calendar', [['Lịch họp', '/meetings', 'fa-video']]],
            ['Chấm công', 'fa-clock', [['Báo cáo chuyên cần', '/attendance-report', 'fa-chart-column']]],
            ['Lịch làm việc', 'fa-calendar-check', [['Thiết lập theo nhóm', '/work-schedules', 'fa-business-time']]],
            ['Quản lý mentor', 'fa-chalkboard-user', [['Danh sách mentor', '/mentors', 'fa-address-book'], ['Phân công mentor', '/mentor-assignments', 'fa-user-group']]],
            ['Xuất báo cáo', 'fa-file-export', [['Báo cáo cuối kỳ', '/final-reports', 'fa-file-lines'], ['Danh bạ người nhận', '/report-recipients', 'fa-address-book']]],
            ['Thông báo', 'fa-bell', [['Tất cả thông báo', '/notifications', 'fa-bell']]]
        ],
        MENTOR: [
            ['Thống kê', 'fa-chart-pie', [['Tổng quan hướng dẫn', '/mentor/dashboard', 'fa-gauge-high']]],
            ['Giao nhiệm vụ', 'fa-list-check', [['Giao việc cho thực tập sinh', '/mentor/tasks/assign', 'fa-user-check']]],
            ['Báo cáo', 'fa-file-circle-check', [['Báo cáo tuần', '/mentor/reports', 'fa-file-pen']]],
            ['Đánh giá', 'fa-star-half-stroke', [['Đánh giá tổng kết', '/mentor/final-evaluation', 'fa-star']]],
            ['Lịch thực tập', 'fa-calendar', [['Lịch họp', '/meetings', 'fa-video']]],
            ['Thông báo', 'fa-bell', [['Tất cả thông báo', '/notifications', 'fa-bell']]]
        ],
        INTERN: [
            ['Quản lý thông tin cá nhân', 'fa-address-card', [['Trang cá nhân', '/home', 'fa-house'], ['Hồ sơ của tôi', '/interns/profile', 'fa-id-card']]],
            ['Quản lý hợp đồng', 'fa-file-contract', [['Hợp đồng của tôi', '/my-contract', 'fa-file-signature']]],
            ['Lịch thực tập', 'fa-calendar', [['Lịch của tôi', '/my-calendar', 'fa-calendar-days']]],
            ['Chấm công', 'fa-clock', [['Check-in / Check-out', '/attendance', 'fa-fingerprint'], ['Đăng ký nghỉ phép', '/leave-requests', 'fa-plane-departure']]],
            ['Thông báo', 'fa-bell', [['Tất cả thông báo', '/notifications', 'fa-bell']]]
        ],
        ADMIN: [
            ['Tài khoản & phân quyền', 'fa-users-gear', [['Quản lý tài khoản', '/admin/accounts', 'fa-user-gear'], ['Tạo tài khoản', '/admin/users', 'fa-user-plus'], ['Phân quyền', '/admin/permissions', 'fa-shield-halved']]],
            ['Thông báo', 'fa-bell', [['Tất cả thông báo', '/notifications', 'fa-bell']]]
        ]
    };

    // Compact information architecture: a few workflow groups instead of one
    // collapsible row for every individual capability.
    navigationByRole = {
        HR: [
            ['Tổng quan', 'fa-chart-line', [['Dashboard HR', '/home', 'fa-gauge-high'], ['Thông báo', '/notifications', 'fa-bell']]],
            ['Hồ sơ & nhân sự', 'fa-users', [['Thực tập sinh', '/interns', 'fa-users'], ['Thêm hồ sơ TTS', '/interns/new', 'fa-user-plus'], ['Tài liệu & CV', '/documents/review', 'fa-folder-open'], ['Xét duyệt hồ sơ', '/applications', 'fa-clipboard-check'], ['Email kết quả', '/review-emails', 'fa-envelope-circle-check'], ['Hợp đồng', '/contracts', 'fa-file-contract']]],
            ['Vận hành thực tập', 'fa-briefcase', [['Chương trình & nhóm', '/programs', 'fa-people-group'], ['Lịch thực tập', '/meetings', 'fa-calendar'], ['Báo cáo chấm công', '/attendance-report', 'fa-clock'], ['Duyệt đơn nghỉ phép', '/leave-approvals', 'fa-calendar-xmark'], ['Lịch làm việc', '/work-schedules', 'fa-calendar-check']]],
            ['Mentor & báo cáo', 'fa-chalkboard-user', [['Quản lý mentor', '/mentors', 'fa-address-book'], ['Tải công việc mentor', '/mentor-workload', 'fa-scale-balanced'], ['Theo dõi mentor', '/mentor-assignments', 'fa-user-group'], ['Xuất báo cáo', '/final-reports', 'fa-file-export'], ['Người nhận báo cáo', '/report-recipients', 'fa-address-book']]]
        ],
        MENTOR: [
            ['Tổng quan', 'fa-chart-pie', [['Không gian hướng dẫn', '/mentor/dashboard', 'fa-gauge-high'], ['Thông báo', '/notifications', 'fa-bell']]],
            ['Công việc thực tập', 'fa-list-check', [['Giao nhiệm vụ', '/mentor/tasks/assign', 'fa-user-check'], ['Lịch thực tập', '/meetings', 'fa-calendar']]],
            ['Báo cáo & đánh giá', 'fa-star-half-stroke', [['Báo cáo tuần', '/mentor/reports', 'fa-file-pen'], ['Đánh giá tổng kết', '/mentor/final-evaluation', 'fa-star']]]
        ],
        INTERN: [
            ['Cá nhân & hồ sơ', 'fa-address-card', [['Trang cá nhân', '/home', 'fa-house'], ['Hồ sơ của tôi', '/interns/profile', 'fa-id-card'], ['Hợp đồng của tôi', '/my-contract', 'fa-file-contract']]],
            ['Công việc & báo cáo', 'fa-list-check', [['Cập nhật tiến độ', '/intern/progress', 'fa-bars-progress'], ['Nộp báo cáo tuần', '/intern/weekly-reports', 'fa-file-circle-check']]],
            ['Lịch & chấm công', 'fa-calendar', [['Lịch thực tập', '/my-calendar', 'fa-calendar-days'], ['Check-in / Check-out', '/attendance', 'fa-fingerprint'], ['Đăng ký nghỉ phép', '/leave-requests', 'fa-plane-departure']]],
            ['Thông báo', 'fa-bell', [['Tất cả thông báo', '/notifications', 'fa-bell']]]
        ],
        ADMIN: [
            ['Tài khoản & phân quyền', 'fa-users-gear', [['Quản lý tài khoản', '/admin/accounts', 'fa-user-gear'], ['Tạo tài khoản', '/admin/users', 'fa-user-plus'], ['Phân quyền', '/admin/permissions', 'fa-shield-halved']]],
            ['Thông báo', 'fa-bell', [['Tất cả thông báo', '/notifications', 'fa-bell']]]
        ]
    };

    function detectRole(sidebar) {
        var text = (sidebar.textContent || '').toLowerCase();
        if (text.indexOf('quản trị') >= 0 || text.indexOf('admin') >= 0) return 'ADMIN';
        if (text.indexOf('phòng nhân sự') >= 0 || text.indexOf('nghiệp vụ hr') >= 0) return 'HR';
        if (text.indexOf('thực tập sinh') >= 0 || text.indexOf('sinh viên') >= 0) return 'INTERN';
        if (sidebar.classList.contains('mentor-sidebar') || text.indexOf('không gian mentor') >= 0 || text.indexOf('dành cho mentor') >= 0) return 'MENTOR';
        return 'HR';
    }

    function normalizePath(path) {
        var context = location.pathname.split('/')[1];
        var prefix = context ? '/' + context : '';
        return path.indexOf(prefix + '/') === 0 ? path.substring(prefix.length) : path;
    }

    function isActive(current, target) {
        if (target === '/home') return current === '/home' || current === '/';
        if (target === '/interns') return current === '/interns';
        if (target === '/admin/accounts') return current === '/admin/accounts';
        return current === target || current.indexOf(target + '/') === 0;
    }

    function buildUnifiedNavigation() {
        var sidebar = document.querySelector('.sidebar');
        if (!sidebar || sidebar.dataset.unifiedNavigation === 'true') return;
        if (!document.getElementById('unifiedNavigationStyles')) {
            var style = document.createElement('style');
            style.id = 'unifiedNavigationStyles';
            style.textContent = '.unified-menu-title{padding-top:8px;margin-bottom:4px}.nav-section{margin:4px 0;border-radius:9px}.nav-section>summary{display:flex!important;align-items:center!important;gap:8px!important;min-height:43px;padding:9px 10px;border-radius:8px;color:#dbe7f5;cursor:pointer;list-style:none!important;font-size:12px;font-weight:650}.nav-section>summary::-webkit-details-marker{display:none!important}.nav-section>summary::marker{content:""!important;display:none!important}.nav-section>summary:hover,.nav-section[open]>summary{background:rgba(77,126,181,.36);color:#fff}.nav-section>summary>i:first-child{flex:0 0 22px;font-size:14px;text-align:center}.nav-section>summary>span{min-width:0;flex:1;line-height:1.3}.nav-chevron{margin-left:auto;font-size:9px;transition:transform .18s}.nav-section[open] .nav-chevron{transform:rotate(180deg)}.nav-section-items{display:grid;gap:2px;padding:4px 0 4px 12px}.nav-section:not([open]) .nav-section-items{display:none}.nav-section-items .menu-item{min-height:38px;margin:0;padding:8px 10px;font-size:12px;border-radius:7px}.nav-section-items .menu-item i{flex:0 0 22px;margin-right:7px;font-size:12px}.nav-section-items .menu-item.active{background:#356ba5;color:#fff;box-shadow:none}@media(max-width:900px){.nav-section summary span,.nav-section summary .nav-chevron{display:none}.nav-section>summary{justify-content:center;padding:10px}.nav-section-items{padding-left:0}}';
            document.head.appendChild(style);
        }
        var role = detectRole(sidebar);
        var groups = navigationByRole[role];
        if (!groups) return;
        var nav = sidebar.querySelector('.menu');
        if (!nav) return;
        var contextPath = '/' + location.pathname.split('/')[1];
        var current = normalizePath(location.pathname);
        var roleNames = {HR: 'Phòng Nhân sự (HR)', MENTOR: 'Không gian Mentor', INTERN: 'Không gian Thực tập sinh', ADMIN: 'Quản trị hệ thống'};
        var logoTitle = sidebar.querySelector('.logo h2');
        var logoRole = sidebar.querySelector('.logo span');
        if (logoTitle) logoTitle.textContent = 'IMS Portal';
        if (logoRole) logoRole.textContent = roleNames[role];
        nav.innerHTML = '';
        var heading = document.createElement('p');
        heading.className = 'menu-title unified-menu-title';
        heading.textContent = 'CHỨC NĂNG ' + roleNames[role].toUpperCase();
        nav.appendChild(heading);
        groups.forEach(function (group, groupIndex) {
            var hasActive = group[2].some(function (item) { return isActive(current, item[1]); });
            var section = document.createElement('details');
            section.className = 'nav-section';
            section.open = hasActive;
            section.addEventListener('toggle', function () {
                if (!this.open) return;
                var openedSection = this;
                nav.querySelectorAll('.nav-section[open]').forEach(function (other) {
                    if (other !== openedSection) other.open = false;
                });
            });
            var summary = document.createElement('summary');
            summary.innerHTML = '<i class="fa-solid ' + group[1] + '"></i><span>' + group[0] + '</span><i class="fa-solid fa-chevron-down nav-chevron"></i>';
            summary.setAttribute('aria-label', group[0]);
            section.appendChild(summary);
            var items = document.createElement('div');
            items.className = 'nav-section-items';
            group[2].forEach(function (item) {
                var link = document.createElement('a');
                link.href = contextPath + item[1];
                link.className = 'menu-item' + (isActive(current, item[1]) ? ' active' : '');
                link.innerHTML = '<i class="fa-solid ' + item[2] + '"></i><span>' + item[0] + '</span>';
                items.appendChild(link);
            });
            section.appendChild(items);
            nav.appendChild(section);
        });
        sidebar.dataset.unifiedNavigation = 'true';
    }

    buildUnifiedNavigation();

    function closeAll(exceptMenu) {
        document.querySelectorAll('.account-dropdown,.notification-menu').forEach(function (item) {
            if (item !== exceptMenu) item.hidden = true;
        });
        document.querySelectorAll('#accountMenuButton,#notificationButton').forEach(function (button) {
            var controlledMenu = button.id === 'accountMenuButton'
                ? document.getElementById('accountMenu')
                : document.getElementById('notificationMenu');
            if (controlledMenu !== exceptMenu) button.setAttribute('aria-expanded', 'false');
        });
    }

    function setup(buttonId, menuId, wrapperSelector) {
        var button = document.getElementById(buttonId);
        var menu = document.getElementById(menuId);
        if (!button || !menu || button.dataset.menuReady === 'true') return;
        button.dataset.menuReady = 'true';
        button.setAttribute('aria-controls', menuId);
        button.addEventListener('click', function (event) {
            event.stopPropagation();
            var opening = menu.hidden;
            closeAll(opening ? menu : null);
            menu.hidden = !opening;
            button.setAttribute('aria-expanded', String(opening));
        });
        button.addEventListener('keydown', function (event) {
            if (event.key === 'Escape') {
                closeAll(null);
                button.focus();
            }
        });
        document.addEventListener('click', function (event) {
            if (!event.target.closest(wrapperSelector)) {
                menu.hidden = true;
                button.setAttribute('aria-expanded', 'false');
            }
        });
    }
    setup('accountMenuButton', 'accountMenu', '.account-menu');
    setup('notificationButton', 'notificationMenu', '.notification-wrap');
    document.addEventListener('keydown', function (event) {
        if (event.key === 'Escape') closeAll(null);
    });
})();
