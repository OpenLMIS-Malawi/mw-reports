package mw.gov.health.lmis;

import org.javers.core.Javers;
import org.javers.core.MappingStyle;
import org.javers.core.diff.ListCompareAlgorithm;
import org.javers.hibernate.integration.HibernateUnproxyObjectAccessHook;
import org.javers.repository.sql.ConnectionProvider;
import org.javers.repository.sql.DialectName;
import org.javers.repository.sql.JaversSqlRepository;
import org.javers.repository.sql.SqlRepositoryBuilder;
import org.javers.spring.auditable.AuthorProvider;
import org.javers.spring.boot.sql.JaversProperties;
import org.javers.spring.jpa.TransactionalJaversBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationStrategy;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.annotation.Profile;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.i18n.CookieLocaleResolver;

import mw.gov.health.lmis.reports.domain.BaseEntity;
import mw.gov.health.lmis.reports.i18n.ExposedMessageSourceImpl;
import mw.gov.health.lmis.security.UserNameProvider;
import mw.gov.health.lmis.settings.domain.ConfigurationSetting;

import java.time.Clock;
import java.time.ZoneId;
import java.util.Locale;

@SpringBootApplication
@ImportResource("applicationContext.xml")
@ComponentScan(basePackageClasses = {BaseEntity.class, ConfigurationSetting.class},
    basePackages = "org.openlmis.util.converter")
@EnableJpaRepositories(basePackageClasses = {BaseEntity.class, ConfigurationSetting.class},
    basePackages = "org.openlmis.util.converter")
public class Application {
  private static Logger LOGGER = LoggerFactory.getLogger(Application.class);

  @Autowired
  private DialectName dialectName;

  @Autowired
  private JaversProperties javersProperties;

  @Value("${spring.jpa.properties.hibernate.default_schema}")
  private String preferredSchema;

  @Value("${time.zoneId}")
  private String timeZoneId;

  public static void main(String[] args) {
    SpringApplication.run(Application.class, args);
  }

  /**
   * Creates new LocaleResolver.
   *
   * @return Created LocalResolver.
   */
  @Bean
  public LocaleResolver localeResolver() {
    CookieLocaleResolver lr = new CookieLocaleResolver();
    lr.setCookieName("lang");
    lr.setDefaultLocale(Locale.ENGLISH);
    return lr;
  }

  /**
   * Creates new MessageSource.
   *
   * @return Created MessageSource.
   */
  @Bean
  public ExposedMessageSourceImpl messageSource() {
    ExposedMessageSourceImpl messageSource = new ExposedMessageSourceImpl();
    messageSource.setBasename("classpath:messages");
    messageSource.setDefaultEncoding("UTF-8");
    messageSource.setUseCodeAsDefaultMessage(true);
    return messageSource;
  }

  /**
   * Create and return a UserNameProvider. By default, if we didn't do so, an instance of
   * SpringSecurityAuthorProvider would automatically be created and returned instead.
   */
  @Bean
  public AuthorProvider authorProvider() {
    return new UserNameProvider();
  }


  /**
   * Create and return an instance of JaVers precisely configured as necessary.
   * This is particularly helpful for getting JaVers to create and use tables
   * within a particular schema (specified via the withSchema method).
   *
   * @See <a href="https://github.com/javers/javers/blob/master/javers-spring-boot-starter-sql/src
   * /main/java/org/javers/spring/boot/sql/JaversSqlAutoConfiguration.java">
   * JaversSqlAutoConfiguration.java</a> for the default configuration upon which this code is based
   */
  @Bean
  public Javers javersProvider(ConnectionProvider connectionProvider,
                               PlatformTransactionManager transactionManager) {
    JaversSqlRepository sqlRepository = SqlRepositoryBuilder
        .sqlRepository()
        .withConnectionProvider(connectionProvider)
        .withDialect(dialectName)
        .withSchema(preferredSchema)
        .build();

    JaVersDateProvider customDateProvider = new JaVersDateProvider();

    return TransactionalJaversBuilder
        .javers()
        .withTxManager(transactionManager)
        .registerJaversRepository(sqlRepository)
        .withObjectAccessHook(new HibernateUnproxyObjectAccessHook())
        .withListCompareAlgorithm(
            ListCompareAlgorithm.valueOf(javersProperties.getAlgorithm().toUpperCase()))
        .withMappingStyle(
            MappingStyle.valueOf(javersProperties.getMappingStyle().toUpperCase()))
        .withNewObjectsSnapshot(javersProperties.isNewObjectSnapshot())
        .withPrettyPrint(javersProperties.isPrettyPrint())
        .withTypeSafeValues(javersProperties.isTypeSafeValues())
        .withPackagesToScan(javersProperties.getPackagesToScan())
        .withDateTimeProvider(customDateProvider)
        .build();
  }

  /**
   * Configures the Flyway migration strategy to clean the DB before migration first. This is used
   * as the default unless the Spring Profile "production" is active.
   *
   * @return the clean-migrate strategy
   */
  @Bean
  @Profile("!production")
  public FlywayMigrationStrategy cleanMigrationStrategy() {
    return flyway -> {
      LOGGER.info("Using clean-migrate flyway strategy -- production profile not active");
      flyway.clean();
      flyway.migrate();
    };
  }

  /**
   * Creates the clock bean and sets the timezone in the Locale Holder.
   *
   * @return clock instance with application configured timezone.
   */
  @Bean
  public Clock clock() {
    return Clock.system(ZoneId.of(timeZoneId));
  }
}
