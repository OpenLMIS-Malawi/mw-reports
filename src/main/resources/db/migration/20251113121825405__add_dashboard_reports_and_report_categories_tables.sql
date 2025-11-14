CREATE TABLE IF NOT EXISTS reports.report_categories (
    id UUID PRIMARY KEY NOT NULL,
    name text UNIQUE NOT NULL
);

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM reports.report_categories
        WHERE name = 'LMIS Reports'
    ) THEN
        INSERT INTO reports.report_categories (id, name) VALUES ('55e19d72-4d0b-4453-b627-2f3477681c24', 'LMIS Reports');
    END IF;
END $$;

CREATE TABLE IF NOT EXISTS reports.dashboard_reports (
    id UUID PRIMARY KEY NOT NULL,
    name text UNIQUE NOT NULL,
    url text NOT NULL,
    type text NOT NULL,
    enabled boolean NOT NULL,
    showOnHomePage boolean NOT NULL,
    categoryId UUID NOT NULL,
    rightName text UNIQUE NOT NULL,
    CONSTRAINT fk_report_categories FOREIGN KEY (categoryId) REFERENCES reports.report_categories (id) ON DELETE RESTRICT
);