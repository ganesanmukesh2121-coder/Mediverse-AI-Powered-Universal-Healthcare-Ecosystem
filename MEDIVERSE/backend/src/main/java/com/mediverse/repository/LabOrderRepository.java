package com.mediverse.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mediverse.entity.LabOrder;
import com.mediverse.entity.OrderStatus;

@Repository
public interface LabOrderRepository extends JpaRepository<LabOrder, String> {

    // 1. Patient-scoped queries
    List<LabOrder> findAllByPatient_PatientIdOrderByOrderedAtDesc(String patientId);
    List<LabOrder> findAllByPatient_User_UserIdOrderByOrderedAtDesc(String userId);

    Optional<LabOrder> findByOrderIdAndPatient_PatientId(String orderId, String patientId);
    Optional<LabOrder> findByOrderIdAndPatient_User_UserId(String orderId, String userId);

    // 2. Laboratory-scoped queries
    List<LabOrder> findAllByLab_LabIdOrderByOrderedAtDesc(String labId);
    List<LabOrder> findAllByLab_User_UserIdOrderByOrderedAtDesc(String userId);
    List<LabOrder> findAllByLab_LabIdAndOrderStatusOrderByOrderedAtDesc(String labId, OrderStatus status);

    // 3. Requesting doctor queries
    List<LabOrder> findAllByRequestingDoctor_DoctorIdOrderByOrderedAtDesc(String doctorId);
    List<LabOrder> findAllByRequestingDoctor_User_UserIdOrderByOrderedAtDesc(String userId);
}
