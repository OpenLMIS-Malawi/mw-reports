UPDATE reports.template_parameters
SET selectexpression = '/api/facilities'
WHERE id = '9201ff0f-26a5-42ca-aedc-7bd44cef58bf';

UPDATE reports.template_parameters
SET selectexpression = '/api/facilityTypes'
WHERE id = 'dd12580b-a3fd-4144-b613-34cd00528adf';

UPDATE reports.template_parameters
SET selectexpression = '/api/orderables'
WHERE id = 'f9a49dcd-3518-473a-a357-3f004fb7334a';

UPDATE reports.template_parameters
SET selectexpression = '/api/lots'
WHERE id = 'a9187ff4-a9d7-4b1c-be69-d180b8bb72a4';