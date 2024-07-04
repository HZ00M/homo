package com.homo.core.utils.origin;

import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;


/**
 * <p>Project: oppo-framework-parent</p>
 * <p>Title: FileOperate</p>
 * <p>Description: 文件操作工具类</p>
 * <p>Copyright (c) 2015 www.oppo.com Inc. All rights reserved.</p>
 * <p>Company: OPPO</p>
 *
 * @author Yibowen
 * @time 14:19
 * @since 2015-05-27
 */
public class FileOperate {

    /**
     * 读取文件路径对应的文件中的对象
     *
     * @param filePath
     * @return
     */
    public static Object getFileObject(String filePath) throws IOException, ClassNotFoundException {
        return getFileObject(new File(filePath));
    }

    /**
     * 读取文件中的对象
     *
     * @param file
     * @return
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public static Object getFileObject(File file) throws IOException, ClassNotFoundException {
        if (!file.exists()) {
            return null;
        }

        ObjectInputStream ois = null;

        try {
            ois = new ObjectInputStream(new FileInputStream(file));
            return ois.readObject();
        } finally {
            close(ois);
        }
    }

    /**
     * 读取文件路径对应的文件的文本内容
     *
     * @param filePath 文件路径
     * @param encoding
     * @return
     */
    public static String getFileContent(String filePath, String encoding) throws Exception {
        return getFileContent(new File(filePath), encoding);
    }

    /**
     * 读取文件的文本内容
     *
     * @param file
     * @param encoding
     * @return
     */
    public static String getFileContent(File file, String encoding) throws IOException {
        if (!file.exists()) {
            return null;
        }

        BufferedInputStream bis = null;

        try {
            bis = new BufferedInputStream(new FileInputStream(file));
            if (StringUtils.isBlank(encoding)) {
                encoding = "UTF-8";
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            int readLen;
            byte[] data = new byte[4096];
            while ((readLen = bis.read(data)) != -1) {
                baos.write(data, 0, readLen);
            }
            return baos.toString(encoding);

//	        //只能读取小文件
//	        int len = (int)file.length();
//	        byte[] data = new byte[len];
//	        bis.read(data, 0, len);
//
//	        return new String(data, encoding);
        } finally {
            close(bis);
        }
    }

    /**
     * 读取文件大小
     *
     * @param file
     * @return
     */
    public static long getFileLength(File file) throws IOException {
        if (!file.exists()) {
            return 0;
        }

        BufferedInputStream bis = null;

        try {
            bis = new BufferedInputStream(new FileInputStream(file));

            int readLen;
            long total = 0;
            byte[] data = new byte[4096];
            while ((readLen = bis.read(data)) != -1) {
                total += readLen;
            }
            return total;
        } finally {
            close(bis);
        }
    }

    /**
     * 高并发时获取小文件内容
     *
     * @param
     * @return
     */
    public static String getShareContent(File file, String encoding) throws IOException {
        byte[] data = getShareBytes(file);
        if (null == data) {
            return null;
        } else {
            if (StringUtils.isBlank(encoding)) {
                encoding = "UTF-8";
            }

            return new String(data, encoding);
        }
    }

    /**
     * 高并发时获取小文件内容,linux下可能无法正确读取到文件大小
     *
     * @param
     * @return
     */
    public static byte[] getShareBytes(File file) throws IOException {
        if (!file.exists()) {
            return null;
        }

        FileChannel fc = null;
        try {
            fc = new FileInputStream(file).getChannel();

            //读取到ByteBuffer中
            ByteBuffer bb = ByteBuffer.allocate((int) file.length());
            fc.read(bb);
            bb.flip();

            return bb.array();
        } finally {
            FileOperate.close(fc);
        }
    }

    /**
     * 保存内容到文件中
     *
     * @param filePath
     * @param content
     * @return
     */
    public static boolean saveContentToFile(String filePath, String content) throws Exception {
        return saveContentToFile(filePath, content, null);
    }

    /**
     * 保存内容到文件中
     *
     * @param filePath
     * @param content
     * @param encoding
     * @return
     */
    public static boolean saveContentToFile(String filePath, String content, String encoding) throws Exception {
        if (StringUtils.isBlank(encoding)) {
            encoding = "UTF-8";
        }

        return saveContentToFile(filePath, content.getBytes(encoding));
    }

    /**
     * 保存内容到文件中
     *
     * @param filePath
     * @param data
     * @return
     */
    public static boolean saveContentToFile(String filePath, byte[] data) throws Exception {
        if (!FileOperate.createDir(filePath, true)) {
            return false;
        }

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(filePath);
            fos.write(data);
            fos.flush();
            return true;
        } finally {
            close(fos);
        }
    }

