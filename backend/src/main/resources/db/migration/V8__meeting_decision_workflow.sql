alter table owner_meeting add column invitation_sent_on date;
alter table owner_meeting add column response_deadline date;
alter table owner_meeting add column quorum_requirement varchar(240) not null default 'Einfache Mehrheit nach MEA';

alter table community_decision add column meeting_id uuid references owner_meeting(id) on delete set null;
alter table community_decision add column agenda_item varchar(240) not null default 'Allgemein';
alter table community_decision add column implementation_due_date date;
alter table community_decision add column responsible_role varchar(80) not null default 'Verwaltung';
alter table community_decision add column cost_impact numeric(14,2) not null default 0;

create index idx_decision_meeting on community_decision(meeting_id);
create index idx_decision_property_due on community_decision(property_id, implementation_due_date);
