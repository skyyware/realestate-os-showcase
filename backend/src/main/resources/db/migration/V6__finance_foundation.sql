alter table finance_event add column event_type varchar(32) not null default 'EXPENSE';
alter table finance_event add column owner_unit_id uuid references owner_unit(id) on delete set null;
alter table finance_event add column allocation_key varchar(80) not null default 'MEA';
alter table finance_event add column counterparty varchar(180);
alter table finance_event add column invoice_number varchar(80);
alter table finance_event add column due_date date;
alter table finance_event add column paid_on date;
alter table finance_event add column document_reference varchar(240);

create table house_money_assessment (
  id uuid primary key,
  property_id uuid not null references property_asset(id) on delete cascade,
  unit_id uuid not null references owner_unit(id) on delete cascade,
  fiscal_year integer not null,
  monthly_house_money numeric(14,2) not null,
  monthly_reserve_contribution numeric(14,2) not null,
  valid_from date not null,
  status varchar(32) not null,
  created_at timestamp with time zone not null,
  unique(property_id, unit_id, fiscal_year)
);

create index idx_house_money_property_year on house_money_assessment(property_id, fiscal_year desc);
create index idx_house_money_unit_year on house_money_assessment(unit_id, fiscal_year desc);
