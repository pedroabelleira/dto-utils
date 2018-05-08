--DROP TABLE OIBADMIN_ROLES;
CREATE TABLE OIBADMIN_ROLES (
  role_id INT,
  name VARCHAR,
  description VARCHAR
);

insert into OIBADMIN_ROLES values (1, 'ADMIN', 'Administrator');
insert into OIBADMIN_ROLES values (2, 'SUPERADMIN', 'Super administrator, no restrictions to what can be done');
insert into OIBADMIN_ROLES values (3, 'USER', 'A regular application user');

--DROP TABLE OIBADMIN_GROUPS;
CREATE TABLE OIBADMIN_GROUPS(
  group_id INT,
  name VARCHAR,
  description VARCHAR
);

insert into OIBADMIN_GROUPS values (1, 'ADMINISTRATORS', 'Administrator users');
insert into OIBADMIN_GROUPS values (2, 'USERS', 'Normal users');

--DROP TABLE OIBADMIN_GROUP_ROLE;
CREATE TABLE OIBADMIN_GROUP_ROLE (
  group_id INT,
  role_id INT
);

insert into OIBADMIN_GROUP_ROLE values (1, 1);
insert into OIBADMIN_GROUP_ROLE values (1, 2);
insert into OIBADMIN_GROUP_ROLE values (2, 3);

--DROP TABLE OIBADMIN_USER_GROUP;
CREATE TABLE OIBADMIN_USER_GROUP (
  user_id VARCHAR,
  group_id INT
);

insert into OIBADMIN_USER_GROUP values ('abellpe', 1);
insert into OIBADMIN_USER_GROUP values ('abellpe', 2);
insert into OIBADMIN_USER_GROUP values ('user', 2);
insert into OIBADMIN_USER_GROUP values ('mock_user1', 1);
insert into OIBADMIN_USER_GROUP values ('mock_user2', 2);


-- select distinct *
-- from OIBADMIN_ROLES r
--   inner join OIBADMIN_GROUP_ROLE gr on gr.role_id = r.role_id
--   inner join OIBADMIN_USER_GROUP ug on ug.group_id = gr.group_id


