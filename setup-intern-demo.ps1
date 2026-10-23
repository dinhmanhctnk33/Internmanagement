[CmdletBinding()]
param()

$ErrorActionPreference = 'Stop'
& (Join-Path $PSScriptRoot 'setup-hr-demo.ps1')
. (Join-Path $PSScriptRoot 'setup-demo-common.ps1')

$sql = @'
USE internship_management;
SET @intern_user_id=(SELECT id FROM users WHERE username='intern_demo' LIMIT 1);
SET @intern_id=(SELECT id FROM intern_profiles WHERE user_id=@intern_user_id LIMIT 1);
SET @mentor_id=(SELECT mentor_id FROM intern_profiles WHERE id=@intern_id);
DROP TEMPORARY TABLE IF EXISTS demo_numbers;
CREATE TEMPORARY TABLE demo_numbers(n INT PRIMARY KEY);
INSERT INTO demo_numbers VALUES (1),(2),(3),(4),(5),(6),(7),(8),(9),(10);

INSERT INTO tasks(task_title,description,mentor_id,intern_profile_id,due_date,priority,completion_percent,status,intern_note,progress_updated_at)
SELECT CONCAT('Công việc cá nhân demo ',LPAD(n,2,'0')),'Dữ liệu trình diễn cho tài khoản intern_demo.',@mentor_id,@intern_id,
       DATE_ADD(NOW(),INTERVAL n DAY),ELT(MOD(n-1,4)+1,'LOW','MEDIUM','HIGH','URGENT'),
       ELT(MOD(n-1,4)+1,0,35,70,100),ELT(MOD(n-1,4)+1,'TODO','IN_PROGRESS','IN_PROGRESS','COMPLETED'),
       CONCAT('Ghi chú tiến độ demo ',n,'.'),NOW()
FROM demo_numbers d WHERE NOT EXISTS(SELECT 1 FROM tasks t WHERE t.intern_profile_id=@intern_id AND t.task_title=CONCAT('Công việc cá nhân demo ',LPAD(d.n,2,'0')));

INSERT INTO weekly_reports(intern_profile_id,week_number,start_date,end_date,completed_work,key_learnings,obstacles,mentor_score,mentor_feedback,status)
SELECT @intern_id,n,DATE_SUB(CURDATE(),INTERVAL (11-n)*7 DAY),DATE_SUB(CURDATE(),INTERVAL ((11-n)*7-6) DAY),
       CONCAT('Kết quả công việc tuần ',n,'.'),'Kỹ năng Java, SQL và làm việc nhóm.','Không có trở ngại lớn.',
       IF(n<=6,4.0+n/10,NULL),IF(n<=6,'Tiến độ tốt.',NULL),IF(n<=6,'APPROVED','SUBMITTED') FROM demo_numbers WHERE TRUE
ON DUPLICATE KEY UPDATE completed_work=VALUES(completed_work),mentor_score=VALUES(mentor_score),mentor_feedback=VALUES(mentor_feedback),status=VALUES(status);

INSERT INTO intern_documents(intern_profile_id,document_type,file_name,file_url,file_size_bytes,approval_status,rejection_note,uploaded_at)
SELECT @intern_id,ELT(MOD(n-1,3)+1,'CV','STUDENT_CARD','TRANSCRIPT'),CONCAT('intern-demo-',LPAD(n,2,'0'),'.pdf'),
       CONCAT('/uploads/demo/intern-',LPAD(n,2,'0'),'.pdf'),400000+n*10000,ELT(MOD(n-1,3)+1,'PENDING','APPROVED','REJECTED'),
       IF(MOD(n-1,3)=2,'Vui lòng tải lại tài liệu rõ hơn.',NULL),DATE_SUB(NOW(),INTERVAL n DAY)
FROM demo_numbers d WHERE NOT EXISTS(SELECT 1 FROM intern_documents x WHERE x.file_name=CONCAT('intern-demo-',LPAD(d.n,2,'0'),'.pdf'));

INSERT INTO contracts(contract_number,intern_profile_id,effective_date,expiration_date,monthly_allowance,contract_pdf_url,signature_status,signed_at)
SELECT CONCAT('HD-INTERN-DEMO-',LPAD(n,2,'0')),@intern_id,DATE_SUB(CURDATE(),INTERVAL n MONTH),DATE_ADD(CURDATE(),INTERVAL (11-n) MONTH),
       3000000+n*100000,CONCAT('/uploads/demo/intern-contract-',LPAD(n,2,'0'),'.pdf'),ELT(MOD(n-1,4)+1,'DRAFT','SENT','SIGNED','EXPIRED'),
       IF(MOD(n-1,4)=2,NOW(),NULL) FROM demo_numbers WHERE TRUE
ON DUPLICATE KEY UPDATE signature_status=VALUES(signature_status),signed_at=VALUES(signed_at);

