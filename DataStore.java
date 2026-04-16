package hospital.model;

import java.util.*;

/**
 * DataStore — simulates a MySQL database in memory.
 * Replace each list/map with actual JDBC calls when backend is ready.
 */
public class DataStore {

    // ─── User ────────────────────────────────────────────────────
    public static class User {
        public String id, name, email, password, role, phone;
        public User(String id, String name, String email, String password, String role, String phone) {
            this.id = id; this.name = name; this.email = email;
            this.password = password; this.role = role; this.phone = phone;
        }
    }

    // ─── Doctor ──────────────────────────────────────────────────
    public static class Doctor {
        public String id, name, department, qualification, availableFrom, availableTo;
        public double rating; public int ratingCount;
        public Doctor(String id, String name, String dept, String qual, String from, String to) {
            this.id = id; this.name = name; this.department = dept;
            this.qualification = qual; this.availableFrom = from; this.availableTo = to;
            this.rating = 0; this.ratingCount = 0;
        }
        public String getDisplayName() { return "Dr. " + name + " (" + department + ")"; }
        public String getRatingStr()   { return ratingCount == 0 ? "No ratings" : String.format("%.1f ★ (%d)", rating, ratingCount); }
    }

    // ─── Department ──────────────────────────────────────────────
    public static class Department {
        public String id, name, description;
        public Department(String id, String name, String desc) {
            this.id = id; this.name = name; this.description = desc;
        }
    }

    // ─── Appointment ─────────────────────────────────────────────
    public static class Appointment {
        public String id, patientId, doctorId, date, time, status, notes, token;
        public int tokenNumber;
        public Appointment(String id, String patientId, String doctorId,
                           String date, String time, int tokenNumber) {
            this.id = id; this.patientId = patientId; this.doctorId = doctorId;
            this.date = date; this.time = time; this.tokenNumber = tokenNumber;
            this.token = String.format("T%03d", tokenNumber);
            this.status = "Booked"; this.notes = "";
        }
    }

    // ─── Prescription ────────────────────────────────────────────
    public static class Prescription {
        public String id, appointmentId, patientId, doctorId, date, notes, medicines;
        public Prescription(String id, String apptId, String patId, String docId, String date) {
            this.id = id; this.appointmentId = apptId;
            this.patientId = patId; this.doctorId = docId;
            this.date = date; this.notes = ""; this.medicines = "";
        }
    }

    // ─── Static Data ─────────────────────────────────────────────
    public static List<User>         users         = new ArrayList<>();
    public static List<Doctor>       doctors       = new ArrayList<>();
    public static List<Department>   departments   = new ArrayList<>();
    public static List<Appointment>  appointments  = new ArrayList<>();
    public static List<Prescription> prescriptions = new ArrayList<>();

    private static int apptCounter = 1;
    private static int tokenCounter = 1;

