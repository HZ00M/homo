package com.homo.core.exend.client;

import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.apis.AppsV1Api;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.*;
import io.kubernetes.client.util.Config;
import io.kubernetes.client.util.KubeConfig;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.StringReader;

@Slf4j
public class K8sExtendClient {
    public ApiClient apiClient;
    public AppsV1Api appsV1Api;
    public CoreV1Api coreV1Api;
    private K8sExtendClient(){}
    public K8sExtendClient(String kubeConfig) throws IOException {
        KubeConfig config = KubeConfig.loadKubeConfig(new StringReader(kubeConfig));
        apiClient = Config.fromConfig(config);
        appsV1Api = new AppsV1Api(apiClient);
        coreV1Api = new CoreV1Api(apiClient);
    }

    public void createNamespaceIfAbsent(String namespace) throws ApiException {
        String fileSelector = String.format("metadata.name=%s",namespace);
        V1NamespaceList v1NamespaceList = coreV1Api.listNamespace(null, false, null, fileSelector, null, 1, null, null, false);
        if(v1NamespaceList == null || v1NamespaceList.getItems().size() == 0){
            log.info("createNamespaceIfAbsent namespace {} is absent,create it",namespace);
            V1Namespace v1Namespace = new V1Namespace();
            V1ObjectMeta meta = new V1ObjectMeta();
            meta.setName(namespace);
            v1Namespace.setMetadata(meta);
            coreV1Api.createNamespace(v1Namespace,null,null,null);
        }else {
            log.info("createNamespaceIfAbsent namespace {} is exist,skip it",namespace);
        }
    }

    public void updateConfigMap(String namespace, V1ConfigMap configMap) throws ApiException {
        String fileSelector = String.format("metadata.name=%s",configMap.getMetadata().getName());
        V1ConfigMapList v1ConfigMapList = coreV1Api.listNamespacedConfigMap(namespace, null, false, null, fileSelector, null, 1, null, null, false);
        if (v1ConfigMapList == null || v1ConfigMapList.getItems().size() == 0) {
            log.info("updateConfigMap namespace {}  is absent,create it", namespace);
            coreV1Api.createNamespacedConfigMap(namespace, configMap, null, null, null);
        }else {
            log.info("updateConfigMap namespace {}  is exist,update it", namespace);
            coreV1Api.replaceNamespacedConfigMap(configMap.getMetadata().getName(), namespace, configMap, null, null, null);
        }
    }

    public void updateService(String namespace, V1Service service, boolean delIfExist) throws ApiException {
        String serviceName = service.getMetadata().getName();
        String filterSelector = String.format("metadata.name=%s",serviceName);
        V1ServiceList v1ServiceList = coreV1Api.listNamespacedService(namespace, null, false, null, filterSelector, null, 1, null, null, false);
        if (v1ServiceList == null || v1ServiceList.getItems().size() == 0) {
            log.info("updateService namespace {} service {} is absent,create it", namespace, serviceName);
            coreV1Api.createNamespacedService(namespace, service, null, null, null);
        }else {

            if(delIfExist){
                log.info("updateService namespace {} service {} is exist,delete and create new", namespace, serviceName);
                coreV1Api.deleteNamespacedService(serviceName, namespace, null, null, 1, null, null, null);
                coreV1Api.createNamespacedService(namespace, service, null, null, null);
            }else {
                log.info("updateService namespace {} service {} is exist,skip it", namespace, serviceName);
                /**
                 * 更新service时，需要传入resourceVersion，否则会报错
                 */
//                String resourceVersion = v1ServiceList.getMetadata().getResourceVersion();
//                coreV1Api.replaceNamespacedService(serviceName, namespace, service, null, null, null);
            }
        }
    }

    public void updateEndpoints(String namespace, V1Endpoints endpoints) {
        String endpointName = endpoints.getMetadata().getName();
        String filterSelector = String.format("metadata.name=%s",endpointName);
        try {
            V1EndpointsList v1EndpointsList = coreV1Api.listNamespacedEndpoints(namespace, null, false, null, filterSelector, null, 1, null, null, false);
            if (v1EndpointsList == null || v1EndpointsList.getItems().size() == 0) {
                log.info("updateEndpoints namespace {} endpoints {} is absent,create it", namespace, endpoints.getSubsets().get(0).getAddresses().get(0).getIp());
                coreV1Api.createNamespacedEndpoints(namespace, endpoints, null, null, null);
            }else {
                log.info("updateEndpoints namespace {} endpoints {} is exist,update it", namespace, endpoints.getSubsets().get(0).getAddresses().get(0).getIp());
                coreV1Api.replaceNamespacedEndpoints(endpointName, namespace, endpoints, null, null, null);
            }
        } catch (ApiException e) {
            log.error("updateEndpoints error namespace {} endpoints {} ", namespace, endpoints, e);
        }
    }

    public void findAndDeleteStatefulSet(String k8sNamespace, String statefulName) {
        String filterSelector = String.format("metadata.name=%s",statefulName);
        try {
            V1StatefulSetList v1StatefulSetList = appsV1Api.listNamespacedStatefulSet(k8sNamespace, null, false, null, filterSelector, null, 1, null, null, false);
            if (v1StatefulSetList != null && v1StatefulSetList.getItems().size() > 0) {
                log.info("findAndDeleteStatefulSet namespace {} statefulName {} is exist,delete it", k8sNamespace, statefulName);
                appsV1Api.deleteNamespacedStatefulSet(statefulName, k8sNamespace, null, null, 1, null, null, null);
            }
        } catch (ApiException e) {
            log.error("findAndDeleteStatefulSet error namespace {} statefulName {} ", k8sNamespace, statefulName, e);
        }
    }
}
