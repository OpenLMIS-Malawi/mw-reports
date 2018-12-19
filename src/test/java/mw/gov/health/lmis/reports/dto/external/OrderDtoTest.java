package mw.gov.health.lmis.reports.dto.external;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.Ordering;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import mw.gov.health.lmis.testutils.OrderDtoDataBuilder;
import mw.gov.health.lmis.testutils.StatusChangeDtoDataBuilder;
import org.junit.Test;

public class OrderDtoTest {

  @Test
  public void shouldReturnEmptySetIfThereIsNoAuthorizeStatusChange() {
    OrderDto order = new OrderDtoDataBuilder()
        .withStatusChanges(singletonList(
            new StatusChangeDtoDataBuilder().build()))
        .build();

    assertEquals(0, order.getApprovedStatusChanges().size());
  }

  @Test
  public void shouldReturnSortedStatusChanges() {
    LocalTime localTime = LocalTime.now();

    OrderDto order = new OrderDtoDataBuilder()
        .withStatusChanges(asList(
            new StatusChangeDtoDataBuilder()
                .withStatus(RequisitionStatusDto.AUTHORIZED)
                .withCreatedDate(ZonedDateTime.of(LocalDate.of(2018, 10, 1), localTime,
                    ZoneId.systemDefault()))
                .build(),
            new StatusChangeDtoDataBuilder()
                .withCreatedDate(ZonedDateTime.of(LocalDate.of(2018, 10, 10), localTime,
                    ZoneId.systemDefault()))
                .build(),
            new StatusChangeDtoDataBuilder()
                .withCreatedDate(ZonedDateTime.of(LocalDate.of(2018, 10, 9), localTime,
                    ZoneId.systemDefault()))
                .build(),
            new StatusChangeDtoDataBuilder()
                .withCreatedDate(ZonedDateTime.of(LocalDate.of(2018, 10, 12), localTime,
                    ZoneId.systemDefault()))
                .build(),
            new StatusChangeDtoDataBuilder()
                .withCreatedDate(ZonedDateTime.of(LocalDate.of(2018, 10, 11), localTime,
                    ZoneId.systemDefault()))
                .build()))
        .build();

    assertTrue(Ordering.natural().isOrdered(order.getApprovedStatusChanges()));

  }

  @Test
  public void shouldReturnStatusChangesAfterLastAuthorization() {
    LocalTime localTime = LocalTime.now();

    StatusChangeDto statusChange1 = new StatusChangeDtoDataBuilder()
        .withCreatedDate(ZonedDateTime.of(LocalDate.of(2018, 10, 11), localTime,
            ZoneId.systemDefault()))
        .build();
    StatusChangeDto statusChange2 = new StatusChangeDtoDataBuilder()
        .withCreatedDate(ZonedDateTime.of(LocalDate.of(2018, 10, 12), localTime,
            ZoneId.systemDefault()))
        .build();

    OrderDto order = new OrderDtoDataBuilder()
        .withStatusChanges(asList(
            new StatusChangeDtoDataBuilder()
                .withStatus(RequisitionStatusDto.AUTHORIZED)
                .withCreatedDate(ZonedDateTime.of(LocalDate.of(2018, 10, 1), localTime,
                    ZoneId.systemDefault()))
                .build(),
            new StatusChangeDtoDataBuilder()
                .withCreatedDate(ZonedDateTime.of(LocalDate.of(2018, 10, 2), localTime,
                    ZoneId.systemDefault()))
                .build(),
            new StatusChangeDtoDataBuilder()
                .withStatus(RequisitionStatusDto.AUTHORIZED)
                .withCreatedDate(ZonedDateTime.of(LocalDate.of(2018, 10, 10), localTime,
                    ZoneId.systemDefault()))
                .build(),
            statusChange1,
            statusChange2))
        .build();

    assertThat(order.getApprovedStatusChanges(), hasItems(statusChange1, statusChange2));
  }

  @Test
  public void shouldReturnApprovedAndInApprovalStatusChanges() {
    LocalTime localTime = LocalTime.now();

    StatusChangeDto statusChange1 = new StatusChangeDtoDataBuilder()
        .withStatus(RequisitionStatusDto.IN_APPROVAL)
        .withCreatedDate(ZonedDateTime.of(LocalDate.of(2018, 10, 11), localTime,
            ZoneId.systemDefault()))
        .build();
    StatusChangeDto statusChange2 = new StatusChangeDtoDataBuilder()
        .withStatus(RequisitionStatusDto.APPROVED)
        .withCreatedDate(ZonedDateTime.of(LocalDate.of(2018, 10, 12), localTime,
            ZoneId.systemDefault()))
        .build();

    OrderDto order = new OrderDtoDataBuilder()
        .withStatusChanges(asList(
            new StatusChangeDtoDataBuilder()
                .withStatus(RequisitionStatusDto.AUTHORIZED)
                .withCreatedDate(ZonedDateTime.of(LocalDate.of(2018, 10, 1), localTime,
                    ZoneId.systemDefault()))
                .build(),
            statusChange1,
            statusChange2))
        .build();

    assertThat(order.getApprovedStatusChanges(), hasItems(statusChange1, statusChange2));
  }
}
