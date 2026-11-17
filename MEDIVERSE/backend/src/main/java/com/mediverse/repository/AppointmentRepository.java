package com.mediverse.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mediverse.entity.Appointment;
import com.mediverse.entity.AppointmentStatus;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, String> {

    // 1. Find appointments belonging to a patient, ordered by appointment date
    List<Appointment> findAllByPatient_PatientIdOrderByAppointmentDateDesc(String patientId);
    List<Appointment> findAllByPatient_PatientIdOrderByAppointmentDateAsc(String patientId);
    List<Appointment> findAllByPatient_User_UserIdOrderByAppointmentDateDesc(String userId);
    List<Appointment> findAllByPatient_User_UserIdOrderByAppointmentDateAsc(String userId);

    // 2. Find appointments assigned to a doctor, ordered by appointment date
    List<Appointment> findAllByDoctor_DoctorIdOrderByAppointmentDateDesc(String doctorId);
    List<Appointment> findAllByDoctor_DoctorIdOrderByAppointmentDateAsc(String doctorId);
    List<Appointment> findAllByDoctor_User_UserIdOrderByAppointmentDateDesc(String userId);
    List<Appointment> findAllByDoctor_User_UserIdOrderByAppointmentDateAsc(String userId);

    // 3. Find appointments for a specific patient and status
    List<Appointment> findAllByPatient_PatientIdAndStatusOrderByAppointmentDateDesc(String patientId, AppointmentStatus status);
    List<Appointment> findAllByPatient_User_UserIdAndStatusOrderByAppointmentDateDesc(String userId, AppointmentStatus status);

    // 4. Find appointments for a specific doctor and status
    List<Appointment> findAllByDoctor_DoctorIdAndStatusOrderByAppointmentDateDesc(String doctorId, AppointmentStatus status);
    List<Appointment> findAllByDoctor_User_UserIdAndStatusOrderByAppointmentDateDesc(String userId, AppointmentStatus status);

    // 5. Detect an existing SCHEDULED appointment for the same doctor and exact appointment date/time
    boolean existsByDoctor_DoctorIdAndAppointmentDateAndStatus(String doctorId, LocalDateTime appointmentDate, AppointmentStatus status);
    boolean existsByDoctor_User_UserIdAndAppointmentDateAndStatus(String userId, LocalDateTime appointmentDate, AppointmentStatus status);

    // 6. Detect an existing SCHEDULED appointment for the same patient and exact appointment date/time
    boolean existsByPatient_PatientIdAndAppointmentDateAndStatus(String patientId, LocalDateTime appointmentDate, AppointmentStatus status);
    boolean existsByPatient_User_UserIdAndAppointmentDateAndStatus(String userId, LocalDateTime appointmentDate, AppointmentStatus status);

    // 7. Find an appointment by appointment ID and patient user ID
    Optional<Appointment> findByAppointmentIdAndPatient_User_UserId(String appointmentId, String userId);
    Optional<Appointment> findByAppointmentIdAndPatient_PatientId(String appointmentId, String patientId);

    // 8. Find an appointment by appointment ID and doctor user ID
    Optional<Appointment> findByAppointmentIdAndDoctor_User_UserId(String appointmentId, String userId);
    Optional<Appointment> findByAppointmentIdAndDoctor_DoctorId(String appointmentId, String doctorId);
}
