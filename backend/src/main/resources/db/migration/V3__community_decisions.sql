create table community_decision (
  id uuid primary key,
  property_id uuid not null references property_asset(id) on delete cascade,
  title varchar(180) not null,
  resolution_text varchar(1600) not null,
  meeting_date date not null,
  meeting_location varchar(180) not null,
  status varchar(32) not null,
  yes_votes integer not null,
  no_votes integer not null,
  abstentions integer not null,
  created_at timestamp with time zone not null
);

create index idx_decision_property_meeting on community_decision(property_id, meeting_date desc);
