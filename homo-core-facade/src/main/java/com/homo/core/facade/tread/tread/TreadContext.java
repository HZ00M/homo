package com.homo.core.facade.tread.tread;

import com.homo.core.facade.tread.tread.op.SeqPoint;
import com.homo.core.utils.fun.FuncWithException;
import lombok.Data;
import lombok.ToString;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

@Data
@ToString
public class TreadContext<T> {
    public static String leftQuote = "(";
    public static String splitQuote = ":";
    public static String rightQuote = ")";
    public static String nextQuote = "->";
    public String ownerId;
    public List<SeqPoint<T>> subs;
    public List<SeqPoint<T>> adds;
    public Iterator<SeqPoint<T>> subIterator;
    public Iterator<SeqPoint<T>> addIterator;
    public SeqPoint<T> processingSeqPoint;
    public StringBuilder recordBuilder;
    public Map<Object, FuncWithException<Object, Object>> createObjFunMap;
    public Map<Object, Object> mgrObjMap;

    public String getOwnerId() {
        return ownerId;
    }

    public TreadContext(String ownerId, List<SeqPoint<T>> subs, List<SeqPoint<T>> adds,
                        Map<Object, FuncWithException<Object, Object>> createObjFunMap,
                        Map<Object, Object> mgrObjMap) {
        this.ownerId = ownerId;
        this.subs = subs;
        this.adds = adds;
        this.subIterator = subs.iterator();
        this.addIterator = adds.iterator();
        this.createObjFunMap = createObjFunMap;
        this.mgrObjMap = mgrObjMap;
    }


    public void setProcessing(SeqPoint<T> seqPoint) {
        processingSeqPoint = seqPoint;
    }


    public SeqPoint<T> getProcessing() {
        return processingSeqPoint;
    }

    public List<SeqPoint<T>> getSubs() {
        return subs;
    }

    public List<SeqPoint<T>> getAdds() {
        return adds;
    }

    public FuncWithException<Object, Object> getCreateObjFun(Object identity) {
        return createObjFunMap.get(identity);
    }

    public boolean containCreateObjFun(Object identity) {
        return createObjFunMap != null && createObjFunMap.containsKey(identity);
    }


    public void record(Object... recordInfo) {
        if (recordBuilder == null) {
            recordBuilder = new StringBuilder();
        }
        recordBuilder.append(nextQuote);
        recordBuilder.append(leftQuote);
        for (Object o : recordInfo) {
            recordBuilder.append(o).append(splitQuote);
        }
        recordBuilder.append(rightQuote);
    }


    public String getRecords() {
        if (this.recordBuilder == null) {
            return null;
        }
        return recordBuilder.toString();
    }
}
