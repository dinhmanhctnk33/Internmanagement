import hr.model.Assignment;
import hr.model.Intern;
import hr.model.Mentor;
import hr.service.AssignmentService;

import java.util.ArrayList;
import java.util.List;

public class Main {

    public static void main(String[] args) {

        // =====================================
        // 1. TẠO DANH SÁCH MENTOR
        // =====================================

        List<Mentor> mentors = new ArrayList<>();

        Mentor mentorNam = new Mentor(
                1,
                "Nguyen Van Nam",
                "nam@gmail.com",
                "Backend",
                true
        );

        Mentor mentorMinh = new Mentor(
                2,
                "Tran Van Minh",
                "minh@gmail.com",
                "Backend",
                true
        );

        Mentor mentorLan = new Mentor(
                3,
                "Nguyen Thi Lan",
                "lan@gmail.com",
                "Frontend",
                true
        );

        mentors.add(mentorNam);
        mentors.add(mentorMinh);
        mentors.add(mentorLan);


        // =====================================
        // 2. TẠO DANH SÁCH THỰC TẬP SINH
        // =====================================

        List<Intern> interns = new ArrayList<>();

        Intern an = new Intern(
                1,
                "Nguyen Van An",
                "an@gmail.com",
                "Backend",
                "Được nhận"
        );

        Intern binh = new Intern(
                2,
                "Tran Van Binh",
                "binh@gmail.com",
                "Backend",
                "Được nhận"
        );

        Intern cuong = new Intern(
                3,
                "Le Van Cuong",
                "cuong@gmail.com",
                "Backend",
                "Được nhận"
        );

        Intern dung = new Intern(
                4,
                "Pham Van Dung",
                "dung@gmail.com",
                "Frontend",
                "Được nhận"
        );

        Intern hoa = new Intern(
                5,
                "Nguyen Thi Hoa",
                "hoa@gmail.com",
                "Backend",
                "Từ chối"
        );

        interns.add(an);
        interns.add(binh);
        interns.add(cuong);
        interns.add(dung);
        interns.add(hoa);


        // =====================================
        // 3. GỌI ASSIGNMENT SERVICE
        // =====================================

        AssignmentService service =
                new AssignmentService();

        List<Assignment> suggestions =
                service.suggestAssignments(
                        interns,
                        mentors
                );


        // =====================================
        // 4. IN KẾT QUẢ
        // =====================================

        System.out.println();
        System.out.println("=================================");
        System.out.println("      DE XUAT PHAN CONG");
        System.out.println("=================================");

        for (Assignment assignment : suggestions) {

            System.out.println(
                    assignment.getIntern().getName()
                    + " -> "
                    + assignment.getProposedMentor().getName()
            );
        }

        System.out.println("=================================");
        System.out.println(
                "Tong so de xuat: "
                + suggestions.size()
        );
        // =================================================
// 4.1 HR CHỈNH SỬA ĐỀ XUẤT
// =================================================

if (!suggestions.isEmpty()) {

    // Đổi mentor cho đề xuất đầu tiên
    service.editAssignment(
        suggestions.get(0),
        mentors.get(1)
    );
}

// Bỏ intern thứ 2 khỏi lô đề xuất
if (interns.size() > 1) {

    service.removeAssignment(
        suggestions,
        interns.get(1)
    );
}

System.out.println();
System.out.println(
    "Tong so de xuat sau khi chinh sua: "
    + suggestions.size()
);

        // =====================================
// 5. HR XÁC NHẬN PHÂN CÔNG
// =====================================

service.confirmAssignments(
        suggestions,
        "HR_Admin"
);
service.confirmAssignments(
    suggestions,
    "HR_Admin"
);


// =================================================
// 5.1 HR ĐỔI MENTOR ĐÃ ĐƯỢC PHÂN CÔNG
// =================================================

service.changeMentor(
    interns.get(2),
    mentors.get(1),
    "HR_Admin"
);


// =================================================
// 6. KIỂM TRA KẾT QUẢ SAU XÁC NHẬN
// =================================================


// =====================================
// 6. KIỂM TRA KẾT QUẢ SAU XÁC NHẬN
// =====================================

System.out.println();
System.out.println("=================================");
System.out.println("     KET QUA SAU XAC NHAN");
System.out.println("=================================");

for (Intern intern : interns) {

    if (intern.getMentor() != null) {

        System.out.println(
                intern.getName()
                + " -> "
                + intern.getMentor().getName()
        );
    }
}
// =====================================
// 7. HIỂN THỊ NHẬT KÝ
// =====================================

service.printLogs();
        System.out.println("=================================");
    }
}