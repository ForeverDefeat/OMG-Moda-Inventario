INSERT INTO producto (id_producto, nombre, categoria, marca, imagen_url) VALUES
(1, 'Camisa Oxford', 'Camisas', 'OMG MODA', 'https://images.unsplash.com/photo-1521572163474-6864f9cf17ab?w=400&q=80'),
(2, 'Blazer Ejecutivo', 'Sacos', 'OMG MODA', 'https://images.unsplash.com/photo-1624378439575-d8705ad7ae80?w=400&q=80'),
(3, 'Pantalon Palazzo', 'Pantalones', 'OMG MODA', 'https://images.unsplash.com/photo-1542272604-780c8d4bb9f3?w=400&q=80'),
(4, 'Vestido Midi', 'Vestidos', 'OMG MODA', 'https://images.unsplash.com/photo-1588850561407-ed78c282e89b?w=400&q=80'),
(5, 'Top Satinado', 'Tops', 'OMG MODA', 'https://images.unsplash.com/photo-1556821840-3a63f95609a7?w=400&q=80'),
(6, 'Falda Plisada', 'Faldas', 'OMG MODA', 'https://images.unsplash.com/photo-1618354691373-d851c5c3a990?w=400&q=80');

INSERT INTO variante_producto (
    id_variante, id_producto, talla, color, material, precio_costo, precio_venta, stock_actual, stock_minimo
) VALUES
(1, 1, 'M', 'Azul', 'Algodon', 45.00, 89.90, 45, 8),
(2, 2, 'L', 'Negro', 'Lino', 120.00, 249.90, 7, 10),
(3, 3, 'S', 'Beige', 'Viscosa', 58.00, 119.90, 22, 6),
(4, 4, 'M', 'Terracota', 'Rayon', 82.00, 169.90, 0, 5),
(5, 5, 'S', 'Marfil', 'Saten', 34.00, 79.90, 34, 10),
(6, 6, 'M', 'Verde', 'Poliester', 40.00, 99.90, 12, 8);

INSERT INTO cliente (id_cliente, nombre, correo, telefono, segmento, total_compras, ultima_compra) VALUES
(1, 'Mariela Torres', 'mariela@email.com', '999 120 554', 'VIP', 1860.00, DATE_SUB(CURRENT_DATE(), INTERVAL 2 DAY)),
(2, 'Andrea Rojas', 'andrea@email.com', '988 771 042', 'Frecuente', 920.00, DATE_SUB(CURRENT_DATE(), INTERVAL 4 DAY)),
(3, 'Lucia Benavides', 'lucia@email.com', '977 113 898', 'Nuevo', 169.90, CURRENT_DATE()),
(4, 'Carolina Vega', 'carolina@email.com', '955 803 210', 'VIP', 2420.00, DATE_SUB(CURRENT_DATE(), INTERVAL 6 DAY));

INSERT INTO movimiento (id_movimiento, id_variante, id_usuario, tipo, cantidad, motivo, fecha) VALUES
(1, 1, 1, 'ENTRADA', 47, 'Seed inicial de inventario', CAST(CONCAT(CURRENT_DATE(), ' 09:00:00') AS DATETIME)),
(2, 1, 2, 'SALIDA', 2, 'Venta seed #101', CAST(CONCAT(CURRENT_DATE(), ' 10:30:00') AS DATETIME)),
(3, 2, 1, 'ENTRADA', 7, 'Seed inicial de inventario', CAST(CONCAT(CURRENT_DATE(), ' 09:05:00') AS DATETIME)),
(4, 3, 1, 'ENTRADA', 22, 'Seed inicial de inventario', CAST(CONCAT(CURRENT_DATE(), ' 09:10:00') AS DATETIME)),
(5, 4, 1, 'AJUSTE', 0, 'Producto agotado en seed', CAST(CONCAT(CURRENT_DATE(), ' 09:15:00') AS DATETIME)),
(6, 5, 1, 'ENTRADA', 35, 'Seed inicial de inventario', CAST(CONCAT(DATE_SUB(CURRENT_DATE(), INTERVAL 1 DAY), ' 09:20:00') AS DATETIME)),
(7, 5, 2, 'SALIDA', 1, 'Venta seed #102', CAST(CONCAT(DATE_SUB(CURRENT_DATE(), INTERVAL 1 DAY), ' 12:20:00') AS DATETIME)),
(8, 6, 1, 'ENTRADA', 12, 'Seed inicial de inventario', CAST(CONCAT(CURRENT_DATE(), ' 09:25:00') AS DATETIME));

INSERT INTO venta (id_venta, id_usuario, estado, metodo_pago, fecha) VALUES
(101, 2, 'COMPLETADA', 'EFECTIVO', CAST(CONCAT(CURRENT_DATE(), ' 10:30:00') AS DATETIME)),
(102, 2, 'COMPLETADA', 'YAPE', CAST(CONCAT(DATE_SUB(CURRENT_DATE(), INTERVAL 1 DAY), ' 12:20:00') AS DATETIME));

INSERT INTO detalle_venta (id_detalle, id_venta, id_variante, cantidad, precio_unitario) VALUES
(1, 101, 1, 2, 89.90),
(2, 102, 5, 1, 79.90);
