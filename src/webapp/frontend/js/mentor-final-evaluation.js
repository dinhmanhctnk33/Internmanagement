(() => {
  const form = document.getElementById('evaluationForm');
  if (!form) return;

  const criteria = ['knowledge', 'problemSolving', 'initiative', 'teamwork'];
  const getScore = name => {
    const selected = form.querySelector(`input[name="${name}"]:checked`);
    return selected ? Number(selected.value) : null;
  };
  const average = values => {
    const scores = values.filter(value => value !== null);
    return scores.length
      ? (scores.reduce((total, score) => total + score, 0) / scores.length).toFixed(1)
      : '—';
  };
  const refreshScores = () => {
    const scores = criteria.map(getScore);
    document.getElementById('skillScore').textContent = average(scores.slice(0, 2));
    document.getElementById('attitudeScore').textContent = average(scores.slice(2, 4));
    document.getElementById('overallScore').textContent = average(scores);
  };
  const showError = message => {
    const box = document.getElementById('formMessage');
    box.textContent = message;
    box.className = 'form-message visible error';
  };

  form.addEventListener('change', refreshScores);
  form.addEventListener('submit', event => {
    if (event.submitter?.value !== 'finalize') return;

    const missingCriterion = criteria.find(name => getScore(name) === null);
    if (missingCriterion) {
      event.preventDefault();
      showError('Vui lòng chấm điểm đủ cả 4 tiêu chí trước khi chốt.');
      form.querySelector(`input[name="${missingCriterion}"]`).focus();
      return;
    }

    const comment = document.getElementById('generalComment');
    if (comment.value.trim().length < 5) {
      event.preventDefault();
      showError('Nhận xét chung cần có ít nhất 5 ký tự khi chốt đánh giá.');
      comment.focus();
      return;
    }

    if (!window.confirm('Bạn có chắc chắn muốn chốt đánh giá? Phiên bản trước sẽ được lưu trong lịch sử.')) {
      event.preventDefault();
    }
  });

  document.querySelector('.evaluation-filters')?.addEventListener('change', event => {
    if (event.target.matches('#status, [name="eligibleOnly"]')) {
      event.target.form.requestSubmit();
    }
  });
  refreshScores();
})();
