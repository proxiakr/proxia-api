-- Create git_repositories table
CREATE TABLE git_repositories (
    id UUID PRIMARY KEY,
    git_integration_id UUID NOT NULL,
    full_name VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    deleted_at TIMESTAMP,
    CONSTRAINT fk_git_repositories_integration
        FOREIGN KEY (git_integration_id)
        REFERENCES git_integrations(id)
);

-- Add git_repository_id column to app_resources
ALTER TABLE app_resources
ADD COLUMN git_repository_id UUID,
ADD CONSTRAINT fk_app_resources_git_repository
    FOREIGN KEY (git_repository_id)
    REFERENCES git_repositories(id);

-- Create indexes
CREATE INDEX idx_git_repos_integration
    ON git_repositories(git_integration_id, deleted_at);

CREATE INDEX idx_git_repos_fullname
    ON git_repositories(full_name, deleted_at);

CREATE INDEX idx_app_resources_git_repository
    ON app_resources(git_repository_id);
