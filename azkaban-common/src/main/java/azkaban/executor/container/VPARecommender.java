package azkaban.executor.container;

import azkaban.executor.ExecutorManagerException;

public interface VPARecommender {
  /**
   * Get recommended flow container resource requests from VPA recommender. If not available, fall
   * back to the default ones.
   *
   * @param namespace Kubernetes namespace
   * @param flowVPALabelName Label name for the VPA object associated with the Azkaban flow
   * @param flowVPAName Name for the VPA object associated with the Azkaban flow
   * @param flowContainerName flow container name to provide recommendation
   * @param cpuRecommendationMultiplier CPU recommendation multiplier
   * @param memoryRecommendationMultiplier memory recommendation multiplier
   * @param maxAllowedCPU maximum allowed CPU
   * @param maxAllowedMemory maximum allowed memory
   * @return Recommended resource requests for a flow container
   */
  VPARecommendation getFlowContainerRecommendedRequests(String namespace, String flowVPALabelName,
      String flowVPAName, String flowContainerName,
      double cpuRecommendationMultiplier, double memoryRecommendationMultiplier,
      String maxAllowedCPU, String maxAllowedMemory) throws ExecutorManagerException;
}
