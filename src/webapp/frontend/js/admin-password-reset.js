(() => {
    const modal = document.getElementById('passwordResetModal');
    const dialogForm = document.getElementById('passwordResetDialogForm');
    const usernameLabel = document.getElementById('passwordResetUsername');
    const passwordInput = document.getElementById('adminNewPassword');
    const confirmInput = document.getElementById('adminConfirmPassword');
    const error = document.getElementById('passwordResetError');
    let targetForm = null;

    if (!modal || !dialogForm) return;

    const closeModal = () => {
        modal.hidden = true;
        document.body.classList.remove('modal-open');
        targetForm = null;
        dialogForm.reset();
        error.textContent = '';
    };

    window.resetUserPassword = form => {
        targetForm = form;
        usernameLabel.textContent = form.dataset.username || 'tài khoản này';
        dialogForm.reset();
        error.textContent = '';
        modal.hidden = false;
        document.body.classList.add('modal-open');
        window.setTimeout(() => passwordInput.focus(), 0);
        return false;
    };

    modal.querySelectorAll('[data-close-reset-modal]').forEach(button => {
        button.addEventListener('click', closeModal);
    });

    modal.querySelectorAll('[data-toggle-password]').forEach(button => {
        button.addEventListener('click', () => {
            const input = button.previousElementSibling;
            const visible = input.type === 'text';
            input.type = visible ? 'password' : 'text';
            button.setAttribute('aria-label', visible ? 'Hiện mật khẩu' : 'Ẩn mật khẩu');
            button.querySelector('i').className = visible ? 'fa-regular fa-eye' : 'fa-regular fa-eye-slash';
        });
    });

    dialogForm.addEventListener('submit', event => {
        event.preventDefault();
        const password = passwordInput.value;
        if (password.length < 6) {
            error.textContent = 'Mật khẩu mới phải có ít nhất 6 ký tự.';
            passwordInput.focus();
            return;
        }
        if (password !== confirmInput.value) {
            error.textContent = 'Mật khẩu xác nhận không khớp.';
            confirmInput.focus();
            return;
        }
        if (!targetForm) return;
        targetForm.querySelector('input[name="newPassword"]').value = password;
        targetForm.submit();
    });

    document.addEventListener('keydown', event => {
        if (event.key === 'Escape' && !modal.hidden) closeModal();
    });
})();
