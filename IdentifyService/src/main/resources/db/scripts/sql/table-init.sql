-- Thêm dữ liệu mẫu cho Permission
INSERT INTO permissions (name, description) VALUES ('0000', 'Cho phép đọc thông tin');
INSERT INTO permissions (name, description) VALUES ('0001', 'Cho phép chỉnh sửa thông tin người dùng');
INSERT INTO permissions (name, description) VALUES ('1111', 'Cho phép xóa người dùng');
INSERT INTO permissions (name, description) VALUES ('0002', 'Cho phép đọc thông tin và vai trò');
INSERT INTO permissions (name, description) VALUES ('0003', 'Cho phép chỉnh sửa thông tin vai trò');

-- Thêm dữ liệu mẫu cho Role
INSERT INTO roles (name, description) VALUES ('ADMIN', 'Quản trị viên hệ thống');
INSERT INTO roles (name, description) VALUES ('USER', 'Người dùng thông thường');
INSERT INTO roles (name, description) VALUES ('MANAGER', 'Quản lý hệ thống');


-- Thiết lập quyền cho vai trò
-- ADMIN có tất cả quyền
INSERT INTO roles_permissions (role_name, permission_name) VALUES ('ADMIN', '0000');
INSERT INTO roles_permissions (role_name, permission_name) VALUES ('ADMIN', '0001');
INSERT INTO roles_permissions (role_name, permission_name) VALUES ('ADMIN', '1111');
INSERT INTO roles_permissions (role_name, permission_name) VALUES ('ADMIN', '0002');
INSERT INTO roles_permissions (role_name, permission_name) VALUES ('ADMIN', '0003');

-- MANAGER có quyền đọc và chỉnh sửa
INSERT INTO roles_permissions (role_name, permission_name) VALUES ('MANAGER', '0000');
INSERT INTO roles_permissions (role_name, permission_name) VALUES ('MANAGER', '0001');
INSERT INTO roles_permissions (role_name, permission_name) VALUES ('MANAGER', '0002');

-- USER chỉ có quyền đọc
INSERT INTO roles_permissions (role_name, permission_name) VALUES ('USER', '0000');

-- Thêm dữ liệu mẫu cho User
INSERT INTO users (id, username, password, status) VALUES
    ('22b9091a-7b72-45c0-b3b3-a362944ac05c', 'adminG18', '$2a$10$HRMp2QPIjgagHS3aVNrFAeNKrBlLsU6hdqvhr3RTNUQE2DPjxosjO', 'ACTIVE');
INSERT INTO users (id, username, password, status) VALUES
    ('6cdeda14-88be-4c9f-9a51-fe0550956303', 'managerG18', '$2a$10$6iO1LpDaRiDId46hXjKuIOItan5Cj/U8/pLhXAMdrHUrs.fIJyHNK', 'ACTIVE');

-- Gán vai trò cho người dùng
INSERT INTO users_roles (user_id, role_name) VALUES ('22b9091a-7b72-45c0-b3b3-a362944ac05c', 'ADMIN');
INSERT INTO users_roles (user_id, role_name) VALUES ('6cdeda14-88be-4c9f-9a51-fe0550956303', 'MANAGER');