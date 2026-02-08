-- users
CREATE TABLE users
(
    id          UUID PRIMARY KEY,
    email       VARCHAR(255) NOT NULL,
    name        VARCHAR(255) NOT NULL,
    provider    VARCHAR(50)  NOT NULL,
    provider_id VARCHAR(255) NOT NULL,
    role        VARCHAR(50)  NOT NULL DEFAULT 'USER',
    created_at  TIMESTAMP    NOT NULL,
    updated_at  TIMESTAMP    NOT NULL,
    UNIQUE (provider, provider_id)
);

CREATE INDEX idx_users_email ON users (email);

-- refresh_tokens
CREATE TABLE refresh_tokens
(
    id         UUID PRIMARY KEY,
    user_id    UUID         NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    token      VARCHAR(512) NOT NULL,
    expires_at TIMESTAMP    NOT NULL,
    created_at TIMESTAMP    NOT NULL,
    updated_at TIMESTAMP    NOT NULL
);

CREATE UNIQUE INDEX idx_refresh_tokens_token ON refresh_tokens (token);
CREATE INDEX idx_refresh_tokens_user_id ON refresh_tokens (user_id);
CREATE INDEX idx_refresh_tokens_expires_at ON refresh_tokens (expires_at);

-- workspaces
CREATE TABLE workspaces
(
    id         UUID PRIMARY KEY,
    name       VARCHAR(255) NOT NULL,
    created_at TIMESTAMP    NOT NULL,
    updated_at TIMESTAMP    NOT NULL
);

-- workspace_members
CREATE TABLE workspace_members
(
    id           UUID PRIMARY KEY,
    workspace_id UUID        NOT NULL REFERENCES workspaces (id) ON DELETE CASCADE,
    user_id      UUID        NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    role         VARCHAR(50) NOT NULL,
    created_at   TIMESTAMP   NOT NULL,
    updated_at   TIMESTAMP   NOT NULL,
    UNIQUE (workspace_id, user_id)
);

CREATE INDEX idx_workspace_members_user_id ON workspace_members (user_id);

-- git_connections
CREATE TABLE git_connections
(
    id              UUID PRIMARY KEY,
    workspace_id    UUID         NOT NULL REFERENCES workspaces (id) ON DELETE CASCADE,
    provider        VARCHAR(50)  NOT NULL,
    installation_id VARCHAR(255) NOT NULL,
    access_token    VARCHAR(512),
    expires_at      TIMESTAMP,
    created_at      TIMESTAMP    NOT NULL,
    updated_at      TIMESTAMP    NOT NULL
);

CREATE INDEX idx_git_connections_workspace_id ON git_connections (workspace_id);

-- projects
CREATE TABLE projects
(
    id           UUID PRIMARY KEY,
    name         VARCHAR(255) NOT NULL,
    subdomain    VARCHAR(255) NOT NULL UNIQUE,
    workspace_id UUID         NOT NULL REFERENCES workspaces (id) ON DELETE CASCADE,
    created_at   TIMESTAMP    NOT NULL,
    updated_at   TIMESTAMP    NOT NULL
);

CREATE INDEX idx_projects_workspace_id ON projects (workspace_id);

-- services (discriminator: type)
CREATE TABLE services
(
    id         UUID PRIMARY KEY,
    type       VARCHAR(50)      NOT NULL,
    name       VARCHAR(255)     NOT NULL,
    x          DOUBLE PRECISION NOT NULL DEFAULT 0,
    y          DOUBLE PRECISION NOT NULL DEFAULT 0,
    project_id UUID             NOT NULL REFERENCES projects (id) ON DELETE CASCADE,
    status     VARCHAR(50)      NOT NULL,
    created_at TIMESTAMP        NOT NULL,
    updated_at TIMESTAMP        NOT NULL
);

CREATE INDEX idx_services_project_id ON services (project_id);
CREATE INDEX idx_services_status ON services (status);

-- app_services
CREATE TABLE app_services
(
    id                UUID PRIMARY KEY REFERENCES services (id) ON DELETE CASCADE,
    repo_full_name    VARCHAR(255) NOT NULL,
    branch            VARCHAR(255) NOT NULL,
    port              INTEGER      NOT NULL,
    framework         VARCHAR(50)  NOT NULL,
    root_directory    VARCHAR(500),
    build_command     VARCHAR(500),
    start_command     VARCHAR(500),
    git_connection_id UUID         NOT NULL REFERENCES git_connections (id) ON DELETE CASCADE
);

CREATE INDEX idx_app_services_git_connection_id ON app_services (git_connection_id);

-- database_services
CREATE TABLE database_services
(
    id       UUID PRIMARY KEY REFERENCES services (id) ON DELETE CASCADE,
    engine   VARCHAR(50)  NOT NULL,
    version  VARCHAR(50)  NOT NULL,
    database VARCHAR(255) NOT NULL,
    username VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL
);

-- environment_variables
CREATE TABLE environment_variables
(
    id         UUID PRIMARY KEY,
    service_id UUID         NOT NULL REFERENCES services (id) ON DELETE CASCADE,
    "key"      VARCHAR(255) NOT NULL,
    value      TEXT         NOT NULL,
    is_secret  BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP    NOT NULL,
    updated_at TIMESTAMP    NOT NULL,
    UNIQUE (service_id, "key")
);

CREATE INDEX idx_environment_variables_service_id ON environment_variables (service_id);

-- deployments
CREATE TABLE deployments
(
    id             UUID PRIMARY KEY,
    app_service_id UUID        NOT NULL REFERENCES app_services (id) ON DELETE CASCADE,
    status         VARCHAR(50) NOT NULL,
    commit_sha     VARCHAR(40) NOT NULL,
    commit_message TEXT        NOT NULL,
    started_at     TIMESTAMP,
    finished_at    TIMESTAMP,
    created_at     TIMESTAMP   NOT NULL,
    updated_at     TIMESTAMP   NOT NULL
);

CREATE INDEX idx_deployments_app_service_id ON deployments (app_service_id);
CREATE INDEX idx_deployments_status ON deployments (status);

-- deployment_logs
CREATE TABLE deployment_logs
(
    id            UUID PRIMARY KEY,
    deployment_id UUID        NOT NULL REFERENCES deployments (id) ON DELETE CASCADE,
    stage         VARCHAR(50) NOT NULL,
    status        VARCHAR(50) NOT NULL,
    started_at    TIMESTAMP,
    finished_at   TIMESTAMP,
    log_url       VARCHAR(500),
    created_at    TIMESTAMP   NOT NULL,
    updated_at    TIMESTAMP   NOT NULL
);

CREATE INDEX idx_deployment_logs_deployment_id ON deployment_logs (deployment_id);
