package hr.service;

import hr.model.Assignment;
import hr.model.AssignmentLog;
import hr.model.Intern;
import hr.model.Mentor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AssignmentService {
    private List<AssignmentLog> logs =
        new ArrayList<>();

    // ==========================================
    // 1. TẠO ĐỀ XUẤT CHIA ĐỀU
    // ==========================================

    public List<Assignment> suggestAssignments(
            List<Intern> interns,
            List<Mentor> mentors) {

        List<Assignment> suggestions = new ArrayList<>();

        // Lưu số người hiện tại của từng Mentor
        Map<Integer, Integer> mentorCounts = new HashMap<>();

        // Đếm số thực tập sinh mỗi Mentor đang hướng dẫn
        for (Mentor mentor : mentors) {

            int count = 0;

            for (Intern intern : interns) {

                if (intern.getMentor() != null
                        && intern.getMentor().getId() == mentor.getId()) {

                    count++;
                }
            }

            mentorCounts.put(mentor.getId(), count);
        }

        // Duyệt từng thực tập sinh
        for (Intern intern : interns) {

            // Chỉ người "Được nhận"
            if (!"Được nhận".equals(intern.getStatus())) {
                continue;
            }

            // Đã có Mentor thì không đổi
            if (intern.getMentor() != null) {
                continue;
            }

            Mentor selectedMentor = null;
            int smallestCount = Integer.MAX_VALUE;

            // Tìm Mentor phù hợp
            for (Mentor mentor : mentors) {

                // Mentor phải đang hoạt động
                if (!mentor.isActive()) {
                    continue;
                }

                // Phải cùng phòng ban
                if (!mentor.getDepartment()
                        .equals(intern.getDepartment())) {
                    continue;
                }

                int currentCount =
                        mentorCounts.getOrDefault(
                                mentor.getId(),
                                0
                        );

                // Chọn Mentor ít người nhất
                if (currentCount < smallestCount) {

                    smallestCount = currentCount;
                    selectedMentor = mentor;
                }
            }

            // Có Mentor phù hợp thì tạo đề xuất
            if (selectedMentor != null) {

                Assignment assignment =
                        new Assignment(
                                intern,
                                selectedMentor
                        );

                suggestions.add(assignment);

                // Tăng số lượng dự kiến
                mentorCounts.put(
                        selectedMentor.getId(),
                        mentorCounts.get(selectedMentor.getId()) + 1
                );
            }
        }

        return suggestions;
    }


    // ==========================================
    // 2. XÁC NHẬN PHÂN CÔNG
    // ==========================================

    public void confirmAssignments(
        List<Assignment> suggestions,
        String hrName) {

        System.out.println();
        System.out.println("=================================");
        System.out.println("       XAC NHAN PHAN CONG");
        System.out.println("=================================");

        int confirmed = 0;

        for (Assignment assignment : suggestions) {

            Intern intern = assignment.getIntern();
            Mentor mentor = assignment.getProposedMentor();

            // Kiểm tra lại trạng thái lúc xác nhận
            if (!"Được nhận".equals(intern.getStatus())) {

                System.out.println(
                        "Bo qua: "
                        + intern.getName()
                        + " - khong con trang thai Duoc nhan"
                );

                continue;
            }

            // Nếu trong lúc chờ xác nhận đã có Mentor
            if (intern.getMentor() != null) {

                System.out.println(
                        "Bo qua: "
                        + intern.getName()
                        + " - da co Mentor"
                );

                continue;
            }
            // Kiem tra mentor van dang hoat dong
if (!mentor.isActive()) {

    System.out.println(
        "Bo qua: "
        + intern.getName()
        + " -> Mentor "
        + mentor.getName()
        + " khong con hoat dong"
    );

    continue;
}

// Kiem tra mentor van cung phong ban
if (!intern.getDepartment()
        .equals(mentor.getDepartment())) {

    System.out.println(
        "Bo qua: "
        + intern.getName()
        + " -> Mentor khong cung phong ban"
    );

    continue;
}

            // Gán Mentor chính thức
           intern.setMentor(mentor);

// Tạo nhật ký
AssignmentLog log =
        new AssignmentLog(
                hrName,
                intern,
                mentor
        );

logs.add(log);

confirmed++;
            System.out.println(
                    "Da xac nhan: "
                    + intern.getName()
                    + " -> "
                    + mentor.getName()
            );
        }

        System.out.println("---------------------------------");
        System.out.println(
                "Tong so phan cong da xac nhan: "
                + confirmed
        );
        System.out.println("=================================");
    }
    public void printLogs() {

    System.out.println();
    System.out.println("=================================");
    System.out.println("        NHAT KY PHAN CONG");
    System.out.println("=================================");

    if (logs.isEmpty()) {

        System.out.println(
                "Chua co nhat ky."
        );

        return;
    }

    for (AssignmentLog log : logs) {

        log.printLog();
    }

    System.out.println(
            "Tong so nhat ky: "
            + logs.size()
    );

    System.out.println(
            "================================="
    );
}
// CHINH MENTOR TRONG DE XUAT
public void editAssignment(Assignment assignment, Mentor newMentor) {

    if (newMentor == null) {
        System.out.println("Mentor moi khong hop le!");
        return;
    }

    if (!newMentor.isActive()) {
        System.out.println("Mentor khong con hoat dong!");
        return;
    }

    if (!assignment.getIntern().getDepartment()
            .equals(newMentor.getDepartment())) {

        System.out.println(
                "Mentor khong cung phong ban voi thuc tap sinh!"
        );
        return;
    }

    assignment.setProposedMentor(newMentor);

    System.out.println(
            "Da doi de xuat: "
            + assignment.getIntern().getName()
            + " -> "
            + newMentor.getName()
    );
}


// BO MOT INTERN KHOI LO DE XUAT
public void removeAssignment(
        List<Assignment> assignments,
        Intern intern) {

    assignments.removeIf(
            assignment ->
                    assignment.getIntern().getId() == intern.getId()
    );

    System.out.println(
            "Da bo "
            + intern.getName()
            + " khoi lo de xuat."
    );
        }
        // DOI MENTOR CHO INTERN DA DUOC PHAN CONG
public void changeMentor(
        Intern intern,
        Mentor newMentor,
        String hrName) {

    // Kiem tra intern da co mentor chua
    if (intern.getMentor() == null) {
        System.out.println(
                "Intern chua co mentor, khong the doi!"
        );
        return;
    }

    // Kiem tra mentor moi
    if (newMentor == null) {
        System.out.println(
                "Mentor moi khong hop le!"
        );
        return;
    }

    // Mentor moi phai dang hoat dong
    if (!newMentor.isActive()) {
        System.out.println(
                "Mentor moi khong con hoat dong!"
        );
        return;
    }

    // Mentor moi phai cung phong ban
    if (!intern.getDepartment()
            .equals(newMentor.getDepartment())) {

        System.out.println(
                "Mentor moi khong cung phong ban!"
        );
        return;
    }

    // Khong cho doi sang chinh mentor hien tai
    if (intern.getMentor().getId()
            == newMentor.getId()) {

        System.out.println(
                "Intern dang duoc gan mentor nay!"
        );
        return;
    }

    // Luu mentor cu
    Mentor oldMentor = intern.getMentor();

    // Doi sang mentor moi
    intern.setMentor(newMentor);

    // Ghi nhat ky
    logs.add(
        new AssignmentLog(
            hrName,
            intern,
            oldMentor,
            newMentor
        )
    );

    System.out.println(
            "Da doi mentor: "
            + intern.getName()
            + " -> "
            + newMentor.getName()
    );
        }
}