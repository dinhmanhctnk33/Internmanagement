(function () {
    var dialog = document.getElementById('taskDetailDialog');
    if (!dialog) return;
    var title = document.getElementById('taskDetailTitle');
    var description = document.getElementById('taskDetailDescription');
    var lastTrigger = null;
    function closeDialog() {
        dialog.close();
        if (lastTrigger) lastTrigger.focus();
    }
    document.querySelectorAll('.task-detail-trigger').forEach(function (trigger) {
        trigger.addEventListener('click', function () {
            var task = trigger.closest('.task');
            title.textContent = task.querySelector('.task-title').textContent.trim();
            description.textContent = task.querySelector('.task-description').textContent.trim() || 'Nhiệm vụ này chưa có mô tả chi tiết.';
            lastTrigger = trigger;
            dialog.showModal();
        });
    });
    dialog.querySelector('.task-detail-close').addEventListener('click', closeDialog);
    dialog.querySelector('.task-detail-done').addEventListener('click', closeDialog);
    dialog.addEventListener('click', function (event) {
        if (event.target === dialog) closeDialog();
    });
}());
