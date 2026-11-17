CREATE TABLE documents (
    id UUID PRIMARY KEY,
    original_filename VARCHAR(255) NOT NULL,
    storage_filename VARCHAR(255) NOT NULL,
    content_type VARCHAR(100) NOT NULL,
    file_size BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL,
    extracted_text TEXT,
    uploaded_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    CONSTRAINT documents_status_check CHECK (status IN ('UPLOADED', 'PROCESSED', 'FAILED'))
);

CREATE INDEX idx_documents_uploaded_at ON documents (uploaded_at);
