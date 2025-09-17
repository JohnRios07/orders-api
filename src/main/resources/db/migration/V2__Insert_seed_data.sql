-- Insert admin user
INSERT INTO users (id, name, email, password_hash, role) VALUES
('admin-uuid-12345', 'Admin', 'admin@bocados.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'ADMIN');

-- Insert test user with simple password (password = 'admin123')
INSERT INTO users (id, name, email, password_hash, role) VALUES
('550e8400-e29b-41d4-a716-446655440001', 'Test User', 'test@example.com', '$2a$10$8K3W2Vz9V1LrO8nJ8Vz9V1LrO8nJ8Vz9V1LrO8nJ8Vz9V1LrO8nJ', 'OPERATOR');

-- Insert demo orders
INSERT INTO orders (id, customer_name, phone, items, subtotal, delivery_fee, total, payment_method, address, notes, status) VALUES
('order-uuid-1', 'Juan Pérez', '3001234567', '[{"name": "Pizza Margherita", "variant": "Grande", "qty": 1, "price": 25000, "line_total": 25000}, {"name": "Coca Cola", "qty": 2, "price": 5000, "line_total": 10000}]', 35000, 3000, 38000, 'EFECTIVO', 'Calle 123 #45-67, Bogotá', 'Sin cebolla', 'RECIBIDO'),
('order-uuid-2', 'María García', '3019876543', '[{"name": "Hamburguesa", "variant": "Doble", "qty": 1, "price": 18000, "line_total": 18000}]', 18000, 2500, 20500, 'NEQUI', 'Carrera 89 #12-34, Medellín', NULL, 'PREPARANDO'),
('order-uuid-3', 'Carlos López', '3024567890', '[{"name": "Ensalada César", "qty": 1, "price": 15000, "line_total": 15000}, {"name": "Agua", "qty": 1, "price": 3000, "line_total": 3000}]', 18000, 2000, 20000, 'TARJETA', 'Avenida 68 #23-45, Cali', 'Extra aderezo', 'LISTO');