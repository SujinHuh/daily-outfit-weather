create table users (
    id bigserial primary key,
    email varchar(255) not null unique,
    nickname varchar(50) not null,
    provider varchar(20) not null,
    provider_id varchar(255) not null,
    created_at timestamptz not null,
    updated_at timestamptz not null,
    constraint uk_users_provider_provider_id unique (provider, provider_id)
);

create table user_profiles (
    id bigserial primary key,
    user_id bigint not null unique references users(id),
    cold_sensitivity integer not null,
    heat_sensitivity integer not null,
    commute_time time not null,
    leave_work_time time not null,
    notification_time time not null,
    transport_type varchar(30) not null,
    message_tone varchar(30) not null,
    change_alert_option varchar(30) not null,
    created_at timestamptz not null,
    updated_at timestamptz not null,
    constraint ck_user_profiles_cold_sensitivity check (cold_sensitivity between 1 and 5),
    constraint ck_user_profiles_heat_sensitivity check (heat_sensitivity between 1 and 5)
);

create table locations (
    id bigserial primary key,
    user_id bigint not null references users(id),
    type varchar(20) not null,
    sido varchar(50) not null,
    sigungu varchar(50) not null,
    dong varchar(50) not null,
    nx integer,
    ny integer,
    created_at timestamptz not null,
    updated_at timestamptz not null,
    constraint uk_locations_user_type unique (user_id, type)
);
