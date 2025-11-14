package mw.gov.health.lmis.reports.domain;

import com.fasterxml.jackson.annotation.JsonView;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Type;
import org.openlmis.util.View;

import java.util.UUID;

import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

import lombok.Getter;
import lombok.Setter;

@MappedSuperclass
public abstract class BaseEntity {
  protected static final String TEXT_COLUMN_DEFINITION = "text";
  protected static final String UUID_COLUMN_DEFINITION = "pg-uuid";
  protected static final String BOOLEAN_COLUMN_DEFINITION = "boolean";

  @Id
  @GeneratedValue(generator = "uuid-gen")
  @GenericGenerator(name = "uuid-gen", strategy = "uuid2")
  @JsonView(View.BasicInformation.class)
  @Type(type = UUID_COLUMN_DEFINITION)
  @Getter
  @Setter
  protected UUID id;
}