INSERT INTO attendance_records(intern_profile_id,work_date,check_in_time,check_out_time,ip_address,status)
SELECT @intern_id,DATE_SUB(CURDATE(),INTERVAL (20+n) DAY),TIMESTAMP(DATE_SUB(CURDATE(),INTERVAL (20+n) DAY),'08:00:00'),
       TIMESTAMP(DATE_SUB(CURDATE(),INTERVAL (20+n) DAY),'17:00:00'),'127.0.0.1',IF(MOD(n,4)=0,'LATE','ON_TIME') FROM demo_numbers WHERE TRUE
ON DUPLICATE KEY UPDATE check_in_time=VALUES(check_in_time),check_out_time=VALUES(check_out_time),status=VALUES(status);

INSERT INTO leave_requests(intern_profile_id,leave_type,start_date,end_date,total_days,reason,approval_status,approver_notes)
SELECT @intern_id,ELT(MOD(n-1,3)+1,'SICK_LEAVE','PERSONAL_LEAVE','UNPAID_LEAVE'),DATE_ADD(CURDATE(),INTERVAL n DAY),
       DATE_ADD(CURDATE(),INTERVAL n DAY),1,CONCAT('[INTERN-DEMO-',LPAD(n,2,'0'),'] Đơn nghỉ phép mẫu.'),
       ELT(MOD(n-1,3)+1,'PENDING','APPROVED','REJECTED'),IF(MOD(n-1,3)=0,NULL,'Phản hồi mẫu từ HR.') FROM demo_numbers d
WHERE NOT EXISTS(SELECT 1 FROM leave_requests x WHERE x.reason LIKE CONCAT('[INTERN-DEMO-',LPAD(d.n,2,'0'),']%'));

INSERT INTO allowance_payouts(intern_profile_id,pay_period,actual_work_days,base_allowance,bonus_allowance,net_amount,payment_status,paid_at)
SELECT @intern_id,DATE_FORMAT(DATE_SUB(CURDATE(),INTERVAL n MONTH),'%Y-%m'),20,3000000,n*50000,3000000+n*50000,
       ELT(MOD(n-1,4)+1,'PENDING','PROCESSING','PAID','FAILED'),IF(MOD(n-1,4)=2,NOW(),NULL) FROM demo_numbers WHERE TRUE
ON DUPLICATE KEY UPDATE net_amount=VALUES(net_amount),payment_status=VALUES(payment_status),paid_at=VALUES(paid_at);

INSERT INTO support_tickets(ticket_code,intern_profile_id,ticket_category,subject,description,hr_response,status)
SELECT CONCAT('TK-INTERN-',LPAD(n,2,'0')),@intern_id,'DOCUMENT',CONCAT('Hỗ trợ cá nhân demo ',LPAD(n,2,'0')),
       'Yêu cầu hỗ trợ mẫu của thực tập sinh.',IF(n<=5,'HR đã phản hồi.',NULL),
       ELT(MOD(n-1,4)+1,'OPEN','IN_PROGRESS','RESOLVED','CLOSED') FROM demo_numbers WHERE TRUE
ON DUPLICATE KEY UPDATE status=VALUES(status),hr_response=VALUES(hr_response);

INSERT INTO meeting_recipients(meeting_id,intern_profile_id,recipient_name,recipient_email,is_active,last_email_status,last_email_type)
SELECT m.id,@intern_id,u.full_name,u.email,TRUE,'PENDING','INVITATION' FROM demo_numbers d
JOIN meetings m ON m.title=CONCAT('Họp demo ',LPAD(d.n,2,'0')) JOIN users u ON u.id=@intern_user_id
WHERE TRUE
ON DUPLICATE KEY UPDATE is_active=TRUE,recipient_name=VALUES(recipient_name),recipient_email=VALUES(recipient_email);

INSERT INTO notifications(user_id,title,message,target_url,is_read)
SELECT @intern_user_id,CONCAT('Thông báo Intern demo ',LPAD(n,2,'0')),CONCAT('Có cập nhật cá nhân số ',n,'.'),
       ELT(MOD(n-1,4)+1,'/intern/progress','/attendance','/leave-requests','/my-calendar'),n<=3 FROM demo_numbers d
WHERE NOT EXISTS(SELECT 1 FROM notifications x WHERE x.user_id=@intern_user_id AND x.title=CONCAT('Thông báo Intern demo ',LPAD(d.n,2,'0')));

DROP TEMPORARY TABLE demo_numbers;
SELECT 'INTERN_DEMO_READY' AS result,@intern_id AS intern_profile_id;
'@

Invoke-DemoSql -Sql $sql -Label 'Intern'
Write-Host 'Intern demo data is ready. Login: intern_demo / 123456' -ForegroundColor Green
