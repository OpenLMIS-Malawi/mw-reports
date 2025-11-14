ALTER TABLE reports.jasper_templates DROP COLUMN category;

ALTER TABLE reports.jasper_templates ADD COLUMN categoryId UUID;

UPDATE reports.jasper_templates
SET categoryId = (
    SELECT id
    FROM reports.report_categories
    WHERE name = 'LMIS Reports'
    LIMIT 1
)
WHERE categoryId IS NULL;

ALTER TABLE reports.jasper_templates ALTER COLUMN categoryId SET NOT NULL;

ALTER TABLE reports.jasper_templates ADD CONSTRAINT fk_category
FOREIGN KEY (categoryId) REFERENCES reports.report_categories (id)
ON DELETE SET NULL;