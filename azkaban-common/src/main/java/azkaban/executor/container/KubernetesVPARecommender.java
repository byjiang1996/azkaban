package azkaban.executor.container;

import azkaban.executor.ExecutorManagerException;
import com.google.common.collect.ImmutableMap;
import io.kubernetes.autoscaling.models.V1VerticalPodAutoscaler;
import io.kubernetes.autoscaling.models.V1VerticalPodAutoscalerSpec;
import io.kubernetes.autoscaling.models.V1VerticalPodAutoscalerSpecResourcePolicy;
import io.kubernetes.autoscaling.models.V1VerticalPodAutoscalerSpecResourcePolicyContainerPolicies;
import io.kubernetes.autoscaling.models.V1VerticalPodAutoscalerSpecUpdatePolicy;
import io.kubernetes.autoscaling.models.V1VerticalPodAutoscalerSpecUpdatePolicy.UpdateModeEnum;
import io.kubernetes.client.custom.Quantity;
import io.kubernetes.client.custom.QuantityFormatException;
import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.models.V1LabelSelector;
import io.kubernetes.client.openapi.models.V1ObjectMeta;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collections;
import java.util.Map;
import java.util.NoSuchElementException;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is the implementation for native Kubernetes VPA Recommender:
 * https://github.com/kubernetes/autoscaler/tree/master/vertical-pod-autoscaler.
 * It has implementation for getting CPU and memory recommendation for a given Azkaban flow. For
 * any recommendation query, it will query the Kubernetes VPA object associated with the given
 * Azkaban flow first: if not exist, create one and apply the default request value for the current
 * execution.
 */
@Singleton
public class KubernetesVPARecommender implements VPARecommender {
  private static final Logger logger = LoggerFactory
      .getLogger(KubernetesVPARecommender.class);

  private static final String VPA_API_VERSION = "autoscaling.k8s.io/v1";
  private static final String VPA_KIND = "VerticalPodAutoscaler";

  private static final String VPA_CPU_KEY = "cpu";
  private static final String VPA_MEMORY_KEY = "memory";

  private final KubernetesVPARecommenderV1Api kubernetesVPARecommenderV1Api;

  @Inject
  public KubernetesVPARecommender(ApiClient client) {
    this.kubernetesVPARecommenderV1Api = new KubernetesVPARecommenderV1Api(client);
  }

  private void createVPAObject(final String namespace, final String flowVPALabelName,
      final String flowVPAName, final String flowContainerName, final String maxAllowedCPU,
      final String maxAllowedMemory) throws ExecutorManagerException {
    try {
      V1VerticalPodAutoscaler vpaObject = new V1VerticalPodAutoscaler()
          .apiVersion(VPA_API_VERSION)
          .kind(VPA_KIND)
          .metadata(new V1ObjectMeta().name(flowVPAName))
          .spec(new V1VerticalPodAutoscalerSpec()
              .selector(new V1LabelSelector().matchLabels(Collections.singletonMap(flowVPALabelName,
                  flowVPAName)))
              .resourcePolicy(new V1VerticalPodAutoscalerSpecResourcePolicy()
                  .containerPolicies(
                      Collections.singletonList(new V1VerticalPodAutoscalerSpecResourcePolicyContainerPolicies()
                          .containerName(flowContainerName)
                          .maxAllowed(ImmutableMap
                              .of(VPA_CPU_KEY, maxAllowedCPU, VPA_MEMORY_KEY, maxAllowedMemory))
                      ))
              )
              .updatePolicy(new V1VerticalPodAutoscalerSpecUpdatePolicy().updateMode(UpdateModeEnum.OFF))
          );

      kubernetesVPARecommenderV1Api.createNamespacedVPA(namespace, vpaObject);
    } catch (Exception e) {
      throw new ExecutorManagerException(e);
    }
  }

