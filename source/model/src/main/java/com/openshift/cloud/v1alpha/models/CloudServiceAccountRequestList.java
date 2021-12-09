package com.openshift.cloud.v1alpha.models;

import io.fabric8.kubernetes.client.CustomResourceList;
import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public class CloudServiceAccountRequestList extends CustomResourceList<CloudServiceAccountRequest> {
}
