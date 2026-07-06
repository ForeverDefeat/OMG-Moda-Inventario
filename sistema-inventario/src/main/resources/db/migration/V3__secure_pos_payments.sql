ALTER TABLE VARIANTE_PRODUCTO
    ADD COLUMN IF NOT EXISTS stock_reservado INT NOT NULL DEFAULT 0;

ALTER TABLE VENTA
    MODIFY COLUMN estado VARCHAR(24) NOT NULL,
    ADD COLUMN IF NOT EXISTS completed_at DATETIME NULL,
    ADD COLUMN IF NOT EXISTS cancelled_at DATETIME NULL,
    ADD COLUMN IF NOT EXISTS expired_at DATETIME NULL,
    ADD COLUMN IF NOT EXISTS idempotency_key VARCHAR(120) NULL,
    ADD COLUMN IF NOT EXISTS idempotency_payload_hash VARCHAR(128) NULL;

UPDATE VENTA SET estado = 'COMPLETED', completed_at = COALESCE(completed_at, fecha) WHERE estado = 'COMPLETADA';
UPDATE VENTA SET estado = 'CANCELLED', cancelled_at = COALESCE(cancelled_at, fecha) WHERE estado = 'ANULADA';
UPDATE VENTA SET estado = 'EXPIRED', expired_at = COALESCE(expired_at, fecha) WHERE estado = 'PENDIENTE';

CREATE TABLE IF NOT EXISTS STOCK_RESERVA (
    id_reserva BIGINT PRIMARY KEY AUTO_INCREMENT,
    id_venta BIGINT NOT NULL,
    id_variante BIGINT NOT NULL,
    cantidad INT NOT NULL,
    estado VARCHAR(20) NOT NULL,
    created_at DATETIME NOT NULL,
    expires_at DATETIME NOT NULL,
    released_at DATETIME NULL
);

CREATE TABLE IF NOT EXISTS PAYMENT_INTENT (
    id_payment BIGINT PRIMARY KEY AUTO_INCREMENT,
    id_venta BIGINT NOT NULL,
    method VARCHAR(20) NOT NULL,
    provider VARCHAR(20) NOT NULL,
    provider_reference VARCHAR(80) NOT NULL,
    amount_due DECIMAL(12,2) NOT NULL,
    amount_received DECIMAL(12,2) NULL,
    change_amount DECIMAL(12,2) NULL,
    currency VARCHAR(3) NOT NULL,
    status VARCHAR(24) NOT NULL,
    payment_reference VARCHAR(120) NULL,
    expires_at DATETIME NOT NULL,
    created_at DATETIME NOT NULL,
    confirmed_at DATETIME NULL,
    CONSTRAINT uk_payment_provider_reference UNIQUE (provider_reference)
);

INSERT INTO PAYMENT_INTENT (
    id_venta, method, provider, provider_reference, amount_due, amount_received,
    change_amount, currency, status, payment_reference, expires_at, created_at, confirmed_at
)
SELECT
    v.id_venta,
    CASE WHEN v.metodo_pago = 'TARJETA' THEN 'CARD' ELSE v.metodo_pago END,
    'LEGACY',
    CONCAT('LEGACY-', v.id_venta),
    COALESCE(SUM(d.cantidad * d.precio_unitario), 0),
    COALESCE(SUM(d.cantidad * d.precio_unitario), 0),
    0,
    'PEN',
    'MANUALLY_CONFIRMED',
    'Migracion legacy',
    v.fecha,
    v.fecha,
    v.completed_at
FROM VENTA v
JOIN DETALLE_VENTA d ON d.id_venta = v.id_venta
WHERE v.estado = 'COMPLETED'
  AND NOT EXISTS (SELECT 1 FROM PAYMENT_INTENT p WHERE p.id_venta = v.id_venta)
GROUP BY v.id_venta, v.metodo_pago, v.fecha, v.completed_at;

CREATE TABLE IF NOT EXISTS PAYMENT_AUDIT (
    id_audit BIGINT PRIMARY KEY AUTO_INCREMENT,
    id_payment BIGINT NULL,
    id_venta BIGINT NULL,
    event_type VARCHAR(40) NOT NULL,
    previous_status VARCHAR(30) NULL,
    new_status VARCHAR(30) NULL,
    user_id BIGINT NULL,
    user_role VARCHAR(20) NULL,
    method VARCHAR(20) NULL,
    amount DECIMAL(12,2) NULL,
    currency VARCHAR(3) NULL,
    reference VARCHAR(120) NULL,
    observation VARCHAR(500) NULL,
    provider_event_id VARCHAR(120) NULL,
    payload_hash VARCHAR(128) NULL,
    payload TEXT NULL,
    created_at DATETIME NOT NULL
);

CREATE TABLE IF NOT EXISTS PAYMENT_WEBHOOK_EVENT (
    id_event BIGINT PRIMARY KEY AUTO_INCREMENT,
    provider VARCHAR(30) NOT NULL,
    provider_event_id VARCHAR(120) NOT NULL,
    payload_hash VARCHAR(128) NOT NULL,
    payload TEXT NULL,
    received_at DATETIME NOT NULL,
    processed_at DATETIME NULL,
    status VARCHAR(20) NOT NULL,
    CONSTRAINT uk_payment_provider_event UNIQUE (provider_event_id)
);

CREATE TABLE IF NOT EXISTS VENTA_IDEMPOTENCY (
    idempotency_key VARCHAR(120) PRIMARY KEY,
    payload_hash VARCHAR(128) NOT NULL,
    id_venta BIGINT NOT NULL,
    id_payment BIGINT NOT NULL,
    response_payload TEXT NULL,
    created_at DATETIME NOT NULL
);
