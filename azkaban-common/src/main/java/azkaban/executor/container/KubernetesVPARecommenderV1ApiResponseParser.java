package azkaban.executor.container;


import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

/**
 * API response schema is defined in VPA v1 crd:
 * https://github.com/kubernetes/autoscaler/blob/vertical-pod-autoscaler-0.9.2/vertical-pod-autoscaler/deploy/vpa-v1-crd.yaml
 */
public class KubernetesVPARecommenderV1ApiResponseParser {
  private static JSONObject parseRecommendations(JSONObject response) throws JSONException {
    // There must be only one flow container name for Azkaban executor pods.
    return response.getJSONObject("status").getJSONObject("recommendation").getJSONArray(
        "containerRecommendations").getJSONObject(0);
  }

  public static String parseCPURecommendation(JSONObject recommendations,
      String flowContainerName) throws JSONException {
    if (recommendations.get("containerName") != flowContainerName) {
      throw new JSONException("Unexpected containerName from Kubernetes VPA Recommender: " +
          recommendations.get("containerName") + " . It should be " + flowContainerName);
    }
    return recommendations.getJSONObject("target").getString("cpu");
  }

  public static String parseMemoryRecommendation(JSONObject recommendations,
      String flowContainerName) throws JSONException {
    if (recommendations.get("containerName") != flowContainerName) {
      throw new JSONException("Unexpected containerName from Kubernetes VPA Recommender: " +
          recommendations.get("containerName") + " . It should be " + flowContainerName);
    }
    return recommendations.getJSONObject("target").getString("memory");
  }
}