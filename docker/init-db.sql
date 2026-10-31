-- TicketHub Database Initialization Script
CREATE DATABASE IF NOT EXISTS snap_seat CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS tp_snapseat_booking CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS tickethub_payment CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS tickethub_notification CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

GRANT ALL PRIVILEGES ON snap_seat.* TO 'root'@'%';
GRANT ALL PRIVILEGES ON tp_snapseat_booking.* TO 'root'@'%';
GRANT ALL PRIVILEGES ON tickethub_payment.* TO 'root'@'%';
GRANT ALL PRIVILEGES ON tickethub_notification.* TO 'root'@'%';
FLUSH PRIVILEGES;
