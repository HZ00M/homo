package com.homo.core.utils.lang;

import lombok.extern.log4j.Log4j2;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

@Log4j2
public class KKMap<K1,K2,T> {
    Map<K1,Map<K2,T>> data = new ConcurrentHashMap<>();

    public T get(K1 k1,K2 k2){
        AtomicReference<T> rel = new AtomicReference<>(null);
        data.computeIfPresent(k1,(key1,v1)->{
            v1.computeIfPresent(k2,(key2,v2)->{
                rel.set(v2);
                return v2;
            });
            return v1;
        });
        return rel.get();
    }

    public Set<T> getAll(){
        AtomicReference<Set<T>> rel = new AtomicReference<>(null);
        rel.set(new HashSet<>());
        data.forEach((k1,v1)->{
            rel.get().addAll(data.get(k1).values());
        });
        return rel.get();
    }

    public Set<T> getAll(K1 k1){
        AtomicReference<Set<T>> rel = new AtomicReference<>(null);
        data.computeIfPresent(k1,(key1,v1)->{
            rel.set(new HashSet<>(v1.values()));
            return v1;
        });
        return rel.get();
    }

    public T remove(K1 k1,K2 k2){
        AtomicReference<T> rel = new AtomicReference<>(null);
        data.computeIfPresent(k1,(key1,v1)->{
            rel.set(v1.remove(k2));
            return v1;
        });
        return rel.get();
    }

    public void set(K1 k1,K2 k2,T v){
        data.computeIfAbsent(k1,key1->new ConcurrentHashMap<>(16)).put(k2,v);
    }

    public Map<K2, T> getFirstKeyData(K1 k1) {
         return data.get(k1);
    }

    public void removeFirstKey(K1 k1){
        data.remove(k1) ;
    }

    public void putFirstKey(K1 k1,Map<K2,T> map){
        data.computeIfAbsent(k1,key1->new ConcurrentHashMap<>(16)).putAll(map);
    }

    public Set<K1> getK1Set(){
        return data.keySet();
    }

    public boolean containsFirstKey(K1 k1){
        Map<K2,T> keyData =  data.get(k1);
        return keyData != null && keyData.size() > 0 ;
    }

    public  void removeAll(){
        data.clear();
    }
}
