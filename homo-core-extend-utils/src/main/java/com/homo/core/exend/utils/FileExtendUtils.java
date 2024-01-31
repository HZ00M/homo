package com.homo.core.exend.utils;

import io.kubernetes.client.common.KubernetesType;
import io.kubernetes.client.util.Yaml;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.FileUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

@Slf4j
@UtilityClass
public class FileExtendUtils {
    private static String[] INCLUDES = new String[]{"**/*.class"};
    private static String[] EXCLUDES = new String[]{"**/*$?.class"};

    public static String mergePath(String p, String... ps) {
        return Paths.get(p, ps).toString();
    }

    /**
     * 获取Jar包下普通的className
     */
    public Set<String> getClassNameFromJarFile(File givenFile) throws IOException {
        Set<String> classNames = new HashSet<>();
        try (JarFile jarFile = new JarFile(givenFile)) {
            Enumeration<JarEntry> entries = jarFile.entries();
            while (entries.hasMoreElements()) {
                JarEntry jarEntry = entries.nextElement();
                /**
                 * 只扫描class文件，排除匿名类，
                 */
                if (jarEntry.getName().endsWith(".class") && !jarEntry.getName().contains("$")) {
                    String className = jarEntry.getName().replace(File.separator, ".")
                            .replace("/", ".")
                            .replace(".class", "");
                    classNames.add(className);
                }
            }
        }
        return classNames;
    }

    /**
     * 获取类文件的类名
     *
     * @param files
     * @return
     */
    public Set<String> getClassSimpleNameFromFiles(List<File> files) {
        Set<String> classNames = new HashSet<>();
        for (File file : files) {
            String className = FileUtils.removeExtension(file.getName()).replace(File.separator, ".").replace("/", ".");
            classNames.add(className);
        }
        return classNames;
    }

    /**
     * 获取目录下所有的类文件
     */
    public static List<File> findClassFilesFromDirectory(File directory) {
        if (directory == null) {
            throw new RuntimeException("'directory' is null");
        } else if (!directory.isDirectory()) {
            throw new RuntimeException(String.format("%s is not a directory", directory));
        } else {
            try {
                String includes = StringUtils.join(INCLUDES, ",");
                String excludes = StringUtils.join(EXCLUDES, ",");
                return FileUtils.getFiles(directory, includes, excludes);
            } catch (IOException e) {
                throw new RuntimeException(String.format("Unable to retrieve the list of files: %s", e.getMessage()), e);
            }
        }
    }

    /**
     * 判断文件路径是否是绝对路径
     *
     * @param path
     * @return
     */
    public boolean isAbsolutePath(String path) {
        Path absolutePath = Paths.get(path);
        return absolutePath.isAbsolute();
    }

    /**
     * 读取yaml文件
     *
     * @param fileName
     * @param isResource
     * @param <T>
     * @return
     */
    public static <T> T readYamlToObj(String fileName, Class<T> obj, boolean isResource) throws IOException {
        String fileContent = readCharacterFileToUtf8Str(fileName, isResource);
        return Yaml.loadAs(fileContent, obj);
    }

    /**
     * 读取文件内容转换为字符串
     *
     * @param fileName
     * @param isResource
     * @return
     */
    public static String readCharacterFileToUtf8Str(String fileName, boolean isResource) throws IOException {
        /**
         * 如果是资源文件直接使用类加载器读取资源文件
         * 如果是文件则使用Files.newInputStream读取文件
         */
        InputStream inputStream = isResource ? Thread.currentThread().getContextClassLoader().getResourceAsStream(fileName) : Files.newInputStream(Paths.get(fileName));
        byte[] bytes = new byte[inputStream.available()];
        inputStream.read(bytes);
        inputStream.close();
        return new String(bytes, StandardCharsets.UTF_8);
    }

    /**
     * 读取文件内容转换为字符串
     * @return
     */
    public static String readCharacterFileToUtf8Str(File file) throws IOException {
        /**
         * 如果是资源文件直接使用类加载器读取资源文件
         * 如果是文件则使用Files.newInputStream读取文件
         */
        InputStream inputStream = Files.newInputStream(file.toPath());
        byte[] bytes = new byte[inputStream.available()];
        inputStream.read(bytes);
        inputStream.close();
        return new String(bytes, StandardCharsets.UTF_8);
    }

    public static <T> void writeK8sObjToFile(String fileName, T k8sObj) throws IOException {
        File file = new File(fileName);
        String k8sObjYamlStr = Yaml.dump(k8sObj);
        writeStringFile(file, k8sObjYamlStr);
    }

