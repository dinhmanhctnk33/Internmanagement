<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Báo Cáo Tổng Hợp Đánh Giá Cuối Kỳ - HR Portal</title>
    <!-- Bootstrap 5 CSS -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <!-- FontAwesome Icons -->
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
    
    <style>
        :root {
            --primary-color: #0d6efd;
            --secondary-bg: #f8f9fa;
        }
        body { 
            background-color: #f4f6f9; 
            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; 
            color: #333;
        }
        .navbar-custom {
            background-color: #ffffff;
            box-shadow: 0 2px 10px rgba(0,0,0,0.05);
        }
        .card-custom {
            border: none;
            border-radius: 12px;
            box-shadow: 0 4px 12px rgba(0,0,0,0.03);
            background: #ffffff;
        }
        .stat-card {
            border-radius: 12px;
            transition: all 0.2s ease-in-out;
            background: #ffffff;
            border: 1px solid #edf2f7;
        }
        .stat-card:hover {
            transform: translateY(-4px);
            box-shadow: 0 6px 15px rgba(0,0,0,0.08);
        }
        .table-responsive { 
            background: white; 
            border-radius: 12px; 
            overflow: hidden;
        }
        .table thead th {
            background-color: #f1f5f9;
            color: #475569;
            font-weight: 600;
            border-bottom: 2px solid #e2e8f0;
            padding: 12px 16px;
        }
        .table tbody td {
            padding: 14px 16px;
        }
        .badge-soft-success { background-color: #d1e7dd; color: #0f5132; }
        .badge-soft-primary { background-color: #cfe2ff; color: #084298; }
        .badge-soft-warning { background-color: #fff3cd; color: #664d03; }
        @media print {
            .no-print { display: none !important; }
            body { background-color: white; }
            .card-custom, .stat-card { box-shadow: none !important; border: 1px solid #ddd !important; }
        }
    </style>
</head>
<body>

<!-- Header Navbar -->
<nav class="navbar navbar-expand-lg navbar-custom py-3 mb-4 no-print">
    <div class="container-fluid px-4">
        <a class="navbar-brand fw-bold text-primary" href="#">
            <i class="fa-solid fa-graduation-cap me-2"></i>INTERN MANAGEMENT SYSTEM
        </a>
        <div class="d-flex align-items-center gap-3">
            <span class="badge bg-primary-subtle text-primary px-3 py-2 rounded-pill fw-semibold">
                <i class="fa-solid fa-user-shield me-1"></i> Role: HR Manager
            </span>
        </div>
    </div>
</nav>

<div class="container-fluid px-4 pb-5">

    <!-- Tiêu đề & Các nút Thao tác -->
    <div class="d-flex flex-wrap justify-content-between align-items-center mb-4 gap-3">
        <div>
            <h3 class="fw-bold text-dark mb-1">
                <i class="fa-solid fa-file-invoice text-primary me-2"></i>Báo Cáo Tổng Hợp Đánh Giá Cuối Kỳ
            </h3>
            <p class="text-muted mb-0">SCRUM-140: Tổng hợp kết quả thực tập để gửi cho Trường đại học & Ban Lãnh Đạo</p>
        </div>
        <div class="d-flex gap-2 no-print">
            <button class="btn btn-outline-secondary px-3" onclick="window.print()">
                <i class="fa-solid fa-print me-2"></i>In báo cáo
            </button>
            <button class="btn btn-success px-3" onclick="sendReportModal()">
                <i class="fa-solid fa-paper-plane me-2"></i>Gửi Cho Trường / BLĐ
            </button>
        </div>
    </div>

    <!-- Thông báo Alert (mô phỏng) -->
    <div id="alertSuccess" class="alert alert-success alert-dismissible fade show d-none no-print" role="alert">
        <i class="fa-solid fa-circle-check me-2"></i><span id="alertText">Đã gửi báo cáo thành công!</span>
        <button type="button" class="btn-close" onclick="hideAlert()"></button>
    </div>

    <!-- Khối Thống kê nhanh (KPI Summary) -->
    <div class="row g-3 mb-4">
        <div class="col-xl-3 col-md-6">
            <div class="p-3 stat-card border-start border-4 border-primary shadow-sm">
                <div class="d-flex justify-content-between align-items-center">
                    <div>
                        <div class="text-muted small fw-semibold">TỔNG THỰC TẬP SINH</div>
                        <div class="fs-3 fw-bold text-dark mt-1">45</div>
                    </div>
                    <div class="p-3 bg-primary-subtle text-primary rounded-circle">
                        <i class="fa-solid fa-users fs-4"></i>
                    </div>
                </div>
            </div>
        </div>
        <div class="col-xl-3 col-md-6">
            <div class="p-3 stat-card border-start border-4 border-success shadow-sm">
                <div class="d-flex justify-content-between align-items-center">
                    <div>
                        <div class="text-muted small fw-semibold">ĐẠT YÊU CẦU (KHÁ+)</div>
                        <div class="fs-3 fw-bold text-success mt-1">38 <small class="fs-6 text-muted">(84.4%)</small></div>
                    </div>
                    <div class="p-3 bg-success-subtle text-success rounded-circle">
                        <i class="fa-solid fa-user-check fs-4"></i>
                    </div>
                </div>
            </div>
        </div>
        <div class="col-xl-3 col-md-6">
            <div class="p-3 stat-card border-start border-4 border-warning shadow-sm">
                <div class="d-flex justify-content-between align-items-center">
                    <div>
                        <div class="text-muted small fw-semibold">ĐIỂM TRUNG BÌNH KỲ</div>
                        <div class="fs-3 fw-bold text-warning mt-1">8.45 / 10</div>
                    </div>
                    <div class="p-3 bg-warning-subtle text-warning rounded-circle">
                        <i class="fa-solid fa-star fs-4"></i>
                    </div>
                </div>
            </div>
        </div>
        <div class="col-xl-3 col-md-6">
            <div class="p-3 stat-card border-start border-4 border-info shadow-sm">
                <div class="d-flex justify-content-between align-items-center">
                    <div>
                        <div class="text-muted small fw-semibold">ĐỀ XUẤT TUYỂN DỤNG</div>
                        <div class="fs-3 fw-bold text-info mt-1">12</div>
                    </div>
                    <div class="p-3 bg-info-subtle text-info rounded-circle">
                        <i class="fa-solid fa-briefcase fs-4"></i>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <!-- Bộ lọc & Xuất file -->
    <div class="card card-custom mb-4 no-print">
        <div class="card-body p-4">
            <div class="row g-3">
                <div class="col-md-3">
                    <label class="form-label fw-semibold small">Trường Đại Học</label>
                    <select id="filterUniversity" class="form-select" onchange="filterData()">
                        <option value="">-- Tất cả trường --</option>
                        <option value="Bách Khoa">Đại học Bách Khoa</option>
                        <option value="Công Nghệ">Đại học Công Nghệ</option>
                        <option value="Công Nghiệp">Đại học Công Nghiệp</option>
                    </select>
                </div>
                <div class="col-md-3">
                    <label class="form-label fw-semibold small">Kỳ Thực Tập</label>
                    <select class="form-select">
                        <option value="2026-K1">Kỳ 1 - Năm 2026</option>
                        <option value="2025-K2">Kỳ 2 - Năm 2025</option>
                    </select>
                </div>
                <div class="col-md-3">
                    <label class="form-label fw-semibold small">Xếp Loại</label>
                    <select id="filterGrade" class="form-select" onchange="filterData()">
                        <option value="">-- Tất cả xếp loại --</option>
                        <option value="Xuất sắc">Xuất sắc</option>
                        <option value="Giỏi">Giỏi</option>
                        <option value="Khá">Khá</option>
                    </select>
                </div>
                <div class="col-md-3 d-flex align-items-end gap-2">
                    <button class="btn btn-primary w-100" onclick="filterData()">
                        <i class="fa-solid fa-filter me-1"></i>Lọc
                    </button>
                    <button class="btn btn-outline-success w-100" onclick="exportExcel()">
                        <i class="fa-solid fa-file-excel me-1"></i>Xuất Excel
                    </button>
                </div>
            </div>
        </div>
    </div>

    <!-- Bảng dữ liệu danh sách tổng hợp -->
    <div class="card card-custom p-3">
        <div class="table-responsive">
            <table class="table table-hover align-middle mb-0" id="reportTable">
                <thead>
                    <tr>
                        <th>Mã TTS</th>
                        <th>Họ và Tên</th>
                        <th>Trường Đại Học</th>
                        <th>Mentor Đánh Giá</th>
                        <th class="text-center">Chuyên Cần</th>
                        <th class="text-center">Dự Án</th>
                        <th class="text-center">Điểm Tổng</th>
                        <th class="text-center">Xếp Loại</th>
                        <th>Đề Xuất Nhân Sự (HR)</th>
                    </tr>
                </thead>
                <tbody>
                    <tr>
                        <td><strong>TTS001</strong></td>
                        <td class="fw-semibold">Nguyễn Văn An</td>
                        <td>Đại học Bách Khoa</td>
                        <td>Trần Văn Bình (Tech Lead)</td>
                        <td class="text-center">9.5</td>
                        <td class="text-center">9.0</td>
                        <td class="text-center fw-bold text-primary">9.2</td>
                        <td class="text-center"><span class="badge badge-soft-success px-3 py-2 rounded-pill">Xuất sắc</span></td>
                        <td><span class="text-success fw-semibold"><i class="fa-solid fa-circle-check me-1"></i>Đề xuất Onboarding Official</span></td>
                    </tr>
                    <tr>
                        <td><strong>TTS002</strong></td>
                        <td class="fw-semibold">Lê Thị Chi</td>
                        <td>Đại học Công Nghệ</td>
                        <td>Phạm Văn Dung (Senior Dev)</td>
                        <td class="text-center">8.5</td>
                        <td class="text-center">8.0</td>
                        <td class="text-center fw-bold text-primary">8.2</td>
                        <td class="text-center"><span class="badge badge-soft-primary px-3 py-2 rounded-pill">Giỏi</span></td>
                        <td>Cấp chứng chỉ hoàn thành</td>
                    </tr>
                    <tr>
                        <td><strong>TTS003</strong></td>
                        <td class="fw-semibold">Trần Quốc Đạt</td>
                        <td>Đại học Công Nghiệp</td>
                        <td>Hoàng Anh Tuấn (PM)</td>
                        <td class="text-center">9.0</td>
                        <td class="text-center">8.8</td>
                        <td class="text-center fw-bold text-primary">8.9</td>
                        <td class="text-center"><span class="badge badge-soft-success px-3 py-2 rounded-pill">Xuất sắc</span></td>
                        <td><span class="text-success fw-semibold"><i class="fa-solid fa-circle-check me-1"></i>Đề xuất Onboarding Official</span></td>
                    </tr>
                    <tr>
                        <td><strong>TTS004</strong></td>
                        <td class="fw-semibold">Phạm Minh Giang</td>
                        <td>Đại học Bách Khoa</td>
                        <td>Nguyễn Thị Mai (QA Lead)</td>
                        <td class="text-center">7.5</td>
                        <td class="text-center">7.0</td>
                        <td class="text-center fw-bold text-primary">7.2</td>
                        <td class="text-center"><span class="badge badge-soft-warning px-3 py-2 rounded-pill">Khá</span></td>
                        <td>Cấp chứng chỉ hoàn thành</td>
                    </tr>
                </tbody>
            </table>
        </div>
    </div>

</div>

<!-- JavaScript tương tác -->
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
<script>
    function sendReportModal() {
        const confirmSend = confirm("Bạn có chắc chắn muốn gửi báo cáo tổng hợp này tới Ban Lãnh Đạo và Đại diện Trường Đại học?");
        if (confirmSend) {
            document.getElementById('alertSuccess').classList.remove('d-none');
            document.getElementById('alertText').innerText = "Báo cáo đã được gửi tới Ban Lãnh Đạo & Trường Đại Học thành công!";
            window.scrollTo({ top: 0, behavior: 'smooth' });
        }
    }

    function hideAlert() {
        document.getElementById('alertSuccess').classList.add('d-none');
    }

    function exportExcel() {
        alert("Đang xuất dữ liệu báo cáo ra file Excel (.xlsx)...");
    }

    function filterData() {
        const uni = document.getElementById('filterUniversity').value.toLowerCase();
        const grade = document.getElementById('filterGrade').value.toLowerCase();
        const rows = document.querySelectorAll('#reportTable tbody tr');

        rows.forEach(row => {
            const rowUni = row.children[2].innerText.toLowerCase();
            const rowGrade = row.children[7].innerText.toLowerCase();

            const matchUni = !uni || rowUni.includes(uni);
            const matchGrade = !grade || rowGrade.includes(grade);

            if (matchUni && matchGrade) {
                row.style.display = '';
            } else {
                row.style.display = 'none';
            }
        });
    }
</script>
</body>
</html>