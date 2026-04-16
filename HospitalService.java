package hospital.service;

import hospital.dao.*;
import hospital.db.DBConnection;
import hospital.model.DataStore;
import hospital.model.DataStore.*;

import java.util.List;

/**
 * HospitalService — the single service class the frontend talks to.
 *
 * HOW TO SWITCH FROM IN-MEMORY TO MYSQL:
 *   In DataStore.java, change USE_DB = false  →  true
 *   That's it. Every call from the UI will route here.
 *
 * All methods mirror the static methods in DataStore exactly,
 * so the UI code never changes.
 */
public class HospitalService {

    private static final UserDAO         userDAO         = new UserDAO();
    private static final DoctorDAO       doctorDAO       = new DoctorDAO();
    private static final DepartmentDAO   departmentDAO   = new DepartmentDAO();
    private static final AppointmentDAO  appointmentDAO  = new AppointmentDAO();
    private static final PrescriptionDAO prescriptionDAO = new PrescriptionDAO();

    // ─── Auth ─────────────────────────────────────────────────
    public static User login(String email, String password) {
        return userDAO.login(email, password);
    }

    public static boolean emailExists(String email) {
        return userDAO.emailExists(email);
    }

    public static boolean registerPatient(String name, String email, String password, String phone) {
        return userDAO.registerPatient(name, email, password, phone);
    }

    // ─── Users ────────────────────────────────────────────────
    public static User getUserById(String id) {
        return userDAO.getUserById(id);
    }

    public static List<User> getAllUsers() {
        return userDAO.getAllUsers();
    }

    public static boolean deleteUser(String id) {
        return userDAO.deleteUser(id);
    }

    // ─── Doctors ──────────────────────────────────────────────
    public static List<Doctor> getAllDoctors() {
        return doctorDAO.getAllDoctors();
    }

    public static Doctor getDoctorById(String id) {
        return doctorDAO.getDoctorById(id);
    }

    public static List<Doctor> searchDoctors(String query) {
        return doctorDAO.searchDoctors(query);
    }

    public static boolean addDoctor(Doctor d, String email, String password, String phone) {
        // Insert user row first, then doctor row
        boolean userOk = userDAO.insertDoctorUser(d.id, "Dr. " + d.name, email, password, phone);
        if (!userOk) return false;
        return doctorDAO.insertDoctor(d);
    }

    public static boolean removeDoctor(String id) {
        doctorDAO.deleteDoctor(id);
        return userDAO.deleteUser(id);   // cascades in DB too
    }

    public static boolean updateAvailability(String doctorId, String from, String to) {
        return doctorDAO.updateAvailability(doctorId, from, to);
    }

    public static boolean addRating(String doctorId, int stars) {
        return doctorDAO.addRating(doctorId, stars);
    }

    public static String generateDoctorId() {
        return doctorDAO.generateNextId();
    }

    // ─── Departments ──────────────────────────────────────────
    public static List<Department> getAllDepartments() {
        return departmentDAO.getAllDepartments();
    }

    public static boolean addDepartment(String name, String description) {
        String id = departmentDAO.generateNextId();
        return departmentDAO.insert(new Department(id, name, description));
    }

    public static boolean removeDepartment(String id) {
        return departmentDAO.delete(id);
    }

    // ─── Appointments ─────────────────────────────────────────
    public static Appointment bookAppointment(String patientId, String doctorId,
                                               String date, String time) {
        return appointmentDAO.bookAppointment(patientId, doctorId, date, time);
    }

    public static List<Appointment> getPatientAppointments(String patientId) {
        return appointmentDAO.getPatientAppointments(patientId);
    }

    public static List<Appointment> getDoctorAppointments(String doctorId) {
        return appointmentDAO.getDoctorAppointments(doctorId);
    }

    public static List<Appointment> getAllAppointments() {
        return appointmentDAO.getAllAppointments();
    }

    public static boolean updateAppointmentStatus(String token, String status) {
        return appointmentDAO.updateStatusByToken(token, status);
    }

    public static boolean cancelAppointment(String token) {
        return appointmentDAO.cancelByToken(token);
    }

    // ─── Prescriptions ────────────────────────────────────────
    public static Prescription savePrescription(String appointmentId, String patientId,
                                                 String doctorId, String medicines, String notes) {
        return prescriptionDAO.create(appointmentId, patientId, doctorId, medicines, notes);
    }

    public static List<Prescription> getPatientPrescriptions(String patientId) {
        return prescriptionDAO.getPatientPrescriptions(patientId);
    }

    public static List<Prescription> getDoctorPrescriptions(String doctorId) {
        return prescriptionDAO.getDoctorPrescriptions(doctorId);
    }

    // ─── Reports ──────────────────────────────────────────────
    public static int countAppointmentsByStatus(String status) {
        return appointmentDAO.countByStatus(status);
    }

    public static int countByDoctor(String doctorId) {
        return appointmentDAO.countByDoctor(doctorId);
    }

    public static int countCompletedByDoctor(String doctorId) {
        return appointmentDAO.countCompletedByDoctor(doctorId);
    }

    // ─── DB health check ──────────────────────────────────────
    public static boolean isDatabaseAvailable() {
        return DBConnection.testConnection();
    }
}
