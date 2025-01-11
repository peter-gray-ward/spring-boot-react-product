
-- Insert seed data for IMAGE table
INSERT INTO "IMAGE" (id, src) VALUES 
(1, decode('89504E470D0A1A0A0000000D4948445200000001000000010806000000', 'hex')),
(2, decode('FFD8FFE000104A46494600010101006000600000FFDB00430008060607060508', 'hex')),
(3, decode('47494638396101000100800000FF00FFFFFF21F90401000001002C00000000', 'hex')),
(4, decode('424D360400000000000036000000280000000200000002000000010008000000', 'hex')),
(5, decode('5249464626D70057415645666D7420100000000100010044AC000010B1020000', 'hex')),
(6, decode('0000010000010001002000FFFFFF000000000000000000000000000000000000', 'hex')),
(7, decode('424D860300000000000036000000280000000100000001000000010004000000', 'hex')),
(8, decode('FFD8FFE000104A46494600010101004800480000FFDB00430009060607060509', 'hex')),
(9, decode('89504E470D0A1A0A0000000D49484452000001000000010008020000005C72A0', 'hex')),
(10, decode('FFD8FFE000104A46494600010101004800480000FFDB00430008080808080808', 'hex'));


-- Insert seed data for PRODUCT table
INSERT INTO "PRODUCT" (id, name, price, description, image_id) VALUES
(1, 'Laptop', 999.99, 'High-performance laptop with 16GB RAM.', 1),
(2, 'Smartphone', 699.49, 'Latest model with excellent camera.', 2),
(3, 'Headphones', 129.99, 'Noise-cancelling over-ear headphones.', 3),
(4, 'Smartwatch', 199.89, 'Fitness-focused smartwatch.', 4),
(5, 'Gaming Console', 499.95, 'Next-gen gaming console.', 5),
(6, 'Tablet', 299.59, 'Portable tablet with 10-inch display.', 6),
(7, 'Wireless Mouse', 29.99, 'Ergonomic wireless mouse.', 7),
(8, 'Keyboard', 49.99, 'Mechanical keyboard with RGB lighting.', 8),
(9, 'Monitor', 149.79, '24-inch Full HD monitor.', 9),
(10, 'Speaker', 89.99, 'Portable Bluetooth speaker.', 10);
