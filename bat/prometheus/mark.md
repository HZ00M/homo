grafana中的prometheus监控 
节点cpu使用率promQL： 100 - (avg by (instance) (rate(node_cpu_seconds_total{mode="idle"}[2m])) * 100)
节点内存使用率promQL： 1 - (node_memory_MemAvailable_bytes{job="kubernetes-node"} / node_memory_MemTotal_bytes{job="kubernetes-node"})
节点磁盘使用率promQL： 1 - (node_filesystem_avail_bytes{job="kubernetes-node"} / node_filesystem_size_bytes{job="kubernetes-node"})
节点网络使用率promQL： rate(node_network_receive_bytes_total{device!="lo"}[5m])


集群节点CPU平均值使用率： avg(100 - (avg by (instance) (rate(node_cpu_seconds_total{mode="idle"}[2m])) * 100))
pod cpu use/request 2分钟平均使用
100
*
(
sum (
rate(container_cpu_usage_seconds_total{cluster="",namespace=~"$namespace",container!="",image!=""}[2m])
* on(namespace, pod) group_left(workload,workload_type)
namespace_workload_pod:kube_pod_owner:relabel{cluster="",namespace=~"$namespace",workload=~"$workload",pod=~"$pod"}
) by (pod, node,container,namespace)
/
sum (
kube_pod_container_resource_requests_cpu_cores{cluster="",namespace=~"$namespace"}
* on(namespace, pod) group_left(workload,workload_type)
namespace_workload_pod:kube_pod_owner:relabel{cluster="",namespace=~"$namespace",workload=~"$workload",pod=~"$pod"}
) by (pod, node,container,namespace)
)


采集器参考：
https://github.com/prometheus-operator/prometheus-operator/blob/main/Documentation/api.md#servicemonitor
https://github.com/prometheus-operator/prometheus-operator/blob/main/Documentation/api.md#podmonitorspec