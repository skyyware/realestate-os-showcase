create table app_user (
  id uuid primary key,
  email varchar(320) not null unique,
  full_name varchar(180) not null,
  organization_name varchar(180) not null,
  password_hash varchar(120),
  status varchar(24) not null,
  role varchar(40) not null,
  created_at timestamp with time zone not null,
  activated_at timestamp with time zone
);

create table registration_token (
  id uuid primary key,
  user_id uuid not null references app_user(id) on delete cascade,
  token_hash varchar(128) not null unique,
  expires_at timestamp with time zone not null,
  used_at timestamp with time zone,
  created_at timestamp with time zone not null
);

create table property_asset (
  id uuid primary key,
  owner_id uuid not null references app_user(id) on delete cascade,
  name varchar(180) not null,
  address varchar(240) not null,
  city varchar(120) not null,
  unit_count integer not null,
  cash_balance numeric(14,2) not null,
  reserve_balance numeric(14,2) not null,
  created_at timestamp with time zone not null
);

create table owner_unit (
  id uuid primary key,
  property_id uuid not null references property_asset(id) on delete cascade,
  owner_name varchar(180) not null,
  unit_label varchar(80) not null,
  share_value numeric(10,2) not null
);

create table work_task (
  id uuid primary key,
  property_id uuid not null references property_asset(id) on delete cascade,
  title varchar(180) not null,
  description varchar(1000) not null,
  status varchar(32) not null,
  priority varchar(32) not null,
  due_date date,
  created_at timestamp with time zone not null
);

create table finance_event (
  id uuid primary key,
  property_id uuid not null references property_asset(id) on delete cascade,
  label varchar(180) not null,
  amount numeric(14,2) not null,
  category varchar(80) not null,
  booked_on date not null,
  status varchar(32) not null
);

create table activity_event (
  id uuid primary key,
  user_id uuid not null references app_user(id) on delete cascade,
  property_id uuid references property_asset(id) on delete cascade,
  event_type varchar(80) not null,
  summary varchar(240) not null,
  created_at timestamp with time zone not null
);

create index idx_property_owner on property_asset(owner_id);
create index idx_task_property on work_task(property_id);
create index idx_finance_property on finance_event(property_id);
create index idx_activity_user_created on activity_event(user_id, created_at desc);
