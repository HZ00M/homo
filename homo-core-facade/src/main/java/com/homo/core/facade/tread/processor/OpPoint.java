package com.homo.core.facade.tread.processor;


import com.homo.core.utils.rector.Homo;

/**
 * 操作顺序操作点
 */
public interface OpPoint {
    /**
     * 执行一个操作，如果操作成功就返回true，否则返回false
     * @return 操作结果
     * @throws Exception 异常
     */
    Homo<Boolean> exec() throws RuntimeException;

    /**
     * 获取操作排序号
     * @return 操作排序号
     */
    default int getOrder(){
        return 0;
    }

}
