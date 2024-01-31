package com.homo.core.exend.client;

import com.alibaba.fastjson.JSONObject;
import com.ctrip.framework.apollo.openapi.client.ApolloOpenApiClient;
import com.ctrip.framework.apollo.openapi.dto.*;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
public class ApolloExtendClient {
    public ApolloOpenApiClient client;

    private ApolloExtendClient() {
    }

    public ApolloExtendClient(String addr, String token) {
        client = ApolloOpenApiClient.newBuilder()
                .withPortalUrl(addr)
                .withToken(token)
                .build();
    }
    public void createOrUpdateNamespaceCoverValue(String appId, String env, String cluster, String editor, boolean isPublic, String namespace, Map<String, String> propertyMap) {
        createAndInitNamespace(appId, env, cluster, editor, isPublic, namespace, null);
        OpenNamespaceDTO namespaceDTO = client.getNamespace(appId, env, cluster, namespace);
        if (namespaceDTO != null) {
            propertyMap.forEach((key, value) -> {
                OpenItemDTO openItemDTO = new OpenItemDTO();
                openItemDTO.setKey(key);
                openItemDTO.setValue(value);
                client.createOrUpdateItem(appId, env, cluster, namespace, openItemDTO);
            });
        } else {
            log.info("createOrUpdateNamespaceOnAbsent fail,namespace is absent.\nappId: {},env: {},cluster: {},namespace: {}", appId, env, cluster, namespace);
        }
    }

    public void createOrUpdateNamespaceUpdateIfAbsent(String appId, String env, String cluster, String editor, boolean isPublic, String namespace, Map<String, String> propertyMap) {
        createAndInitNamespace(appId, env, cluster, editor, isPublic, namespace, propertyMap);
        OpenNamespaceDTO namespaceDTO = client.getNamespace(appId, env, cluster, namespace);
        if (namespaceDTO != null) {
            Map<String, String> oldPropertyMap = namespaceDTO.getItems().stream().collect(Collectors.toMap(OpenItemDTO::getKey, OpenItemDTO::getValue));
            propertyMap.forEach((key, value) -> {
                if (oldPropertyMap.containsKey(key)) {
                    return;
                }
                OpenItemDTO openItemDTO = new OpenItemDTO();
                openItemDTO.setKey(key);
                openItemDTO.setValue(value);
                client.createOrUpdateItem(appId, env, cluster, namespace, openItemDTO);
            });
        } else {
           log.info("createOrUpdateNamespaceOnAbsent fail,namespace is absent.\nappId: {},env: {},cluster: {},namespace: {}", appId, env, cluster, namespace);
        }
    }

    private void createAndInitNamespace(String appId, String env, String cluster, String editor, boolean isPublic, String namespace, Map<String, String> propertyMap) {
        List<OpenNamespaceDTO> namespaces = client.getNamespaces(appId, env, cluster);
        boolean haveNamespace = false;
        for (OpenNamespaceDTO openNamespaceDTO : namespaces) {
            if (openNamespaceDTO.getNamespaceName().equals(namespace)) {
                haveNamespace = true;
                break;
            }
        }
        if (!haveNamespace) {
            log.info("createOrUpdateNamespaceOnAbsent fail,namespace is absent.\nappId: {},env: {},cluster: {},namespace: {}", appId, env, cluster, namespace);
            OpenAppNamespaceDTO openAppNamespaceDTO = new OpenAppNamespaceDTO();
            openAppNamespaceDTO.setAppId(appId);
            openAppNamespaceDTO.setFormat("properties");
            openAppNamespaceDTO.setName(namespace);
            openAppNamespaceDTO.setPublic(isPublic);
            openAppNamespaceDTO.setComment("auto create");
            openAppNamespaceDTO.setDataChangeCreatedBy(editor);
            OpenAppNamespaceDTO appNamespace = client.createAppNamespace(openAppNamespaceDTO);

            propertyMap.forEach((key, value) -> {
                OpenItemDTO openItemDTO = new OpenItemDTO();
                openItemDTO.setKey(key);
                openItemDTO.setValue(value);
                client.createOrUpdateItem(appId, env, cluster, namespace, openItemDTO);
            });

        }
    }