  @Override
  public VPARecommendation getFlowContainerRecommendedRequests(final String namespace,
      final String flowVPALabelName, final String flowVPAName,
      final String flowContainerName, final double cpuRecommendationMultiplier,
      final double memoryRecommendationMultiplier, final String maxAllowedCPU,
      final String maxAllowedMemory) throws ExecutorManagerException {
    final VPARecommendation maxAllowedLimits = new VPARecommendation(maxAllowedCPU,
        maxAllowedMemory);
    try {
      final Quantity maxAllowedCPUQuantity = new Quantity(maxAllowedCPU);
      final Quantity maxAllowedMemoryQuantity = new Quantity(maxAllowedMemory);

      try {
        V1VerticalPodAutoscaler vpaObject =
            kubernetesVPARecommenderV1Api.getNamespacedVPA(namespace, flowVPAName).getData();

        Map<String, Object> recommendation =
            vpaObject.getStatus().getRecommendation().getContainerRecommendations()
                .stream()
                .filter(r -> flowContainerName.equals(r.getContainerName()))
                .findFirst()
                .get()
                .getTarget();

        Quantity rawCpuRecommendationQuantity =
            new Quantity((String) recommendation.get(VPA_CPU_KEY));
        Quantity rawMemoryRecommendationQuantity =
            new Quantity((String) recommendation.get(VPA_MEMORY_KEY));

        // Kubernetes doesn't allow you to specify CPU resources with a precision finer than 1m
        String cpuRecommendationMilliUnit =
            rawCpuRecommendationQuantity.getNumber().multiply(new BigDecimal(cpuRecommendationMultiplier * 1000)).toBigInteger().toString();
        // Better to specify memory resources with a precision as fine as 1 unit
        String memoryRecommendationUnit =
            rawMemoryRecommendationQuantity.getNumber().multiply(new BigDecimal(memoryRecommendationMultiplier)).toBigInteger().toString();

        Quantity cpuRecommendationQuantity = new Quantity(cpuRecommendationMilliUnit + "m");
        Quantity memoryRecommendationQuantity = new Quantity(memoryRecommendationUnit);

        logger.info("Raw recommendation quantities: " + rawCpuRecommendationQuantity.toSuffixedString() + ", " + rawMemoryRecommendationQuantity.toSuffixedString());
        logger.info("Converted recommendation quantities: " + cpuRecommendationQuantity.getNumber() + cpuRecommendationQuantity.getFormat() + ", " + memoryRecommendationQuantity.getNumber() + memoryRecommendationQuantity.getFormat());

        return new VPARecommendation(
            cpuRecommendationQuantity.getNumber().compareTo(maxAllowedCPUQuantity.getNumber()) < 0
                ? cpuRecommendationQuantity.toSuffixedString()
                : maxAllowedCPUQuantity.toSuffixedString(),
            memoryRecommendationQuantity.getNumber().compareTo(maxAllowedMemoryQuantity.getNumber()) < 0
                ? memoryRecommendationQuantity.toSuffixedString()
                : maxAllowedMemoryQuantity.toSuffixedString()
        );
      } catch (ApiException e) {
        // If VPA object not found: should create and then apply max resource limits.
        // Top-down approach to apply maxAllowed first and then find the optimal resource request
        if (e.getCode() == HttpStatus.SC_NOT_FOUND) {
          this.createVPAObject(namespace, flowVPALabelName, flowVPAName, flowContainerName,
              maxAllowedCPU, maxAllowedMemory);
          return maxAllowedLimits;
        }

        // Unknown errors
        throw e;
      } catch (NullPointerException | NoSuchElementException e) {
        logger.warn("VPA object for flowVPAName " + flowVPAName + " is found but recommendation has "
            + "not been generated yet", e);
        // It is highly possible that this flow completes too fast to be captured by VPA.
        // Therefore, do not apply VPA recommendation to this flow.
        throw e;
      }
    } catch (Exception e) {
      throw new ExecutorManagerException(e);
    }
  }
}
