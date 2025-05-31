CREATE SCHEMA IF NOT EXISTS fleet_manager;
create user fleet_manager with password 'fleet_manager_password';
GRANT USAGE ON SCHEMA fleet_manager TO fleet_manager;
GRANT ALL PRIVILEGES ON SCHEMA fleet_manager TO fleet_manager;