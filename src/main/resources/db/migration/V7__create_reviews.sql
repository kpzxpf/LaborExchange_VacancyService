CREATE TABLE company_reviews (
    id          BIGSERIAL PRIMARY KEY,
    company_id  BIGINT        NOT NULL REFERENCES companies(id) ON DELETE CASCADE,
    author_id   BIGINT        NOT NULL,
    rating      SMALLINT      NOT NULL CHECK (rating >= 1 AND rating <= 5),
    title       VARCHAR(200)  NOT NULL,
    text        TEXT          NOT NULL,
    created_at  TIMESTAMP     NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP     NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_reviews_company ON company_reviews (company_id);
CREATE INDEX idx_reviews_author  ON company_reviews (author_id);
-- One review per author per company
CREATE UNIQUE INDEX idx_unique_review ON company_reviews (company_id, author_id);
