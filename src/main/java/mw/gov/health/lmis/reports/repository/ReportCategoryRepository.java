package mw.gov.health.lmis.reports.repository;

import java.util.Optional;
import java.util.UUID;
import mw.gov.health.lmis.reports.domain.ReportCategory;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface ReportCategoryRepository
    extends PagingAndSortingRepository<ReportCategory, UUID> {
  Optional<ReportCategory> findByName(String name);

  boolean existsByName(String name);

  boolean existsByIdIsNotAndName(UUID id, String name);
}
