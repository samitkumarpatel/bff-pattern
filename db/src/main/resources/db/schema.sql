-- Create the user table
CREATE TABLE users (
       id SERIAL PRIMARY KEY,  -- Auto-incrementing primary key
       username VARCHAR(50) NOT NULL UNIQUE, -- Username must be unique
       password VARCHAR(50) NOT NULL, -- Password field
       activate BOOLEAN NOT NULL DEFAULT TRUE, -- User activation status
       doj DATE NOT NULL DEFAULT CURRENT_DATE -- Date of joining
);

-- Create the roles table with CHECK constraint
CREATE TABLE roles (
       id INT NOT NULL, -- Foreign key referencing users table
       role VARCHAR(10) NOT NULL, -- Role assigned to the user
       UNIQUE (id, role), -- Combination of userId and role must be unique
       CONSTRAINT fk_user FOREIGN KEY (id) REFERENCES users (id) ON DELETE CASCADE,
       CONSTRAINT chk_role CHECK (role IN ('READ', 'WRITE', 'ADMIN')) -- Restrict role values
);

-- Insert a user into the users table
INSERT INTO users (id, username, password, activate) VALUES (11,'admin', 'password', TRUE);

-- Insert roles for the user into the roles table
INSERT INTO roles (id, role) VALUES (11, 'ADMIN');
INSERT INTO roles (id, role) VALUES (11, 'READ');
INSERT INTO roles (id, role) VALUES (11, 'WRITE');


CREATE TABLE messages (
      id SERIAL PRIMARY KEY,
      sender_id INT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
      receiver_id INT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
      content TEXT NOT NULL,
      created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
      is_read BOOLEAN DEFAULT FALSE
);