    public void createOrUpdatePropertyNamespaces(String appId, String env, String cluster, String editor, List<OpenNamespaceDTO> namespaceDTOList) {
        List<OpenEnvClusterDTO> envClusterInfo = client.getEnvClusterInfo(appId);
        boolean hasEnv = false;
        boolean hasCluster = false;
        for (OpenEnvClusterDTO openEnvClusterDTO : envClusterInfo) {
            if (openEnvClusterDTO.getEnv().equals(env)) {
                hasEnv = true;
                hasCluster = openEnvClusterDTO.getClusters().contains(cluster);
            }
        }
        if (!hasEnv) {
            String errMsg = String.format("createOrUpdatePropertyNamespaces fail,not find env.please create env before operator.\nappId: %s,env: %s,cluster: %s", appId, env, cluster);
            throw new RuntimeException(errMsg);
        }
        if (!hasCluster) {
            String errMsg = String.format("createOrUpdatePropertyNamespaces fail,not find cluster.please create cluster before operator.\nappId: %s,env: %s,cluster: %s", appId, env, cluster);
            throw new RuntimeException(errMsg);
        }

        NamespaceReleaseDTO namespaceReleaseDTO = new NamespaceReleaseDTO();
        namespaceReleaseDTO.setReleaseTitle("auto release");
        namespaceReleaseDTO.setReleasedBy(editor);
        List<OpenNamespaceDTO> presentOpenNamespaceDTO = client.getNamespaces(appId, env, cluster);
        for (OpenNamespaceDTO openNamespaceDTO : namespaceDTOList) {
            if (!isNamespaceExist(presentOpenNamespaceDTO, openNamespaceDTO)) {
                //该namespace不存在，则创建
                OpenAppNamespaceDTO openAppNamespaceDTO = new OpenAppNamespaceDTO();
                openAppNamespaceDTO.setAppId(openNamespaceDTO.getAppId());
                openAppNamespaceDTO.setFormat("properties");
                openAppNamespaceDTO.setName(openNamespaceDTO.getNamespaceName());
                openAppNamespaceDTO.setPublic(openNamespaceDTO.isPublic());
                openAppNamespaceDTO.setComment(openNamespaceDTO.getComment());
                openAppNamespaceDTO.setDataChangeCreatedBy(editor);
                client.createAppNamespace(openAppNamespaceDTO);
            } else {
                //检查当前namespace与apollo上的namespace是否一致，不一致则不更新
                OpenNamespaceDTO remoteNamespace = client.getNamespace(appId, env, cluster, openNamespaceDTO.getNamespaceName());
                if (remoteNamespace.isPublic() != openNamespaceDTO.isPublic() || !remoteNamespace.getComment().equals(openNamespaceDTO.getComment())) {
                    String errMsg = String.format("createOrUpdatePropertyNamespaces namespace update  fail,isPublic is conflict ,.please check config before operator." +
                                    "\nappId: %s,env: %s,cluster: %s namespace: %s",
                            appId, env, cluster, openNamespaceDTO.getNamespaceName());
                    throw new RuntimeException(errMsg);
                }
            }
            for (OpenItemDTO item : openNamespaceDTO.getItems()) {
                item.setDataChangeCreatedBy(editor);
                client.createOrUpdateItem(appId, env, cluster, openNamespaceDTO.getNamespaceName(), item);
            }
            //发布变更

            client.publishNamespace(appId, env, cluster, openNamespaceDTO.getNamespaceName(), namespaceReleaseDTO);
        }

    }

    /**
     * 将键值对配置的Map转换为OpenNamespaceDTO
     *
     * @param appId
     * @param cluster
     * @param namespace
     * @param isPublic
     * @param propertyMap
     * @return
     */
    public OpenNamespaceDTO propertyMapToNamespaceDTO(String appId, String cluster, String namespace, boolean isPublic, Map<String, String> propertyMap) {
        OpenNamespaceDTO openNamespaceDTO = new OpenNamespaceDTO();
        openNamespaceDTO.setAppId(appId);
        openNamespaceDTO.setClusterName(cluster);
        openNamespaceDTO.setNamespaceName(namespace);
        openNamespaceDTO.setPublic(isPublic);
        openNamespaceDTO.setFormat("properties");
        List<OpenItemDTO> itemDTOS = new ArrayList<>();
        propertyMap.forEach((key, value) -> {
            OpenItemDTO openItemDTO = new OpenItemDTO();
            openItemDTO.setKey(key);
            openItemDTO.setValue(value);
            itemDTOS.add(openItemDTO);
        });
        openNamespaceDTO.setItems(itemDTOS);
        return openNamespaceDTO;
    }

    /**
     * 将键值对配置的json转换为OpenNamespaceDTO
     *
     * @param appId
     * @param cluster
     * @param namespace
     * @param isPublic
     * @param propertyJson
     * @return
     */
    public OpenNamespaceDTO propertyJsonToNamespaceDTO(String appId, String cluster, String namespace, boolean isPublic, JSONObject propertyJson) {
        Map<String, String> propertyMap = new HashMap<>();
        propertyJson.forEach((key, value) -> {
            propertyMap.put(key, value.toString());
        });
        return propertyMapToNamespaceDTO(appId, cluster, namespace, isPublic, propertyMap);
    }

    public OpenNamespaceDTO pullNamespaceInfo(String appId, String env, String cluster, String namespace) {
        OpenNamespaceDTO openNamespaceDTO = client.getNamespace(appId, env, cluster, namespace);
        return openNamespaceDTO;
    }

    public List<OpenNamespaceDTO> pullClusterInfo(String appId, String env, String cluster) {
        List<OpenNamespaceDTO> openNamespaceDTOS = client.getNamespaces(appId, env, cluster);
        return openNamespaceDTOS;
    }


    private static boolean isNamespaceExist(List<OpenNamespaceDTO> openNamespaceDTOS, OpenNamespaceDTO openNamespaceDTO) {
        String namespace = openNamespaceDTO.getNamespaceName();
        for (OpenNamespaceDTO namespaceDTO : openNamespaceDTOS) {
            if (namespaceDTO.getNamespaceName().equals(namespace)) {
                return true;
            }
        }
        return false;
    }


}
