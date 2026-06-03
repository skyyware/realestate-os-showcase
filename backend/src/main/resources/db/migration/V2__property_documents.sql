create table property_document (
  id uuid primary key,
  property_id uuid not null references property_asset(id) on delete cascade,
  title varchar(180) not null,
  document_type varchar(80) not null,
  file_name varchar(240) not null,
  document_date date not null,
  created_at timestamp with time zone not null
);

create index idx_document_property_date on property_document(property_id, document_date desc);
