alter table app_user add column identity_provider varchar(80) not null default 'local';
alter table app_user add column external_subject varchar(180);

create unique index idx_app_user_external_identity on app_user(identity_provider, external_subject);

create table audit_log (
  id uuid primary key,
  actor_id uuid not null references app_user(id) on delete cascade,
  action varchar(120) not null,
  target_type varchar(120) not null,
  target_id uuid,
  summary varchar(300) not null,
  occurred_at timestamp with time zone not null
);

create index idx_audit_actor_time on audit_log(actor_id, occurred_at desc);
create index idx_audit_target on audit_log(target_type, target_id);

create table annual_plan (
  id uuid primary key,
  property_id uuid not null references property_asset(id) on delete cascade,
  fiscal_year integer not null,
  house_money_budget numeric(14,2) not null,
  maintenance_budget numeric(14,2) not null,
  reserve_contribution numeric(14,2) not null,
  status varchar(32) not null,
  created_at timestamp with time zone not null
);

create index idx_annual_plan_property_year on annual_plan(property_id, fiscal_year desc);

create table owner_meeting (
  id uuid primary key,
  property_id uuid not null references property_asset(id) on delete cascade,
  title varchar(180) not null,
  meeting_date date not null,
  location varchar(180) not null,
  agenda varchar(1800) not null,
  status varchar(32) not null,
  created_at timestamp with time zone not null
);

create index idx_owner_meeting_property_date on owner_meeting(property_id, meeting_date desc);

create table community_message (
  id uuid primary key,
  property_id uuid not null references property_asset(id) on delete cascade,
  audience varchar(120) not null,
  subject varchar(180) not null,
  message varchar(1200) not null,
  status varchar(32) not null,
  created_at timestamp with time zone not null
);

create index idx_community_message_property_created on community_message(property_id, created_at desc);
