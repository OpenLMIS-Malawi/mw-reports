package mw.gov.health.lmis.reports.repository;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.data.repository.CrudRepository;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import mw.gov.health.lmis.Application;
import mw.gov.health.lmis.reports.domain.BaseEntity;

@RunWith(SpringJUnit4ClassRunner.class)
@ActiveProfiles("test")
@SpringApplicationConfiguration(Application.class)
@Transactional
public abstract class BaseCrudRepositoryIntegrationTest<T extends BaseEntity> {

  abstract CrudRepository<T, UUID> getRepository();

  /*
   * Generate a unique instance of given type.
   * @return generated instance
   */
  abstract T generateInstance();

  private AtomicInteger instanceNumber = new AtomicInteger(0);

  int getNextInstanceNumber() {
    return this.instanceNumber.incrementAndGet();
  }

  protected void assertInstance(T instance) {
    Assert.assertNotNull(instance.getId());
  }

  @Test
  public void testCreate() {
    CrudRepository<T, UUID> repository = this.getRepository();

    T instance = this.generateInstance();
    Assert.assertNull(instance.getId());

    instance = repository.save(instance);
    assertInstance(instance);

    Assert.assertTrue(repository.exists(instance.getId()));
  }

  @Test
  public void testFindOne() {
    CrudRepository<T, UUID> repository = this.getRepository();

    T instance = this.generateInstance();

    instance = repository.save(instance);
    assertInstance(instance);

    UUID id = instance.getId();

    instance = repository.findOne(id);
    assertInstance(instance);
    Assert.assertEquals(id, instance.getId());
  }

  @Test
  public void testDelete() {
    CrudRepository<T, UUID> repository = this.getRepository();

    T instance = this.generateInstance();
    Assert.assertNotNull(instance);

    instance = repository.save(instance);
    assertInstance(instance);

    UUID id = instance.getId();

    repository.delete(id);
    Assert.assertFalse(repository.exists(id));
  }
}
