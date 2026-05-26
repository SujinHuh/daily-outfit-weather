create table recommendation_feedbacks (
    id bigserial primary key,
    recommendation_id bigint not null unique references outfit_recommendations(id),
    user_id bigint not null references users(id),
    temperature_feedback varchar(20),
    rain_feedback varchar(20),
    comment text,
    created_at timestamptz not null,
    updated_at timestamptz not null
);

create index idx_recommendation_feedbacks_user_id on recommendation_feedbacks(user_id);

create table notification_logs (
    id bigserial primary key,
    user_id bigint not null references users(id),
    recommendation_id bigint references outfit_recommendations(id),
    notification_type varchar(50) not null,
    title varchar(255) not null,
    body text not null,
    scheduled_at timestamptz not null,
    sent_at timestamptz,
    status varchar(50) not null,
    failure_reason text,
    created_at timestamptz not null,
    constraint uk_notification_logs_user_type_scheduled_at unique (user_id, notification_type, scheduled_at)
);

create index idx_notification_logs_user_created_at on notification_logs(user_id, created_at desc);
create index idx_notification_logs_user_scheduled_at on notification_logs(user_id, scheduled_at);
