alter table property_document add column status varchar(32) not null default 'RECEIVED';
alter table property_document add column visibility varchar(32) not null default 'ALL_OWNERS';
alter table property_document add column source varchar(80) not null default 'MANUAL';
alter table property_document add column description varchar(1000);
alter table property_document add column linked_entity_type varchar(32) not null default 'GENERAL';
alter table property_document add column linked_entity_id uuid;

create index idx_document_property_link on property_document(property_id, linked_entity_type, linked_entity_id);
create index idx_document_property_status on property_document(property_id, status);
