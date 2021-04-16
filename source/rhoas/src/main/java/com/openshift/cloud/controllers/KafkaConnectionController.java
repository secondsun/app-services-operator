package com.openshift.cloud.controllers;

import com.openshift.cloud.beans.AccessTokenSecretTool;
import com.openshift.cloud.beans.KafkaApiClient;
import com.openshift.cloud.utils.ConnectionResourcesMetadata;
import com.openshift.cloud.utils.InvalidUserInputException;
import com.openshift.cloud.v1alpha.models.KafkaConnection;
import io.fabric8.kubernetes.client.WatcherException;
import io.javaoperatorsdk.operator.api.*;
import io.javaoperatorsdk.operator.processing.event.DefaultEventSourceManager;
import io.javaoperatorsdk.operator.processing.event.EventSourceManager;
import io.javaoperatorsdk.operator.processing.event.internal.CustomResourceEventSource;
import io.quarkus.runtime.Quarkus;
import io.quarkus.scheduler.Scheduled;
import java.lang.reflect.Field;
import java.time.Instant;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.inject.Inject;

@Controller
public class KafkaConnectionController extends AbstractCloudServicesController<KafkaConnection> {

  private static final Logger LOG = Logger.getLogger(KafkaConnectionController.class.getName());

  @Inject KafkaApiClient apiClient;

  @Inject AccessTokenSecretTool accessTokenSecretTool;

  private EventSourceManager eventSourceManager;

  @Override
  void doCreateOrUpdateResource(KafkaConnection resource, Context<KafkaConnection> context)
      throws ConditionAwareException, InvalidUserInputException {
    LOG.info(String.format("Creating or Updating resource %s", resource.getMetadata().getName()));

    validateResource(resource);

    var kafkaId = resource.getSpec().getKafkaId();
    var accessTokenSecretName = resource.getSpec().getAccessTokenSecretName();
    var serviceAccountSecretName =
        resource.getSpec().getCredentials().getServiceAccountSecretName();
    var namespace = resource.getMetadata().getNamespace();

    String accessToken = accessTokenSecretTool.getAccessToken(accessTokenSecretName, namespace);

    var kafkaServiceInfo = apiClient.getKafkaById(kafkaId, accessToken);

    var bootStrapHost = kafkaServiceInfo.getBootstrapServerHost();

    var status = resource.getStatus();
    status.setMessage("Created");
    status.setUpdated(Instant.now().toString());
    status.setBootstrapServerHost(bootStrapHost);
    status.setServiceAccountSecretName(serviceAccountSecretName);
    status.setMetadata(ConnectionResourcesMetadata.buildKafkaMetadata(kafkaId));
  }

  @Override
  public void init(EventSourceManager eventSourceManager) {
    this.eventSourceManager = eventSourceManager;
    LOG.info("Init! This is where we would add watches for child resources");
  }

  @Scheduled(every = "600s")
  public void reconnect() {
    LOG.info("Begin reconnect");
    DefaultEventSourceManager x = (DefaultEventSourceManager) this.eventSourceManager;
    Field f;
    if (x != null) {
      try {
        f = x.getClass().getDeclaredField("customResourceEventSource");
        f.setAccessible(true);
        CustomResourceEventSource cre = (CustomResourceEventSource) f.get(x);
        LOG.info("Reconnecting");
        cre.onClose(
            new WatcherException("Hack Exception") {
              @Override
              public boolean isHttpGone() {
                return true;
              }
            });
      } catch (NoSuchFieldException
          | SecurityException
          | IllegalArgumentException
          | IllegalAccessException e) {
        // TODO Auto-generated catch block
        LOG.log(Level.SEVERE, "Failed to get customResourceEventSource", e);
      } catch (Exception e) {
        LOG.log(Level.SEVERE, "Reconnect failed, exiting", e);
        Quarkus.asyncExit(1);
      }
    }
  }

  void validateResource(KafkaConnection resource) throws InvalidUserInputException {
    ConditionUtil.assertNotNull(resource.getSpec(), "spec");
    ConditionUtil.assertNotNull(
        resource.getSpec().getAccessTokenSecretName(), "spec.accessTokenSecretName");
    ConditionUtil.assertNotNull(resource.getSpec().getCredentials(), "spec.credentials");
    ConditionUtil.assertNotNull(
        resource.getSpec().getCredentials().getServiceAccountSecretName(),
        "spec.credentials.serviceAccountSecretName");
    ConditionUtil.assertNotNull(resource.getSpec().getKafkaId(), "spec.kafkaId");
    ConditionUtil.assertNotNull(resource.getMetadata().getNamespace(), "metadata.namespace");
  }
}
