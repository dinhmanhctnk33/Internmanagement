(() => {
    const modal = document.getElementById('assignmentModal');
    if (!modal) return;
    const profileInput = document.getElementById('assignmentProfileId');
    const mentorInput = document.getElementById('assignmentMentorId');
    const internName = document.getElementById('selectedInternName');
    const internMeta = document.getElementById('selectedInternMeta');
    const confirmButton = modal.querySelector('.confirm-assignment');
    const emptyMessage = modal.querySelector('.no-compatible-mentor');
    const choices = [...modal.querySelectorAll('[data-mentor-choice]')];

    const close = () => {
        modal.hidden = true;
        modal.classList.remove('open');
        document.body.classList.remove('modal-lock');
    };

    document.querySelectorAll('.open-assignment').forEach(button => button.addEventListener('click', () => {
        const departmentId = button.dataset.departmentId || '';
        const currentMentorId = button.dataset.mentorId || '';
        profileInput.value = button.dataset.profileId;
        mentorInput.value = '';
        internName.textContent = button.dataset.internName;
        internMeta.textContent = `${button.dataset.internCode} · ${button.dataset.programName}`;
        let compatibleCount = 0;
        choices.forEach(choice => {
            const sameDepartment = !departmentId || !choice.dataset.departmentId || choice.dataset.departmentId === departmentId;
            const hasCapacity = Number(choice.dataset.load) < Number(choice.dataset.capacity) || choice.dataset.mentorChoice === currentMentorId;
            choice.hidden = !(sameDepartment && hasCapacity);
            choice.classList.remove('selected');
            if (!choice.hidden) compatibleCount++;
        });
        emptyMessage.hidden = compatibleCount > 0;
        confirmButton.disabled = true;
        modal.hidden = false;
        modal.classList.add('open');
        document.body.classList.add('modal-lock');
    }));

    choices.forEach(choice => choice.addEventListener('click', () => {
        choices.forEach(item => item.classList.remove('selected'));
        choice.classList.add('selected');
        mentorInput.value = choice.dataset.mentorChoice;
        confirmButton.disabled = false;
    }));

    modal.querySelectorAll('[data-close-assignment]').forEach(button => button.addEventListener('click', close));
    document.addEventListener('keydown', event => { if (event.key === 'Escape' && !modal.hidden) close(); });
    document.getElementById('assignmentForm').addEventListener('submit', () => { confirmButton.disabled = true; });
})();
