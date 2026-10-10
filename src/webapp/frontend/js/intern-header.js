(function () {
    function setup(buttonId, menuId, wrapperSelector) {
        var button = document.getElementById(buttonId);
        var menu = document.getElementById(menuId);
        if (!button || !menu) return;
        button.addEventListener('click', function (event) {
            event.stopPropagation();
            var opening = menu.hidden;
            document.querySelectorAll('.account-dropdown,.notification-menu').forEach(function (item) { item.hidden = true; });
            menu.hidden = !opening;
            button.setAttribute('aria-expanded', String(opening));
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
})();
