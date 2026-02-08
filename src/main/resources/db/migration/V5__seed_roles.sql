INSERT INTO roles (role_name)
values ('ROLE_USER'),('ROLE_SELLER'),('ROLE_ADMIN')
ON CONFLICT (role_name) DO NOTHING;