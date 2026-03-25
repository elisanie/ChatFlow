-- messages table: stores all chat messages for persistence and analytics
CREATE TABLE messages (

    -- unique message id (UUID) for idempotency
    message_id   VARCHAR(36)  NOT NULL,

    -- chat room identifier
    room_id      VARCHAR(50)  NOT NULL,

    -- sender identifier
    user_id      VARCHAR(50)  NOT NULL,

    -- message content (not indexed to avoid overhead)
    content      TEXT,

    -- timestamp in epoch milliseconds (used for ordering and range queries)
    ts           BIGINT       NOT NULL,

    -- primary key ensures uniqueness and fast lookup
    PRIMARY KEY (message_id),

    -- index for room-based queries (chat history)
    -- supports: WHERE room_id = ? AND ts BETWEEN ? AND ?
    INDEX idx_room_ts (room_id, ts),

    -- index for user-based queries (user history)
    -- supports: WHERE user_id = ? ORDER BY ts
    INDEX idx_user_ts (user_id, ts),

    -- index for global time-based analytics
    -- supports: WHERE ts BETWEEN ? AND ?
    INDEX idx_ts (ts)

) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- user_room_activity: pre-aggregated table for fast user-room analytics
-- stores message count and last activity time per user per room
CREATE TABLE user_room_activity (

    -- user identifier
    user_id       VARCHAR(50) NOT NULL,

    -- chat room identifier
    room_id       VARCHAR(50) NOT NULL,

    -- total number of messages sent by the user in this room
    message_count INT         NOT NULL DEFAULT 0,

    -- last message timestamp
    last_ts       BIGINT      NOT NULL,

    -- ensures one record per user-room pair
    PRIMARY KEY (user_id, room_id)

) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;