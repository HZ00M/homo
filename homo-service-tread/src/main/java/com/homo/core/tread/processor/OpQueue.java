package com.homo.core.tread.processor;

import com.homo.core.facade.tread.processor.OpPoint;
import com.homo.core.utils.rector.Homo;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

/**
 * 操作队列
 */
@Slf4j
public abstract class OpQueue implements OpPoint {
    private List<OpPoint> opList;

    public OpQueue() {
        this(new ArrayList<>());
    }

    public OpQueue(List<OpPoint> opList) {
        this.opList = opList;
    }

    OpPoint getOp(int index){
        return opList.get(index);
    }

    protected void add(OpPoint opPoint){
        opList.add(opPoint);
    }

    private Homo<Boolean> exec(Iterator<OpPoint> it) throws RuntimeException {
        // 取下一个操作
        if (it.hasNext()){
            OpPoint op = it.next();
            // 取到了就执行
            return op.exec().nextDo(rel->{
                if (rel){
                    // 当前执行成功就执行下一个操作
                    return exec(it);
                } else
                    // 当前操作执行失败直接返回
                    return Homo.result(false);
            });
        }
        // 所有操作执行完成，返回成功
        return Homo.result(true);
    }

    /**
     * 执行一个队列
     * @return 返回执行结果
     * @throws Exception 执行异常
     */
    @Override
    public Homo<Boolean> exec() throws RuntimeException {
        opList.sort(Comparator.comparingInt(OpPoint::getOrder));
        Iterator<OpPoint> iterator = opList.iterator();
        return exec(iterator);
    }

}