    // ─── Seed Data ────────────────────────────────────────────────
    static {
        // Departments
        departments.add(new Department("D1", "Cardiology",    "Heart & cardiovascular care"));
        departments.add(new Department("D2", "Neurology",     "Brain & nervous system"));
        departments.add(new Department("D3", "Orthopedics",   "Bones, joints & muscles"));
        departments.add(new Department("D4", "Pediatrics",    "Children healthcare"));
        departments.add(new Department("D5", "Dermatology",   "Skin & hair care"));
        departments.add(new Department("D6", "General",       "General medicine"));

        // Doctors
        Doctor d1 = new Doctor("DR1","Arjun Mehta",     "Cardiology",  "MBBS, MD",  "09:00","17:00");
        Doctor d2 = new Doctor("DR2","Priya Sharma",    "Neurology",   "MBBS, DM",  "10:00","18:00");
        Doctor d3 = new Doctor("DR3","Ramesh Kumar",    "Orthopedics", "MBBS, MS",  "08:00","16:00");
        Doctor d4 = new Doctor("DR4","Sunita Rao",      "Pediatrics",  "MBBS, DCH", "09:00","17:00");
        Doctor d5 = new Doctor("DR5","Kavya Nair",      "Dermatology", "MBBS, DVD", "11:00","19:00");
        Doctor d6 = new Doctor("DR6","Vijay Patel",     "General",     "MBBS",      "08:00","20:00");
        d1.rating = 4.7; d1.ratingCount = 23;
        d2.rating = 4.5; d2.ratingCount = 18;
        d3.rating = 4.8; d3.ratingCount = 31;
        d4.rating = 4.6; d4.ratingCount = 15;
        d5.rating = 4.3; d5.ratingCount = 12;
        d6.rating = 4.9; d6.ratingCount = 45;
        doctors.add(d1); doctors.add(d2); doctors.add(d3);
        doctors.add(d4); doctors.add(d5); doctors.add(d6);

        // Users
        users.add(new User("U0", "Admin",         "admin@hospital.com",   "admin123",   "admin",   "9800000000"));
        users.add(new User("U1", "Raj Kumar",      "raj@email.com",        "pass123",    "patient", "9876543210"));
        users.add(new User("U2", "Meena Devi",     "meena@email.com",      "pass123",    "patient", "9876543211"));
        users.add(new User("DR1","Dr. Arjun Mehta","arjun@hospital.com",   "doctor123",  "doctor",  "9800000001"));
        users.add(new User("DR2","Dr. Priya Sharma","priya@hospital.com",  "doctor123",  "doctor",  "9800000002"));
        users.add(new User("DR3","Dr. Ramesh Kumar","ramesh@hospital.com", "doctor123",  "doctor",  "9800000003"));
        users.add(new User("DR4","Dr. Sunita Rao", "sunita@hospital.com",  "doctor123",  "doctor",  "9800000004"));
        users.add(new User("DR5","Dr. Kavya Nair", "kavya@hospital.com",   "doctor123",  "doctor",  "9800000005"));
        users.add(new User("DR6","Dr. Vijay Patel","vijay@hospital.com",   "doctor123",  "doctor",  "9800000006"));

        // Sample appointments
        Appointment a1 = new Appointment("A1","U1","DR1","2025-04-10","10:00",nextToken());
        a1.status = "Completed"; a1.notes = "BP normal. Prescribed atenolol.";
        Appointment a2 = new Appointment("A2","U1","DR3","2025-04-15","11:00",nextToken());
        a2.status = "Booked";
        Appointment a3 = new Appointment("A3","U2","DR2","2025-04-12","14:00",nextToken());
        a3.status = "Cancelled";
        appointments.add(a1); appointments.add(a2); appointments.add(a3);
        apptCounter = 4;

        // Sample prescription
        Prescription p1 = new Prescription("P1","A1","U1","DR1","2025-04-10");
        p1.medicines = "Atenolol 50mg — 1 tablet daily after breakfast\nAspirin 75mg — 1 tablet daily after dinner";
        p1.notes = "Reduce salt intake. Avoid stress. Follow-up in 30 days.";
        prescriptions.add(p1);
    }

    private static int nextToken() { return tokenCounter++; }

    // ─── Auth ─────────────────────────────────────────────────────
    public static User login(String email, String password) {
        for (User u : users)
            if (u.email.equalsIgnoreCase(email) && u.password.equals(password)) return u;
        return null;
    }

    public static boolean emailExists(String email) {
        for (User u : users) if (u.email.equalsIgnoreCase(email)) return true;
        return false;
    }

    public static void registerPatient(String name, String email, String password, String phone) {
        String id = "U" + (users.size() + 1);
        users.add(new User(id, name, email, password, "patient", phone));
    }

    // ─── Appointments ─────────────────────────────────────────────
    public static Appointment bookAppointment(String patientId, String doctorId, String date, String time) {
        String id = "A" + apptCounter++;
        Appointment a = new Appointment(id, patientId, doctorId, date, time, nextToken());
        appointments.add(a);
        return a;
    }

    public static List<Appointment> getPatientAppointments(String patientId) {
        List<Appointment> result = new ArrayList<>();
        for (Appointment a : appointments) if (a.patientId.equals(patientId)) result.add(a);
        return result;
    }

    public static List<Appointment> getDoctorAppointments(String doctorId) {
        List<Appointment> result = new ArrayList<>();
        for (Appointment a : appointments) if (a.doctorId.equals(doctorId)) result.add(a);
        return result;
    }

    // ─── Doctors ──────────────────────────────────────────────────
    public static Doctor getDoctorById(String id) {
        for (Doctor d : doctors) if (d.id.equals(id)) return d;
        return null;
    }

    public static List<Doctor> searchDoctors(String query) {
        List<Doctor> result = new ArrayList<>();
        String q = query.toLowerCase();
        for (Doctor d : doctors)
            if (d.name.toLowerCase().contains(q) || d.department.toLowerCase().contains(q)) result.add(d);
        return result;
    }

    public static User getUserById(String id) {
        for (User u : users) if (u.id.equals(id)) return u;
        return null;
    }

    public static List<Prescription> getPatientPrescriptions(String patientId) {
        List<Prescription> result = new ArrayList<>();
        for (Prescription p : prescriptions) if (p.patientId.equals(patientId)) result.add(p);
        return result;
    }

    public static void addRating(String doctorId, int stars) {
        Doctor d = getDoctorById(doctorId);
        if (d != null) {
            d.rating = (d.rating * d.ratingCount + stars) / (d.ratingCount + 1);
            d.ratingCount++;
        }
    }
}
