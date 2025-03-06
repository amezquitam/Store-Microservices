package com.example.paymentservice.repository;

import com.example.paymentservice.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, UUID> {

    /**
     * Buscar pagos por el estado.
     *
     * @param status Estado del pago (por ejemplo: PENDING, SUCCESS, FAILED).
     * @return Lista de pagos que coincidan con el estado.
     */
    List<Payment> findByStatus(String status);

    /**
     * Buscar pagos por el cliente ID.
     *
     * @param customerId ID del cliente asociado con el pago.
     * @return Lista de pagos del cliente.
     */
    List<Payment> findByCustomerId(String customerId);

    /**
     * Buscar pagos por transactionId.
     *
     * @param transactionId Identificador único de la transacción.
     * @return El pago con ese transactionId, si existe.
     */
    Payment findByTransactionId(String transactionId);
}