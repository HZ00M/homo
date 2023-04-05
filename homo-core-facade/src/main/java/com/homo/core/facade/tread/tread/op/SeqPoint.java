package com.homo.core.facade.tread.tread.op;

import com.homo.core.facade.tread.tread.enums.SeqType;
import lombok.Data;

import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Supplier;

@Data
public class SeqPoint<T> {
    public Object object;
    public String methodName;
    public T param;
    public SeqType type;
    public BiPredicate<T, T> checkPredicate;
    public Consumer<T> resultConsumer;
    public Supplier<Object> targetSupplier;

    public SeqPoint(Object object, String methodName, T param, SeqType type, BiPredicate<T, T> checkPredicate, Consumer<T> resultConsumer) {
        this(object, methodName, param, type, checkPredicate, resultConsumer, null);
    }

    public SeqPoint(Object object, String methodName, T param, SeqType type, BiPredicate<T, T> checkPredicate, Consumer<T> resultConsumer, Supplier<Object> targetSupplier) {
        this.object = object;
        this.methodName = methodName;
        this.param = param;
        this.type = type;
        this.checkPredicate = checkPredicate;
        this.resultConsumer = resultConsumer;
        this.targetSupplier = targetSupplier;
    }
}
