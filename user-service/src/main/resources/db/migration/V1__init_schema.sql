CREATE TABLE users
(
    user_id           UUID PRIMARY KEY,
    phone_number      VARCHAR(20) NOT NULL UNIQUE,
    first_name        VARCHAR(64),
    last_name         VARCHAR(64),
    tag               VARCHAR(64),

    phone_is_visible  BOOLEAN     NOT NULL DEFAULT TRUE,

    profile_completed BOOLEAN     NOT NULL DEFAULT FALSE,

    created_at        TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at        TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CHECK ( profile_completed = FALSE OR first_name IS NOT NULL ),
    CHECK ( tag IS NULL OR LENGTH(tag) >=5 )
);