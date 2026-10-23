[CmdletBinding()]
param()

$ErrorActionPreference = 'Stop'
& (Join-Path $PSScriptRoot 'setup-intern-demo.ps1')
. (Join-Path $PSScriptRoot 'setup-demo-common.ps1')

$sql = @'
USE internship_management;
SET @admin_user_id=(SELECT id FROM users WHERE username='admin_demo' LIMIT 1);
DROP TEMPORARY TABLE IF EXISTS demo_numbers;
CREATE TEMPORARY TABLE demo_numbers(n INT PRIMARY KEY);
INSERT INTO demo_numbers VALUES (1),(2),(3),(4),(5),(6),(7),(8),(9),(10);

INSERT IGNORE INTO users(username,password,email,full_name,status,create_at,update_at)
SELECT CONCAT('account_demo_',LPAD(n,2,'0')),'123456',CONCAT('account.demo',LPAD(n,2,'0'),'@ims.local'),
       CONCAT('Tài khoản Demo ',LPAD(n,2,'0')),ELT(MOD(n-1,3)+1,'ACTIVE','INACTIVE','LOCKED'),CURDATE(),CURDATE() FROM demo_numbers;

INSERT IGNORE INTO user_roles(user_id,role_id)
SELECT u.id,r.id FROM demo_numbers d
JOIN users u ON u.username=CONCAT('account_demo_',LPAD(d.n,2,'0'))
JOIN roles r ON r.role_code=ELT(MOD(d.n-1,4)+1,'HR','MENTOR','INTERN','CANDIDATE');

INSERT IGNORE INTO permissions(permission_code,permission_name,module_group) VALUES
('APPLICATION_REVIEW','Xét duyệt hồ sơ đăng ký','APPLICATION'),
('APPLICATION_SUBMIT','Nộp hồ sơ đăng ký','APPLICATION'),
('PROGRAM_MANAGE','Quản lý chương trình thực tập','PROGRAM'),
('CONTRACT_MANAGE','Quản lý hợp đồng','CONTRACT'),
('MENTOR_ASSIGN','Phân công mentor','MENTOR'),
('ATTENDANCE_REPORT','Xem báo cáo chấm công','ATTENDANCE'),
('ATTENDANCE_USE','Sử dụng chức năng chấm công','ATTENDANCE');

INSERT IGNORE INTO role_permissions(role_id,permission_id)
SELECT r.id,p.id FROM roles r CROSS JOIN permissions p WHERE r.role_code='ADMIN';

INSERT INTO audit_logs(user_id,action_code,table_name,record_id,new_values_json,ip_address,created_at)
SELECT @admin_user_id,CONCAT('ADMIN_DEMO_',LPAD(n,2,'0')),'users',u.id,
       JSON_OBJECT('username',u.username,'status',u.status),'127.0.0.1',DATE_SUB(NOW(),INTERVAL n HOUR)
FROM demo_numbers d JOIN users u ON u.username=CONCAT('account_demo_',LPAD(d.n,2,'0'))
WHERE NOT EXISTS(SELECT 1 FROM audit_logs a WHERE a.action_code=CONCAT('ADMIN_DEMO_',LPAD(d.n,2,'0')));

INSERT INTO notifications(user_id,title,message,target_url,is_read)
SELECT @admin_user_id,CONCAT('Thông báo Admin demo ',LPAD(n,2,'0')),CONCAT('Có thay đổi quản trị số ',n,'.'),
       ELT(MOD(n-1,3)+1,'/admin/users','/admin/accounts','/admin/permissions'),n<=3 FROM demo_numbers d
WHERE NOT EXISTS(SELECT 1 FROM notifications x WHERE x.user_id=@admin_user_id AND x.title=CONCAT('Thông báo Admin demo ',LPAD(d.n,2,'0')));

DROP TEMPORARY TABLE demo_numbers;
SELECT 'ADMIN_DEMO_READY' AS result,@admin_user_id AS admin_user_id;
'@

Invoke-DemoSql -Sql $sql -Label 'Admin'
Write-Host 'Admin demo data is ready. Login: admin_demo / 123456' -ForegroundColor Green
