package com.openshift.cloud.controllers;

import com.openshift.cloud.beans.AccessTokenSecretTool;
import com.openshift.cloud.beans.KafkaApiClient;
import com.openshift.cloud.beans.KafkaK8sClients;
import com.openshift.cloud.utils.InvalidUserInputException;
import com.openshift.cloud.v1alpha.models.CloudServiceAccountRequest;
import io.fabric8.kubernetes.client.WatcherException;
import io.javaoperatorsdk.operator.api.*;
import io.javaoperatorsdk.operator.processing.event.DefaultEventSourceManager;
import io.javaoperatorsdk.operator.processing.event.EventSourceManager;
import io.javaoperatorsdk.operator.processing.event.internal.CustomResourceEventSource;
import io.quarkus.runtime.Quarkus;
import io.quarkus.scheduler.Scheduled;
import java.lang.reflect.Field;
import java.time.Instant;
import javax.inject.Inject;
import org.jboss.logging.Logger;
import org.jboss.logging.Logger.Level;

/** Controller for CloudServiceAccountRequest CRs */
@Controller
public class CloudServiceAccountRequestController
    extends AbstractCloudServicesController<CloudServiceAccountRequest> {

  private static final Logger LOG =
      Logger.getLogger(CloudServiceAccountRequestController.class.getName());

  @Inject AccessTokenSecretTool accessTokenSecretTool;

  @Inject KafkaK8sClients kafkaClientFactory;

  @Inject KafkaApiClient apiClient;

  private EventSourceManager eventSourceManager;

  @Override
  void doCreateOrUpdateResource(
      CloudServiceAccountRequest resource, Context<CloudServiceAccountRequest> context)
      throws ConditionAwareException, InvalidUserInputException {

    validateResource(resource);
    var accessTokenSecretName = resource.getSpec().getAccessTokenSecretName();
    var namespace = resource.getMetadata().getNamespace();

    String accessToken = null;

    accessToken = accessTokenSecretTool.getAccessToken(accessTokenSecretName, namespace);

    var serviceAccount = apiClient.createServiceAccount(resource.getSpec(), accessToken);

    apiClient.createSecretForServiceAccount(resource, serviceAccount);

    var status = resource.getStatus();
    status.setMessage("Created");
    status.setUpdated(Instant.now().toString());
    status.setServiceAccountSecretName(resource.getSpec().getServiceAccountSecretName());

    resource.setStatus(status);
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
        LOG.log(Level.ERROR, "Failed to get customResourceEventSource", e);
      } catch (Exception e) {
        LOG.log(Level.ERROR, "Reconnect failed, exiting", e);
        Quarkus.asyncExit(1);
      }
    }
  }

  void validateResource(CloudServiceAccountRequest resource) throws InvalidUserInputException {
    ConditionUtil.assertNotNull(resource.getSpec(), "spec");
    ConditionUtil.assertNotNull(
        resource.getSpec().getAccessTokenSecretName(), "spec.accessTokenSecretName");
    ConditionUtil.assertNotNull(
        resource.getSpec().getServiceAccountName(), "spec.serviceAccountName");
    ConditionUtil.assertNotNull(
        resource.getSpec().getServiceAccountSecretName(), "spec.serviceAccountSecretName");
    ConditionUtil.assertNotNull(resource.getMetadata().getNamespace(), "metadata.namespace");
  }
}
