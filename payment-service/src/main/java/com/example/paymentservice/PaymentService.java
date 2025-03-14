package com.example.paymentservice;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PaymentService {

    /**
     * Crear un nuevo pago.
     *
     * @param payment Objeto Payment con la información del pago a crear.
     * @return El objeto Payment creado.
     */
    Payment createPayment(Payment payment);

    /**
     * Obtener un pago por su ID.
     *
     * @param id Identificador único del pago.
     * @return El objeto Payment, envuelto en un Optional si existe,
     *         o un Optional vacío si no existe.
     */
    Optional<Payment> getPaymentById(UUID id);

    /**
     * Obtener una lista de todos los pagos.
     *
     * @return Lista de objetos Payment.
     */
    List<Payment> getAllPayments();

    /**
     * Actualizar un pago existente.
     *
     * @param id Identificador único del pago a actualizar.
     * @param updatedPayment Objeto Payment con la información actualizada.
     * @return El objeto Payment actualizado.
     */
    Payment updatePayment(UUID id, Payment updatedPayment);

    /**
     * Eliminar un pago por su ID.
     *
     * @param id Identificador único del pago a eliminar.
     */
    void deletePayment(UUID id);
}