    /**
     * 将对象保存到文件中
     *
     * @param filePath
     * @param obj
     * @return
     */
    public static boolean saveObjectToFile(String filePath, Object obj) throws IOException {
        if (!FileOperate.createDir(filePath, true)) {
            return false;
        }

        ObjectOutputStream oos = null;
        try {
            oos = new ObjectOutputStream(new FileOutputStream(filePath));
            oos.writeObject(obj);
            oos.flush();

            return true;
        } catch (IOException e) {
            throw e;
        } finally {
            close(oos);
        }
    }

    /**
     * 将inputStream存入到指定文件中
     *
     * @param
     * @return
     */
    public static boolean saveStreamToFile(String filePath, InputStream inputStream) throws IOException {
        return saveStreamToFile(new File(filePath), inputStream);
    }

    /**
     * 将inputStream存入到指定文件中
     *
     * @param
     * @return
     */
    public static boolean saveStreamToFile(File outputFile, InputStream inputStream) throws IOException {
        if (!FileOperate.createDir(outputFile, true)) {
            throw new IOException("can not create directory " + outputFile.getAbsolutePath());
        }

        OutputStream output = null;
        InputStream input = null;
        //将输入流进行包装
        if (inputStream instanceof BufferedInputStream) {
            input = inputStream;
        } else {
            input = new BufferedInputStream(inputStream);
        }

        try {
            output = new BufferedOutputStream(new FileOutputStream(outputFile));

            int bufferLen = 4096;
            byte[] data = new byte[bufferLen];
            int readLen;
            while ((readLen = input.read(data, 0, bufferLen)) != -1) {
                output.write(data, 0, readLen);
            }

            output.flush();

            return true;
        } finally {
            FileOperate.close(output);
        }
    }

    /**
     * 将制定源文件拷贝到目的文件
     *
     * @param sourceFile 源文件
     * @param destFile   目的文件
     * @return
     */
    public static boolean copyFile(String sourceFile, String destFile) throws IOException {
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(sourceFile);
            return saveStreamToFile(destFile, fis);
        } finally {
            close(fis);
        }
    }

    /**
     * 将制定源文件拷贝到目的文件
     *
     * @param sourceFile 源文件
     * @param destFile   目的文件
     * @return
     */
    public static boolean copyFile(File sourceFile, File destFile) throws IOException {
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(sourceFile);
            return saveStreamToFile(destFile, fis);
        } finally {
            close(fis);
        }
    }

    /**
     * 添加内容到文本中
     *
     * @param filePath
     * @param content
     * @return
     */
    public static boolean appendContentToFile(String filePath, String content) throws IOException {
        if (!createDir(filePath, true)) {
            return false;
        }

        BufferedWriter bw = null;

        try {
            bw = new BufferedWriter(new FileWriter(filePath, true));
            bw.write(content);
            bw.flush();
            return true;
        } finally {
            close(bw);
        }
    }

    /**
     * 创建目录
     *
     * @param path   路径
     * @param isFile
     * @return
     */
    public static boolean createDir(String path, boolean isFile) {
        File f = new File(path);
        if (isFile) {
            f = f.getParentFile();
        }

        if (!f.exists()) {
            return f.mkdirs();
        } else {
            return true;
        }
    }

    /**
     * 创建目录
     *
     * @param file   文件
     * @param isFile
     * @return
     */
    public static boolean createDir(File file, boolean isFile) {
        if (isFile) {
            file = file.getParentFile();
        }

        if (!file.exists()) {
            return file.mkdirs();
        } else {
            return true;
        }
    }

    /**
     * 关闭源
     *
     * @param clo
     * @return
     */
    public static boolean close(Closeable clo) {
        if (null == clo) {
            return true;
        }

        try {
            clo.close();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

}