    public static void writeStringFile(File file, String content) throws IOException {
        createFileIfAbsent(file.getParentFile());
        try (FileWriter writer = new FileWriter(file)) {
            writer.write(content);
            writer.flush();
        } catch (IOException e) {
            log.error("saveStringFile error file {} content {} ", file.getAbsolutePath(), content, e);
            throw e;
        }
    }
    public static void write2File(String pathName, String content) throws IOException {
        File file = new File(pathName);
        createFileIfAbsent(file.getParentFile());
        BufferedWriter writer = new BufferedWriter(new FileWriter(file));
        writer.write(content);
        writer.close();
    }
    private static boolean createFileIfAbsent(File dir) {
        /**
         * File.mkdir()方法用于创建单个目录。如果给定路径中的父目录不存在，则无法创建该目录，并且会返回false。只有在父目录存在的情况下，才能成功创建单个目录。
         *
         * File.mkdirs()方法则可以同时创建多级目录。如果给定路径中的任何一个父目录不存在，该方法会自动创建缺失的父目录。
         * 这意味着即使整个路径中的多级父目录都不存在，也能成功地创建最终指定的目录。如果所有必要的父级目录已经存在或者成功地被创建了，该方法会返回true；否则返回false。
         */
        return dir.mkdirs();
    }

    /**
     * 找到maven项目所有的class的名字全限定名
     *
     * @param project
     * @return
     */
    public static Set<String> findAllClassNamesFromMavenProject(MavenProject project) {
        String outputDir = project.getBuild().getOutputDirectory();
        Set<String> classNames = new HashSet<>();
        List<File> classFiles = findClassFilesFromDirectory(new File(outputDir));
        for (File classFile : classFiles) {
            String path = classFile.getPath();
            String name = FileUtils.removeExtension(path.substring(outputDir.length() + 1))
                    .replace(File.separator, ".")
                    .replace("/", ".");
            classNames.add(name);
        }
        return classNames;
    }

    public static <T extends KubernetesType> void writeYamlObjToFile(String fileName, Iterator<T> iterator) throws IOException {
        File file = new File(fileName);
        createFileIfAbsent(file.getParentFile());
        FileWriter fileWriter = new FileWriter(file);
        String serviceContent = Yaml.dumpAll(iterator);
        fileWriter.write(serviceContent);
        fileWriter.flush();
        fileWriter.close();
    }

    /**
     * 删除指定文件列表
     *
     * @param fileArray
     */
    public static void deleteFiles(String[] fileArray) {
        for (String fileName : fileArray) {
            File file = new File(fileName);
            file.deleteOnExit();
        }
    }
    public static Map<String, Properties> readPropertiesFiles(String directoryPath) {
        File directory = new File(directoryPath);
        Map<String, Properties> propertiesMap = new HashMap<>();

        if (directory.exists() && directory.isDirectory()) {
            readPropertiesFilesRecursive(directory, propertiesMap);
        }

        return propertiesMap;
    }

    private static void readPropertiesFilesRecursive(File directory, Map<String, Properties> propertiesMap) {
        File[] files = directory.listFiles();

        if (files != null) {
            for (File file : files) {
                if (file.isFile() && file.getName().endsWith(".properties")) {
                    Properties properties = new Properties();
                    try (FileInputStream fis = new FileInputStream(file)) {
                        properties.load(fis);
                        propertiesMap.put(file.getAbsolutePath(), properties);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else if (file.isDirectory()) {
                    readPropertiesFilesRecursive(file, propertiesMap);
                }
            }
        }
    }


    public static List<KubernetesType> readFileToK8sObjs(String buildServiceFilePath, boolean isResource) throws IOException {
        String content = readCharacterFileToUtf8Str(buildServiceFilePath, isResource);
        List<Object> loadAll = Yaml.loadAll(content);
        List<KubernetesType> kubeObjs = new ArrayList<>();
        for (Object obj : loadAll) {
            kubeObjs.add((KubernetesType) obj);
        }
        return kubeObjs;
    }

    public static File getProjectRootDir(File moduleBasedir) {
        File rootDir = moduleBasedir;
        while (rootDir != null) {
            File pomFile = new File(rootDir.getParentFile(), "pom.xml");
            if (!pomFile.exists()) {
                return rootDir;
            }
            rootDir = rootDir.getParentFile();
        }
        return rootDir;
    }
}
