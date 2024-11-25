package com.homo.turntable.core;

import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;

import java.io.*;
import java.nio.charset.StandardCharsets;

@Slf4j
public class JsonUtil {

    /**
     * 读取json文件
     */
    public static String readFile(String path) {
        BufferedReader reader = null;
        StringBuilder laststr = new StringBuilder();
        try {
            /**
             通过路径获取流文件（注：这种形式能够确保在以jar包形式运行时也可以成功获取到文件，之前
             踩过坑，长心长心）**/
            InputStream inputStream = new ClassPathResource(path).getInputStream();
            //设置字符编码为UTF-8，避免读取中文乱码
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream,"UTF-8");
            // 通过BufferedReader进行读取
            reader = new BufferedReader(inputStreamReader);
            String tempString = null;
            while ((tempString = reader.readLine()) != null) {
                laststr.append(tempString);
            }
            //关闭BufferedReader
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    //不管执行是否出现异常，必须确保关闭BufferedReader
                    reader.close();
                } catch (IOException e1) {
                }
            }
        }
        return laststr.toString();
    }

    /**
     * 写入json文件
     */
    public static void write(JSONObject jsonObject, String filePath) throws JSONException, IOException {
        writeFile(filePath, jsonObject.toString());
    }

    public static void writeFile(String filePath, String sets)
            throws IOException {
        try {
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filePath, false), StandardCharsets.UTF_8));
            PrintWriter out = new PrintWriter(writer);
            out.write(sets);
            out.println();
            writer.close();
            out.close();
        } catch (Exception e) {
            // 处理异常，例如打印错误日志
            log.error("写文件信息失败： {}", filePath, e);
            throw e; // 可以选择重新抛出异常，也可以选择不抛出，根据实际情况决定
        }
    }

}
