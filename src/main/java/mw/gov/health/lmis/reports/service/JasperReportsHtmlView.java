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

package mw.gov.health.lmis.reports.service;

import net.sf.jasperreports.engine.JRExporter;
import net.sf.jasperreports.engine.export.HtmlExporter;
import org.springframework.web.servlet.view.jasperreports.AbstractJasperReportsSingleFormatView;

/**
 * Implementation of {@code AbstractJasperReportsSingleFormatView}
 * that renders report results in HTML format.
 *
 * <p>This is an equivalent of the Spring class that creates Jasper HTML exporter, that doesn't use
 * JRHtmlExporter removed in Jasper 6.4.3.
 */
@SuppressWarnings("deprecation")
public class JasperReportsHtmlView extends AbstractJasperReportsSingleFormatView {

  public JasperReportsHtmlView() {
    setContentType("text/html");
  }

  @Override
  protected JRExporter createExporter() {
    return new HtmlExporter();
  }

  @Override
  protected boolean useWriter() {
    return true;
  }
}
