CREATE UNIQUE INDEX ux_users_tag_lower
    ON users (LOWER(tag))
    WHERE tag IS NOT NULL;
