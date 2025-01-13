# OpenLMIS Malawi Reporting Service
Service for managing and printing reports for Malawi OpenLMIS implementation.

# Updating reports to database

1. Build report in Jaspersoft Studio application to have `.jasper` file.
2. Go to reports 
```bash
 cd reports/
```

3. Create hex file of created jasper file.
```
xxd -pxxd -c 999999999 <report_file_name>.jasper > <report_file_name>.hex 
```

4. Go back to main repository.
```bash
cd ..
```
5. Create migration file
```
gradle generateMigration -PmigrationName=<migration_name>
```
example migration name `update_<field>_<reportName>`

6. Edit migration file and make sure it will be added to the changes with new commit

```
UPDATE reports.jasper_templates
SET data = '\xHEX'
WHERE id = 'report-uuid';
```
`HEX` is exactly, what have you generated in step 3. \
`report-uuid` you should know the `uuid` or take it from database

7. Commit your changes, make sure that migration and updated <report_name>.jrxml are added.
8. Push and create Pull Request.
