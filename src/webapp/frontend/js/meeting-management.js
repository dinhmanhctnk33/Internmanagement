(function(){
 const modal=document.getElementById('meetingModal'),form=document.getElementById('meetingForm');
 const open=()=>{modal.classList.add('open');modal.setAttribute('aria-hidden','false');document.body.classList.add('modal-lock')};
 const close=()=>{modal.classList.remove('open');modal.setAttribute('aria-hidden','true');document.body.classList.remove('modal-lock');if(location.search.includes('edit='))location.href=location.pathname};
 document.querySelectorAll('[data-open-form]').forEach(x=>x.addEventListener('click',open));document.querySelectorAll('[data-close-form]').forEach(x=>x.addEventListener('click',close));
 if(modal.classList.contains('open'))document.body.classList.add('modal-lock');
 const mode=document.getElementById('meetingMode'),placeLabel=document.getElementById('placeLabel'),place=form.elements.locationOrLink;
 function syncMode(){const online=mode.value==='ONLINE';placeLabel.textContent=(online?'Liên kết họp':'Địa điểm họp')+' *';place.placeholder=online?'https://meet...':'Phòng họp, tầng...'}mode.addEventListener('change',syncMode);syncMode();
 const boxes=[...form.querySelectorAll('[name=recipientIds]')],count=document.getElementById('selectedCount');function syncCount(){count.textContent=boxes.filter(x=>x.checked).length}boxes.forEach(x=>x.addEventListener('change',syncCount));syncCount();
 const search=document.getElementById('recipientSearch');search.addEventListener('input',()=>{const q=search.value.trim().toLowerCase();document.querySelectorAll('[data-recipient]').forEach(x=>x.hidden=!x.dataset.recipient.toLowerCase().includes(q))});
 document.getElementById('selectAll').addEventListener('click',()=>{document.querySelectorAll('[data-recipient]:not([hidden]) input').forEach(x=>x.checked=true);syncCount()});
 form.addEventListener('submit',e=>{if(e.submitter&&e.submitter.dataset.confirmCancel&&!confirm('Hủy lịch sẽ gỡ lịch khỏi người nhận và gửi email hủy. Tiếp tục?')){e.preventDefault();return}if(!(e.submitter&&e.submitter.formNoValidate)&&boxes.every(x=>!x.checked)){e.preventDefault();alert('Vui lòng chọn ít nhất một thực tập sinh.');return}const start=form.elements.startTime.value,end=form.elements.endTime.value;if(start&&end&&end<=start){e.preventDefault();alert('Giờ kết thúc phải sau giờ bắt đầu.')}});
 document.addEventListener('keydown',e=>{if(e.key==='Escape'&&modal.classList.contains('open'))close()});
})();
