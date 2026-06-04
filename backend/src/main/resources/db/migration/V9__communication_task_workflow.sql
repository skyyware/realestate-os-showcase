alter table work_task add column assignee_role varchar(80) not null default 'Verwaltung';
alter table work_task add column source_type varchar(40) not null default 'MANUAL';
alter table work_task add column source_id uuid;
alter table work_task add column reminder_date date;
alter table work_task add column completed_at timestamp with time zone;

create index idx_task_property_due_status on work_task(property_id, due_date, status);
create index idx_task_source on work_task(source_type, source_id);

alter table community_message add column channel varchar(40) not null default 'EMAIL';
alter table community_message add column source_type varchar(40) not null default 'MANUAL';
alter table community_message add column source_id uuid;
alter table community_message add column follow_up_task_id uuid references work_task(id) on delete set null;
alter table community_message add column ready_to_send_on date;
alter table community_message add column sent_at timestamp with time zone;

create index idx_community_message_source on community_message(source_type, source_id);
create index idx_community_message_follow_up_task on community_message(follow_up_task_id);
