package mw.gov.health.lmis.reports.repository;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

import mw.gov.health.lmis.reports.domain.JasperTemplate;

public interface JasperTemplateRepository
    extends PagingAndSortingRepository<JasperTemplate, UUID> {
  JasperTemplate findByName(@Param("name") String name);
}
