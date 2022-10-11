package azkaban.executor.container;


import azkaban.utils.Props;
import io.kubernetes.client.openapi.ApiClient;
import org.apache.commons.lang3.NotImplementedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VPARecommenderFactory {
//  private static final Logger logger = LoggerFactory.getLogger(VPARecommenderFactory.class);
//
//  public static VPARecommender getVPARecommender(Props azkProps, ApiClient client) {
//    final String vpaRecommenderClassParam =
//        azkProps.getString(KUBERNETES_VERTICAL_POD_AUTOSCALER_CLASS_PARAM, KubernetesVPARecommender.class.getName());
//
//    logger.info("Instantiating VPA Recommender class: " + vpaRecommenderClassParam);
//    if (vpaRecommenderClassParam.equals(KubernetesVPARecommender.class.getName())) {
//      return new KubernetesVPARecommender(client);
//    }
//
//    throw new NotImplementedException(VPARecommenderFactory.class.getName() + " does not "
//        + "support instantiating VPA Recommender class " + vpaRecommenderClassParam);
//  }
}
