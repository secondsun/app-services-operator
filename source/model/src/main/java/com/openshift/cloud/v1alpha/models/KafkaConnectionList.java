package com.openshift.cloud.v1alpha.models;

import io.fabric8.kubernetes.client.CustomResourceList;
import io.quarkus.runtime.annotations.RegisterForReflection;

/** required for using Fabric8 CRD support. */
@RegisterForReflection
public class KafkaConnectionList extends CustomResourceList<KafkaConnection> {

  /** Generated */
  private static final long serialVersionUID = 1198350360303794020L;
}
