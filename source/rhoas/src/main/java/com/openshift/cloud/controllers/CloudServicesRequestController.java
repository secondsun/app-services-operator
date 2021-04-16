package com.openshift.cloud.controllers;

import com.openshift.cloud.beans.AccessTokenSecretTool;
import com.openshift.cloud.beans.KafkaApiClient;
import com.openshift.cloud.beans.KafkaK8sClients;
import com.openshift.cloud.utils.InvalidUserInputException;
import com.openshift.cloud.v1alpha.models.CloudServicesRequest;
import com.openshift.cloud.v1alpha.models.UserKafka;

import io.fabric8.kubernetes.client.WatcherException;
import io.javaoperatorsdk.operator.api.*;
import io.javaoperatorsdk.operator.processing.event.DefaultEventSourceManager;
import io.javaoperatorsdk.operator.processing.event.EventSourceManager;
import io.javaoperatorsdk.operator.processing.event.internal.CustomResourceEventSource;
import io.quarkus.scheduler.Scheduled;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.logging.Logger;
import javax.inject.Inject;

@Controller
public class CloudServicesRequestController
    extends AbstractCloudServicesController<CloudServicesRequest> {

  private static final Logger LOG =
      Logger.getLogger(CloudServicesRequestController.class.getName());

  @Inject AccessTokenSecretTool accessTokenSecretTool;

  @Inject KafkaK8sClients kafkaClientFactory;

  @Inject KafkaApiClient apiClient;

  private EventSourceManager eventSourceManager;

  public CloudServicesRequestController() {}

  /** @return true if there were changes, false otherwise */
  @Override
  public void init(EventSourceManager eventSourceManager) {
    this.eventSourceManager = eventSourceManager;
    LOG.info("Init! This is where we would add watches for child resources");
  }

@Scheduled(every = "600s")  
public void reconnect() {
  DefaultEventSourceManager x = (DefaultEventSourceManager) this.eventSourceManager;
  Field f;
  if (x != null)
  try {
    f = x.getClass().getDeclaredField("customResourceEventSource");
    f.setAccessible(true);
    CustomResourceEventSource cre = (CustomResourceEventSource) f.get(x);
    cre.onClose(new WatcherException("Hack Exception"){
      @Override
      public boolean isHttpGone() {
          return true;
      }
    });
  } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
    // TODO Auto-generated catch block
    e.printStackTrace();
  }
  

}

  @Override
  void doCreateOrUpdateResource(
      CloudServicesRequest resource, Context<CloudServicesRequest> context)
      throws ConditionAwareException, InvalidUserInputException {
    var accessTokenSecretName = resource.getSpec().getAccessTokenSecretName();
    var namespace = resource.getMetadata().getNamespace();
    validateResource(resource);
    String accessToken = null;
    accessToken = accessTokenSecretTool.getAccessToken(accessTokenSecretName, namespace);

    var kafkaList = apiClient.listKafkas(accessToken);

    var userKafkas = new ArrayList<UserKafka>();

    kafkaList.getItems().stream()
        .forEach(
            listItem -> {
              var userKafka =
                  new UserKafka()
                      .setId(listItem.getId())
                      .setName(listItem.getName())
                      .setOwner(listItem.getOwner())
                      .setBootstrapServerHost(listItem.getBootstrapServerHost())
                      .setStatus(listItem.getStatus())
                      .setCreatedAt(listItem.getCreatedAt().toInstant().toString())
                      .setUpdatedAt(listItem.getUpdatedAt().toInstant().toString())
                      .setProvider(listItem.getCloudProvider())
                      .setRegion(listItem.getRegion());

              userKafkas.add(userKafka);
            });

    resource.getStatus().setUserKafkas(userKafkas);
  }

  void validateResource(CloudServicesRequest resource) throws InvalidUserInputException {
    ConditionUtil.assertNotNull(resource.getSpec(), "spec");
    ConditionUtil.assertNotNull(
        resource.getSpec().getAccessTokenSecretName(), "spec.accessTokenSecretName");
    ConditionUtil.assertNotNull(resource.getMetadata().getNamespace(), "metadata.namespace");
  }
}
