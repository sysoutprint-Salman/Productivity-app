CREATE TABLE IF NOT EXISTS tasks (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    date INTEGER,
    description TEXT,
    title TEXT,
    status TEXT,
    creation_date INTEGER,
    user_id INTEGER NOT NULL
);
CREATE TABLE IF NOT EXISTS users (
    user_id INTEGER PRIMARY KEY,
    username TEXT,
    email TEXT
);

CREATE TABLE IF NOT EXISTS boards (
    board_id INTEGER PRIMARY KEY,
    board_title TEXT NOT NULL,
    creation_date INTEGER,
    user_id INTEGER NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(user_id)
);

CREATE TABLE IF NOT EXISTS lists (
    list_id INTEGER PRIMARY KEY,
    board_id INTEGER NOT NULL,
    title TEXT,
    hex_color TEXT,
    status TEXT,
    list_position INTEGER,
    FOREIGN KEY (board_id) REFERENCES boards(board_id)
);

CREATE TABLE IF NOT EXISTS cards (
    card_id INTEGER PRIMARY KEY,
    board_id INTEGER NOT NULL,
    list_id INTEGER,
    description TEXT NOT NULL,
    hex_color TEXT,
    status TEXT,
    card_position INTEGER,
    FOREIGN KEY (board_id) REFERENCES boards(board_id),
    FOREIGN KEY (list_id) REFERENCES lists(list_id)
);

CREATE TABLE IF NOT EXISTS notebooks (
    notebook_id INTEGER PRIMARY KEY,
    tab_title TEXT,
    notebook_text TEXT,
    user_id INTEGER NOT NULL,
    hex_color TEXT,
    FOREIGN KEY (user_id) REFERENCES users(user_id)
);

CREATE TABLE IF NOT EXISTS reminders (
    reminder_id INTEGER PRIMARY KEY,
    board_id INTEGER NOT NULL,
    reminder_title TEXT NOT NULL,
    description TEXT,
    priority TEXT NOT NULL,
    due_date TEXT NOT NULL,
    FOREIGN KEY (board_id) REFERENCES boards(board_id)
);

CREATE TABLE IF NOT EXISTS gpt_responses (
    response_id INTEGER PRIMARY KEY,
    response TEXT,
    timestamp INTEGER,
    prompt TEXT,
    user_id INTEGER NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(user_id)
);

CREATE TABLE IF NOT EXISTS tasks (
    id INTEGER PRIMARY KEY,
    date INTEGER,
    description TEXT,
    title TEXT,
    status TEXT,
    creation_date INTEGER,
    user_id INTEGER NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(user_id)
);