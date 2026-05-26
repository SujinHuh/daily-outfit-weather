create table outfit_recommendations (
    id bigserial primary key,
    user_id bigint not null references users(id),
    target_date date not null,
    summary_message text not null,
    top_recommendation varchar(255),
    outer_recommendation varchar(255),
    item_recommendation varchar(255),
    character_image_type varchar(100),
    reason text,
    recommendation_type varchar(50) not null,
    weather_snapshot jsonb not null,
    created_at timestamptz not null,
    constraint uk_outfit_recommendations_user_date unique (user_id, target_date)
);
