alter table audit_log add column property_id uuid references property_asset(id) on delete set null;

create index idx_audit_property_time on audit_log(property_id, occurred_at desc);
