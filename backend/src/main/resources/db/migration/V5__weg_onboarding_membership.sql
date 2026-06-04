alter table property_asset add column fiscal_year integer not null default 2026;
alter table property_asset add column reserve_target numeric(14,2) not null default 0;
alter table property_asset add column share_total numeric(10,2) not null default 1000;
alter table property_asset add column management_mode varchar(32) not null default 'SELF_MANAGED';

alter table owner_unit add column owner_email varchar(320) not null default 'unknown@example.invalid';
alter table owner_unit add column occupancy_type varchar(32) not null default 'OWNER_OCCUPIED';
alter table owner_unit add column voting_weight numeric(10,2) not null default 0;
update owner_unit set voting_weight = share_value where voting_weight = 0;

create table community_member (
  id uuid primary key,
  property_id uuid not null references property_asset(id) on delete cascade,
  user_id uuid references app_user(id) on delete set null,
  full_name varchar(180) not null,
  email varchar(320) not null,
  role varchar(40) not null,
  status varchar(32) not null,
  invited_at timestamp with time zone,
  accepted_at timestamp with time zone,
  created_at timestamp with time zone not null,
  unique(property_id, email)
);

create index idx_community_member_property on community_member(property_id);
create index idx_community_member_email on community_member(email);

