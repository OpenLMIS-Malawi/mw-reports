/*
 * This program is part of the OpenLMIS logistics management information system platform software.
 * Copyright © 2017 VillageReach
 *
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of the GNU Affero General Public License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details. You should have received a copy of
 * the GNU Affero General Public License along with this program. If not, see
 * http://www.gnu.org/licenses.  For additional information contact info@OpenLMIS.org.
 */

package mw.gov.health.lmis.reports.service.stockmanagement;

import mw.gov.health.lmis.reports.exception.JasperReportViewException;
import mw.gov.health.lmis.utils.Message;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.xml.JRXmlLoader;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;

import static mw.gov.health.lmis.reports.i18n.MessageKeys.ERROR_GENERATE_REPORT_FAILED;
import static mw.gov.health.lmis.reports.i18n.MessageKeys.ERROR_IO;

@Service
public class JasperReportService {

  static final String PI_LINES_REPORT_URL = "/jasperTemplates/physicalinventoryLines.jrxml";


  /**
   * Creates PI line sub-report.
   * */
  public JasperDesign createCustomizedPhysicalInventoryLineSubreport()
      throws JasperReportViewException {
    try (InputStream inputStream = getClass().getResourceAsStream(PI_LINES_REPORT_URL)) {
      return JRXmlLoader.load(inputStream);
    } catch (IOException ex) {
      throw new JasperReportViewException(new Message((ERROR_IO), ex.getMessage()), ex);
    } catch (JRException ex) {
      throw new JasperReportViewException(new Message(ERROR_GENERATE_REPORT_FAILED), ex);
    }
  }
}
