alter table property_document add column storage_key varchar(520);
alter table property_document add column content_type varchar(160);
alter table property_document add column file_size_bytes bigint;
alter table property_document add column sha256_checksum varchar(64);
alter table property_document add column uploaded_at timestamp with time zone;

create index idx_document_storage_key on property_document(storage_key);
