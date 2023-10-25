package com.homo.core.utils.delegate;

import com.homo.core.utils.exception.HomoError;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;

/**
 * 广播者  用于在同一线程内广播消息到目标
 * 支持调用返回
 * @param <Target>
 */
@Slf4j
public abstract class BroadCasterCall<Target> extends BroadCaster<Target> {

    private final ThreadLocal<Optional<Object>> relThreadLocal = new ThreadLocal<>(); //todo 有bug需要优化

    protected abstract Object execute(Target target, Object... objects) throws Exception;

    /**
     * 有值时不覆盖，所以默认只收集一个返回
     * 使用场景 需要有一个返回目标，同时可能需要执行多个目标的情况
     * 使用时把需要返回的目标放在队头 bindToHead 其他都往队尾插入
     * @param target
     * @param objects
     * @return
     * @throws Exception
     */
    @Override
    boolean run(Target target, Object... objects) throws Exception {
        Object rel = execute(target, objects);
        if (relThreadLocal.get()==null) {
            if (rel == null) {
                relThreadLocal.set(Optional.empty());
            } else {
                relThreadLocal.set(Optional.of(rel));
            }
        }
        return true;
    }

    public Object call(Object... objects){
        try {
            broadcast(objects);
            Optional<Object> rel = relThreadLocal.get();
            Object relValue = null;
            if (rel.isPresent()){
                if (!Optional.empty().equals(rel)){
                    relValue = rel.get();
                }
                relThreadLocal.remove();
            }
            return relValue;
        }catch (Exception e){
            log.error("call objects {} e",objects,e);
            throw HomoError.throwError(HomoError.broadcastError,e);
        }
    }
}
