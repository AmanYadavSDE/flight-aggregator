-- Flight Booking Aggregator - Database Initialization
-- This creates ONLY the tables needed for an aggregator (like MMT)
-- We DON'T create flights, inventory, airlines, etc. (those belong to airlines)

-- Create test user for API testing
INSERT INTO users (email, phone, password_hash, status) VALUES
('test@example.com', '+1234567890', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi', 'ACTIVE')
ON CONFLICT (email) DO NOTHING;

-- Create user profile
INSERT INTO user_profiles (user_id, first_name, last_name, date_of_birth, nationality)
SELECT u.id, 'Test', 'User', '1990-01-01', 'Indian'
FROM users u 
WHERE u.email = 'test@example.com'
ON CONFLICT (user_id) DO NOTHING;

-- Create indexes for better performance
CREATE INDEX IF NOT EXISTS idx_bookings_user ON bookings(user_id);
CREATE INDEX IF NOT EXISTS idx_bookings_reference ON bookings(booking_reference);
CREATE INDEX IF NOT EXISTS idx_bookings_airline_pnr ON bookings(airline_pnr);
CREATE INDEX IF NOT EXISTS idx_payments_booking ON payments(booking_id);
CREATE INDEX IF NOT EXISTS idx_payments_transaction ON payments(transaction_id);
CREATE INDEX IF NOT EXISTS idx_passengers_booking ON passengers(booking_id);
