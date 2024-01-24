package com.homo.core.exend.utils;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;

@Slf4j
@UtilityClass
public class CommandUtils {
    private static final boolean isWin = System.getProperty("os.name").toLowerCase().contains("win");
    public static boolean executeCommand(List<String> commands, List<String> result) {
        log.debug("executeCommand start, commands: {}", commands);
        Runtime run = Runtime.getRuntime();
        try {
            List<String> commandList = getShellCommand();
            commandList.addAll(commands);
            Process proc = run.exec(commandList.toArray(new String[0]));
            //起两个进程读取输出流，防止阻塞
            new ProcessRunner(proc.getInputStream(),result).start();
            new ProcessRunner(proc.getErrorStream(),result).start();
            proc.waitFor();
            log.debug("executeCommand end, commands: {}", commands);
            int exitValue = proc.exitValue();
            proc.destroy();
            return exitValue == 0;
        } catch (IOException | InterruptedException e1) {
            log.info("exec command error ", e1);
            return false;
        }
    }

    private static List<String> getShellCommand() {
        List<String> commands = new LinkedList<>();
        if (isWin) {
            /**
             * cmd /X /c 是一个 Windows 命令行参数的组合。
             *
             * /X 参数用于启用扩展语法，允许使用额外的命令行操作符和字符串括号。
             * /c 参数用于执行完指定的命令后关闭命令行窗口。
             */
            commands.add("cmd");
            commands.add("/X");
            commands.add("/C");
        } else {
            /**
             * /bin/sh -c 是一个在Unix和类Unix系统中使用的命令行参数组合。
             *
             * /bin/sh 是指定要使用的shell程序路径，通常是Bourne shell或其变种。
             * -c 参数表示后面跟着要执行的命令或命令字符串。
             */
            commands.add("/bin/sh");
            commands.add("-c");
        }
        return null;
    }

    private static class ProcessRunner implements Runnable{
        private final InputStream inputStream;
        private final List<String> result;
        public ProcessRunner(InputStream inputStream,List<String> result) {
            this.inputStream = inputStream;
            this.result = result;
        }

        @Override
        public void run() {
             BufferedReader br = null;
            try {
                br = new BufferedReader(new InputStreamReader(inputStream,getEncoding()));
                String line = null;
                while ((line = br.readLine()) != null) {
                    log.info(line);
                    if(result != null){
                        result.add(line);
                    }
                }
            } catch (IOException e) {
                log.error("read process stream error", e);
            } finally {
                if (br != null) {
                    try {
                        br.close();
                    } catch (IOException e) {
                        log.error("close process stream error", e);
                    }
                }
            }
        }
        private static String getEncoding() {
            if (isWin) {
                // windows 下是 GBK
                return "GBK";
            }
            return "UTF-8";
        }
        public void start(){
            new Thread(this).start();
        }
    }
}
