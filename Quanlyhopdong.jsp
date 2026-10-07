<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Quản lý hợp đồng thực tập</title>

    <link rel="stylesheet" href="${pageContext.request.contextPath}/style.css">
</head>

<body>

<!-- ================= HEADER ================= -->
<header class="top-header">
    <div class="brand">
        <div class="brand-logo">IMS</div>

        <div>
            <h1>IMS Portal</h1>
            <span>Hệ thống quản lý thực tập sinh</span>
        </div>
    </div>

    <div class="header-actions">
        <button class="header-btn" type="button">
            🔔 Thông báo
        </button>

        <button class="account-btn" type="button">
            HR
        </button>
    </div>
</header>


<div class="app-layout">

    <!-- ================= SIDEBAR ================= -->
    <aside class="sidebar">
        <nav>
            <a href="#" class="nav-item">
                <span>▦</span>
                Tổng quan
            </a>

            <a href="#" class="nav-item">
                <span>▤</span>
                Hồ sơ ứng tuyển
            </a>

            <a href="#" class="nav-item">
                <span>▣</span>
                Chương trình
            </a>

            <a href="#" class="nav-item active">
                <span>▧</span>
                Hợp đồng
            </a>

            <a href="#" class="nav-item">
                <span>♙</span>
                Phân công Mentor
            </a>

            <a href="#" class="nav-item">
                <span>▦</span>
                Báo cáo
            </a>
        </nav>
    </aside>


    <!-- ================= MAIN ================= -->
    <main class="main-content">

        <!-- Breadcrumb -->
        <div class="breadcrumb">
            Quản lý nhân sự
            <span>/</span>
            Hợp đồng thực tập
        </div>

        <!-- Tiêu đề -->
        <div class="page-heading">
            <div>
                <h2>Hợp đồng thực tập</h2>
                <p>
                    Quản lý, tải lên và theo dõi trạng thái xác nhận
                    hợp đồng của thực tập sinh.
                </p>
            </div>
        </div>


        <!-- ================= THỐNG KÊ ================= -->
        <section class="stats-grid">

            <div class="stat-card">
                <div class="stat-label">Tổng thực tập sinh</div>
                <div class="stat-value" id="totalCount">0</div>
            </div>

            <div class="stat-card">
                <div class="stat-label">Chưa tải hợp đồng</div>
                <div class="stat-value" id="notUploadedCount">0</div>
            </div>

            <div class="stat-card">
                <div class="stat-label">Chờ xác nhận</div>
                <div class="stat-value warning-text" id="pendingCount">0</div>
            </div>

            <div class="stat-card">
                <div class="stat-label">Đã xác nhận</div>
                <div class="stat-value success-text" id="confirmedCount">0</div>
            </div>

        </section>


        <!-- ================= DANH SÁCH ================= -->
        <section class="card">

            <div class="card-header">
                <div>
                    <h3>Danh sách hợp đồng</h3>
                    <p>Danh sách thực tập sinh đã được tiếp nhận.</p>
                </div>
            </div>


            <!-- Filter -->
            <div class="filter-bar">

                <div class="search-box">
                    <label for="searchInput">Tìm kiếm</label>

                    <input
                        type="text"
                        id="searchInput"
                        placeholder="Nhập tên thực tập sinh..."
                    >
                </div>


                <div class="filter-group">
                    <label for="statusFilter">Trạng thái</label>

                    <select id="statusFilter">
                        <option value="all">Tất cả trạng thái</option>
                        <option value="not-uploaded">Chưa tải</option>
                        <option value="pending">Chờ xác nhận</option>
                        <option value="confirmed">Đã xác nhận</option>
                    </select>
                </div>

            </div>


            <!-- Table -->
            <div class="table-wrapper">

                <table>
                    <thead>
                    <tr>
                        <th>TTS</th>
                        <th>Chương trình</th>
                        <th>Mentor</th>
                        <th>Thời gian</th>
                        <th>Hợp đồng</th>
                        <th>Xác nhận</th>
                        <th>Thao tác</th>
                    </tr>
                    </thead>

                    <tbody id="contractTableBody">
                    <!-- JavaScript render dữ liệu -->
                    </tbody>
                </table>


                <!-- Empty state -->
                <div id="emptyState" class="empty-state hidden">
                    <div class="empty-icon">▧</div>
                    <h4>Không tìm thấy hợp đồng</h4>
                    <p>Không có dữ liệu phù hợp với điều kiện tìm kiếm.</p>
                </div>

            </div>

        </section>


        <!-- ================= FORM UPLOAD ================= -->
        <section id="uploadSection" class="card upload-card hidden">

            <div class="card-header form-header">
                <div>
                    <h3 id="formTitle">Tải hợp đồng</h3>

                    <p>
                        Thực tập sinh:
                        <strong id="selectedInternName">---</strong>
                    </p>
                </div>

                <button
                    type="button"
                    class="close-button"
                    id="closeFormBtn"
                    aria-label="Đóng"
                >
                    ×
                </button>
            </div>


            <form id="contractForm" novalidate>

                <input type="hidden" id="selectedContractId">


                <div class="form-grid">

                    <!-- Số hợp đồng -->
                    <div class="form-group">
                        <label for="contractNumber">
                            Số hợp đồng <span class="required">*</span>
                        </label>

                        <input
                            type="text"
                            id="contractNumber"
                            placeholder="VD: HD-2026-08"
                        >

                        <small
                            class="error-message"
                            id="contractNumberError"
                        ></small>
                    </div>


                    <!-- Phụ cấp -->
                    <div class="form-group">
                        <label for="allowance">
                            Phụ cấp
                        </label>

                        <div class="input-suffix">
                            <input
                                type="number"
                                id="allowance"
                                min="0"
                                step="1"
                                placeholder="VD: 5000000"
                            >
                            <span>VNĐ</span>
                        </div>

                        <small
                            class="error-message"
                            id="allowanceError"
                        ></small>
                    </div>


                    <!-- Ngày bắt đầu -->
                    <div class="form-group">
                        <label for="startDate">
                            Ngày bắt đầu <span class="required">*</span>
                        </label>

                        <input
                            type="date"
                            id="startDate"
                        >

                        <small
                            class="error-message"
                            id="startDateError"
                        ></small>
                    </div>


                    <!-- Ngày kết thúc -->
                    <div class="form-group">
                        <label for="endDate">
                            Ngày kết thúc <span class="required">*</span>
                        </label>

                        <input
                            type="date"
                            id="endDate"
                        >

                        <small
                            class="error-message"
                            id="endDateError"
                        ></small>
                    </div>


                    <!-- Hạn xác nhận -->
                    <div class="form-group">
                        <label for="confirmationDeadline">
                            Hạn xác nhận <span class="required">*</span>
                        </label>

                        <input
                            type="datetime-local"
                            id="confirmationDeadline"
                        >

                        <small
                            class="error-message"
                            id="deadlineError"
                        ></small>
                    </div>


                    <!-- File -->
                    <div class="form-group">
                        <label>
                            Tệp hợp đồng PDF <span class="required">*</span>
                        </label>

                        <div
                            class="file-upload"
                            id="fileUploadArea"
                        >
                            <input
                                type="file"
                                id="contractFile"
                                accept=".pdf,application/pdf"
                            >

                            <div class="upload-content">
                                <div class="upload-icon">PDF</div>

                                <div>
                                    <strong>Chọn tệp hợp đồng</strong>
                                    <p>Chỉ chấp nhận định dạng PDF</p>
                                </div>
                            </div>
                        </div>

                        <div
                            id="selectedFile"
                            class="selected-file hidden"
                        ></div>

                        <small
                            class="error-message"
                            id="fileError"
                        ></small>
                    </div>

                </div>


                <!-- Actions -->
                <div class="form-actions">

                    <button
                        type="button"
                        class="btn btn-danger-outline"
                        id="cancelBtn"
                    >
                        Hủy
                    </button>

                    <button
                        type="submit"
                        class="btn btn-primary"
                    >
                        Gửi hợp đồng cho TTS
                    </button>

                </div>

            </form>

        </section>

    </main>
</div>


<!-- ================= TOAST ================= -->
<div
    id="toast"
    class="toast"
    role="status"
    aria-live="polite"
>
    <span id="toastMessage"></span>
</div>


<script src="${pageContext.request.contextPath}/Quanlyhopdong.js"></script>

</body>
</html>