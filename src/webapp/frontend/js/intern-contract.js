document.addEventListener('DOMContentLoaded', () => {
    const openPdf = document.getElementById('openContractPdf');
    const agreement = document.getElementById('contractAgreement');
    const confirmButton = document.getElementById('confirmContractButton');
    const viewedStatus = document.getElementById('contractViewedStatus');

    if (!agreement || !confirmButton) return;

    const updateSubmitState = () => {
        confirmButton.disabled = agreement.disabled || !agreement.checked;
    };

    agreement.addEventListener('change', updateSubmitState);

    if (openPdf) {
        openPdf.addEventListener('click', () => {
            agreement.disabled = false;
            if (viewedStatus) {
                viewedStatus.classList.add('complete');
                const icon = viewedStatus.querySelector('i');
                const text = viewedStatus.querySelector('span');
                if (icon) icon.className = 'fa-solid fa-check';
                if (text) text.textContent = 'Đã mở';
            }
            updateSubmitState();
        });
    }

    updateSubmitState();
});
