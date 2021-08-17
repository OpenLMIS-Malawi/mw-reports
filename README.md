# OpenLMIS Malawi Reporting Service
Service for managing and printing reports for Malawi OpenLMIS implementation.

## Modifying jasper reports
1. After modification in .jrxml file with report it should be compiled in Jaspersoft Studio to .jasper file. 
2. Convert .jasper file to hex. Cammand in terminal for that: 
 ```shell
xxd -pxxd -c 999999999 plik.jsaper > plik.hex
```
3. Add new migration file with update on reports.jasper_templates using converted data
