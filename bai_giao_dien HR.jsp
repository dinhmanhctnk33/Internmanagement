<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title>HR - Phân công thực tập sinh cho Mentor</title>
    <style>
*{box-sizing:border-box}
body{margin:0;font:14px Arial,sans-serif;background:#f5f7fb;color:#172b4d}
.page{max-width:1250px;margin:auto;padding:28px}
.topbar{display:flex;justify-content:space-between;align-items:center;margin-bottom:22px}
h1{margin:0 0 7px;font-size:26px}.topbar p{margin:0;color:#627d98}
.card,.stat{background:#fff;border:1px solid #e1e7ef;border-radius:10px}
.summary{display:grid;grid-template-columns:repeat(3,1fr);gap:16px;margin-bottom:18px}
.stat{padding:18px}.stat span{display:block;color:#627d98;margin-bottom:7px}.stat b{font-size:25px}
.toolbar{display:flex;gap:10px;padding:16px;border-bottom:1px solid #e8edf3}
input,select{border:1px solid #cbd5e1;border-radius:6px;padding:10px 12px;background:#fff}
.toolbar input{flex:1}.toolbar select{width:190px}
.table-wrap{overflow:auto}table{width:100%;border-collapse:collapse}
th,td{text-align:left;padding:14px 16px;border-bottom:1px solid #edf0f4}th{background:#fafbfd;color:#52606d;font-size:13px}
.load{display:inline-block;background:#eaf2ff;color:#1868db;padding:5px 9px;border-radius:14px}.before{background:#f1f5f9;color:#52606d}
.badge{padding:5px 9px;border-radius:14px;background:#e8f7ee;color:#237a4b}.badge.full{background:#fff0f0;color:#c53030}
.btn{border:1px solid #cbd5e1;background:#fff;border-radius:6px;padding:10px 15px;cursor:pointer}.primary{background:#1868db;color:#fff;border-color:#1868db}
.link{border:0;background:none;color:#1868db;cursor:pointer}.empty{text-align:center;color:#718096;padding:30px}
.modal{display:none;position:fixed;inset:0;background:#0006;align-items:center;justify-content:center}.modal.show{display:flex}
.modal-box{width:430px;background:#fff;border-radius:10px;padding:22px;box-shadow:0 12px 40px #0003}.modal-head{display:flex;justify-content:space-between;align-items:center;margin-bottom:18px}
.modal-head h2{margin:0}.close{border:0;background:none;font-size:25px;cursor:pointer}
.modal-box label{display:block;margin:12px 0 6px;font-weight:bold}.modal-box input,.modal-box select{width:100%}
.actions{display:flex;justify-content:flex-end;gap:10px;margin-top:20px}
@media(max-width:700px){.page{padding:15px}.summary{grid-template-columns:1fr}.topbar{align-items:flex-start;gap:15px;flex-direction:column}.toolbar{flex-direction:column}.toolbar select{width:100%}}

    </style>
</head>
<body>
<div class="page">
    <header class="topbar">
        <div>
            <h1>Phân công thực tập sinh cho Mentor</h1>
            <p>Quản lý tải trước sau và phân công thực tập sinh cho mentor.</p>
        </div>
        <button class="btn primary" onclick="openAssign()">+ Phân công</button>
    </header>

    <section class="summary">
        <div class="stat"><span>Tổng mentor</span><b id="totalMentor">0</b></div>
        <div class="stat"><span>Đang hướng dẫn</span><b id="totalIntern">0</b></div>
        <div class="stat"><span>Còn chỗ</span><b id="totalSlot">0</b></div>
    </section>

    <section class="card">
        <div class="toolbar">
            <input id="search" type="text" placeholder="Tìm mentor, email, phòng ban..." oninput="render()">
            <select id="status" onchange="render()">
                <option value="">Tất cả trạng thái</option>
                <option value="Còn chỗ">Còn chỗ</option>
                <option value="Đầy">Đầy</option>
            </select>
        </div>

        <div class="table-wrap">
            <table>
                <thead>
                <tr>
                    <th>Mentor</th>
                    <th>Email</th>
                    <th>Phòng ban</th>
                    <th>Tải trước</th>
                    <th>Tải sau</th>
                    <th>Trạng thái</th>
                    <th>Thao tác</th>
                </tr>
                </thead>
                <tbody id="mentorTable"></tbody>
            </table>
        </div>
    </section>
</div>

<div id="modal" class="modal">
    <div class="modal-box">
        <div class="modal-head">
            <h2>Phân công thực tập sinh</h2>
            <button class="close" onclick="closeAssign()">×</button>
        </div>
        <label>Mentor</label>
        <select id="mentorSelect"></select>
        <label>Tên thực tập sinh</label>
        <input id="internName" placeholder="Nhập tên thực tập sinh">
        <label>Email thực tập sinh</label>
        <input id="internEmail" placeholder="Nhập email">
        <div class="actions">
            <button class="btn" onclick="closeAssign()">Hủy</button>
            <button class="btn primary" onclick="assignIntern()">Lưu phân công</button>
        </div>
    </div>
</div>

<script>
const mentors = [
    {id:1,name:"Nguyễn Văn An",email:"an@company.vn",dept:"IT",before:2,after:3,max:5},
    {id:2,name:"Trần Thị Bình",email:"binh@company.vn",dept:"HR",before:3,after:5,max:5},
    {id:3,name:"Lê Minh Khoa",email:"khoa@company.vn",dept:"Business",before:1,after:2,max:4},
    {id:4,name:"Phạm Hoàng Nam",email:"nam@company.vn",dept:"IT",before:4,after:4,max:4}
];

function statusOf(m){ return m.after >= m.max ? "Đầy" : "Còn chỗ"; }

function render(){
    const q = document.getElementById("search").value.toLowerCase();
    const st = document.getElementById("status").value;
    const rows = mentors.filter(m =>
        (!q || `${m.name} ${m.email} ${m.dept}`.toLowerCase().includes(q)) &&
        (!st || statusOf(m) === st)
    );

    document.getElementById("mentorTable").innerHTML = rows.map(m => `
        <tr>
            <td><b>${m.name}</b></td>
            <td>${m.email}</td>
            <td>${m.dept}</td>
            <td><span class="load before">${m.before}/${m.max}</span></td>
            <td><span class="load">${m.after}/${m.max}</span></td>
            <td><span class="badge ${statusOf(m)==="Đầy"?"full":""}">${statusOf(m)}</span></td>
            <td><button class="link" onclick="openAssign(${m.id})">Phân công</button></td>
        </tr>
    `).join("") || `<tr><td colspan="7" class="empty">Không tìm thấy mentor.</td></tr>`;

    document.getElementById("totalMentor").textContent = mentors.length;
    document.getElementById("totalIntern").textContent = mentors.reduce((s,m)=>s+m.after,0);
    document.getElementById("totalSlot").textContent = mentors.reduce((s,m)=>s+Math.max(m.max-m.after,0),0);
}

function openAssign(id){
    const select = document.getElementById("mentorSelect");
    select.innerHTML = mentors.filter(m => m.after < m.max)
        .map(m => `<option value="${m.id}" ${m.id===id?"selected":""}>${m.name} - còn ${m.max-m.after} chỗ</option>`).join("");
    if(!select.options.length){ alert("Tất cả mentor đã đủ tải."); return; }
    document.getElementById("internName").value = "";
    document.getElementById("internEmail").value = "";
    document.getElementById("modal").classList.add("show");
}
function closeAssign(){ document.getElementById("modal").classList.remove("show"); }

function assignIntern(){
    const id = Number(document.getElementById("mentorSelect").value);
    const name = document.getElementById("internName").value.trim();
    const email = document.getElementById("internEmail").value.trim();
    const m = mentors.find(x=>x.id===id);
    if(!name || !email){ alert("Vui lòng nhập đầy đủ thông tin thực tập sinh."); return; }
    if(!m || m.after >= m.max){ alert("Mentor đã đủ tải."); return; }
    m.after++;
    closeAssign();
    render();
    alert("Đã phân công " + name + " cho " + m.name + ".");
}
render();
</script>
</body>
</html>
