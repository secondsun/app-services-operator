package com.openshift.cloud.controllers;

import static com.openshift.cloud.v1alpha.models.ManagedKafkaCondition.Status.True;

import com.openshift.cloud.v1alpha.models.*;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

public class ConditionUtil {
  public static void initializeConditions(ManagedKafkaRequest resource) {
    var status = resource.getStatus();
    if (status == null) {
      resource.setStatus(
          new ManagedKafkaRequestStatusBuilder()
              .withLastUpdate(isoNow())
              .withUserKafkas(new ArrayList<>())
              .withConditions(managedKafkaRequestDefaultConditions())
              .build());
    } else {
      status.setConditions(managedKafkaRequestDefaultConditions());
    }
  }

  private static List<ManagedKafkaCondition> managedKafkaRequestDefaultConditions() {
    return List.of(
        new ManagedKafkaCondition()
            .setLastTransitionTime(isoNow())
            .setType(ManagedKafkaCondition.Type.AcccesTokenSecretAvailable.name())
            .setReason("")
            .setMessage("")
            .setStatus(ManagedKafkaCondition.Status.Unknown.name()),
        new ManagedKafkaCondition()
            .setLastTransitionTime(isoNow())
            .setReason("")
            .setMessage("")
            .setType(ManagedKafkaCondition.Type.Finished.name())
            .setStatus(ManagedKafkaCondition.Status.Unknown.name()),
        new ManagedKafkaCondition()
            .setLastTransitionTime(isoNow())
            .setReason("")
            .setMessage("")
            .setType(ManagedKafkaCondition.Type.UserKafkasUpToDate.name())
            .setStatus(ManagedKafkaCondition.Status.Unknown.name()));
  }

  public static void setConditionFromException(
      List<ManagedKafkaCondition> conditions, ConditionAwareException e) {
    var condition = getCondition(conditions, e.getType());
    condition.setLastTransitionTime(isoNow());
    condition.setMessage(e.getConditionMessage());
    condition.setStatus(e.getStatus().name());
    condition.setReason(e.getReason());
  }

  public static ManagedKafkaCondition getCondition(
      List<ManagedKafkaCondition> resource, ManagedKafkaCondition.Type type) {
    return resource.stream()
        .filter(condition -> condition.getType().equalsIgnoreCase(type.name()))
        .findFirst()
        .orElseThrow(
            () -> {
              return new RuntimeException(
                  resource.stream().map(a -> a.getType()).collect(Collectors.joining(", "))
                      + " does not equal"
                      + type);
            });
  }

  private static String isoNow() {
    return ZonedDateTime.now(ZoneOffset.UTC).format(DateTimeFormatter.ISO_INSTANT);
  }

  public static void initializeConditions(ManagedServiceAccountRequest resource) {
    var status = resource.getStatus();
    if (status == null) {
      resource.setStatus(
          new ManagedServiceAccountRequestStatusBuilder()
              .withConditions(managedKafkaServiceAccountRequestDefaultConditions())
              .build());
    } else {
      status.setConditions(managedKafkaServiceAccountRequestDefaultConditions());
    }
  }

  private static List<ManagedKafkaCondition> managedKafkaServiceAccountRequestDefaultConditions() {
    return List.of(
        new ManagedKafkaCondition()
            .setLastTransitionTime(isoNow())
            .setType(ManagedKafkaCondition.Type.AcccesTokenSecretAvailable.name())
            .setReason("")
            .setMessage("")
            .setStatus(ManagedKafkaCondition.Status.Unknown.name()),
        new ManagedKafkaCondition()
            .setLastTransitionTime(isoNow())
            .setReason("")
            .setMessage("")
            .setType(ManagedKafkaCondition.Type.ServiceAccountCreated.name())
            .setStatus(ManagedKafkaCondition.Status.Unknown.name()),
        new ManagedKafkaCondition()
            .setLastTransitionTime(isoNow())
            .setReason("")
            .setMessage("")
            .setType(ManagedKafkaCondition.Type.ServiceAccountSecretCreated.name())
            .setStatus(ManagedKafkaCondition.Status.Unknown.name()),
        new ManagedKafkaCondition()
            .setLastTransitionTime(isoNow())
            .setReason("")
            .setMessage("")
            .setType(ManagedKafkaCondition.Type.Finished.name())
            .setStatus(ManagedKafkaCondition.Status.Unknown.name()));
  }

  public static void setAllConditionsTrue(List<ManagedKafkaCondition> conditions) {
    conditions.forEach(
        condition -> {
          condition.setLastTransitionTime(isoNow());
          condition.setMessage("");
          condition.setStatus(True.name());
          condition.setReason("");
        });
  }

  public static void initializeConditions(ManagedKafkaConnection resource) {
    var status = resource.getStatus();
    if (status == null) {
      resource.setStatus(
          new ManagedKafkaConnectionStatusBuilder()
              .withConditions(managedKafkaConnectionDefaultConditions())
              .build());
    } else {
      status.setConditions(managedKafkaConnectionDefaultConditions());
    }
  }

  private static List<ManagedKafkaCondition> managedKafkaConnectionDefaultConditions() {
    return List.of(
        new ManagedKafkaCondition()
            .setLastTransitionTime(isoNow())
            .setType(ManagedKafkaCondition.Type.AcccesTokenSecretAvailable.name())
            .setReason("")
            .setMessage("")
            .setStatus(ManagedKafkaCondition.Status.Unknown.name()),
        new ManagedKafkaCondition()
            .setLastTransitionTime(isoNow())
            .setType(ManagedKafkaCondition.Type.FoundKafkaById.name())
            .setReason("")
            .setMessage("")
            .setStatus(ManagedKafkaCondition.Status.Unknown.name()),
        new ManagedKafkaCondition()
            .setLastTransitionTime(isoNow())
            .setReason("")
            .setMessage("")
            .setType(ManagedKafkaCondition.Type.Finished.name())
            .setStatus(ManagedKafkaCondition.Status.Unknown.name()));
  }

  public static boolean allTrue(List<ManagedKafkaCondition> conditions) {
    if (conditions == null) {
      return false;
    }

    AtomicBoolean allTrue = new AtomicBoolean(true);

    for (var cond : conditions) {
      if (!True.name().equalsIgnoreCase(cond.getStatus())) {
        allTrue.set(false);
        break;
      }
    }
    ;
    return allTrue.get();
  }
}
