package com.homo.core.exend.client;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.model.Image;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.core.DockerClientImpl;
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient;
import com.google.common.base.Strings;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class DockerExtentClient {
    public final static String WINDOW_DOCKER_SOCKET = "tcp://localhost:2375";
    public final static String UNIX_DOCKER_SOCKET = "unix:///var/run/docker.sock";
    public DockerClient dockerClient;
    public DockerExtentClient(String userName, String password) {
        String host = isOsWindows() ? WINDOW_DOCKER_SOCKET: UNIX_DOCKER_SOCKET;
        DockerClientConfig dockerConfig = DefaultDockerClientConfig.createDefaultConfigBuilder()
                .withDockerHost(host)
                .withRegistryUsername(userName)
                .withRegistryPassword(password)
                .build();
        ApacheDockerHttpClient dockerHttpClient = new ApacheDockerHttpClient.Builder()
                .dockerHost(dockerConfig.getDockerHost())
                .sslConfig(dockerConfig.getSSLConfig())
                .build();
        dockerClient = DockerClientImpl.getInstance(dockerConfig, dockerHttpClient);
    }

    public boolean isOsWindows() {
        String osName = System.getProperty("os.name");
        if (isBlank(osName)) {
            return false;
        }
        return osName.startsWith("Windows");
    }

    public static boolean isBlank(String str) {
        return Strings.nullToEmpty(str).trim().isEmpty();
    }

    public boolean isImageExist(String imageName) {
        List<Image> imageList = dockerClient.listImagesCmd()
                .withImageNameFilter(imageName)
                .exec();
        if (imageList == null || imageList.size() == 0) {
            return false;
        }else {
            return true;
        }
    }
}
