(function () {
    const modal = document.getElementById('deleteModal');
    const idInput = document.getElementById('deleteId');
    const groupName = document.getElementById('deleteGroupName');
    const cancel = document.getElementById('cancelDelete');
    if (!modal) return;

    function closeModal() {
        modal.hidden = true;
        document.body.classList.remove('modal-open');
    }

    document.querySelectorAll('.delete-trigger').forEach(function (button) {
        button.addEventListener('click', function () {
            idInput.value = button.dataset.id;
            groupName.textContent = button.dataset.name;
            modal.hidden = false;
            document.body.classList.add('modal-open');
            cancel.focus();
        });
    });
    cancel.addEventListener('click', closeModal);
    modal.addEventListener('click', function (event) { if (event.target === modal) closeModal(); });
    document.addEventListener('keydown', function (event) { if (event.key === 'Escape' && !modal.hidden) closeModal(); });
}());
