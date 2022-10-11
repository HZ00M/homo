package com.homo.core.utils.serial;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.nustaq.serialization.FSTClazzNameRegistry;
import org.nustaq.serialization.FSTConfiguration;
import org.nustaq.serialization.FSTDecoder;
import org.nustaq.serialization.FSTEncoder;
import org.nustaq.serialization.coders.FSTStreamDecoder;
import org.nustaq.serialization.coders.FSTStreamEncoder;

import java.lang.reflect.Field;

public class FSTSerializationProcessor implements HomoSerializationProcessor{
    private FSTConfiguration fst;

    public FSTSerializationProcessor(){
        fst = FSTConfiguration.createAndroidDefaultConfiguration();
    }

    public void registerClass(Class<?>... classes){
        fst.registerClass(classes);
    }

    @Override
    public byte[] writeByte(Object obj) {
        return fst.asByteArray(obj);
    }

    @Override
    public String writeString(Object obj) {
        return fst.asJsonString(obj);
    }

    @Override
    public <T> T readValue(byte[] obj, Class<T> clazz) {
        return (T) fst.asObject(obj);
    }

    @Override
    public <T> T readValue(byte[] obj, HomoTypeReference<T> reference) {
        return (T) fst.asObject(obj);
    }

    @Override
    public <T> T readValue(String obj, Class<T> clazz) {
        throw new RuntimeException("Not support!");
    }

    @Override
    public <T> T readValue(String obj, HomoTypeReference<T> reference) {
        throw new RuntimeException("Not support!");
    }

    static ThreadLocal input = new ThreadLocal();
    static ThreadLocal output = new ThreadLocal();

    private class HomoStreamCoderFactory implements FSTConfiguration.StreamCoderFactory{
        protected final FSTConfiguration conf;

        public HomoStreamCoderFactory(FSTConfiguration conf) {
            this.conf = conf;
        }

        @SneakyThrows
        @Override
        public FSTEncoder createStreamEncoder() {
            return new HomoStreamEncoder(conf);
        }

        @Override
        public FSTDecoder createStreamDecoder() {
            return new FSTStreamDecoder(conf);
        }

        @Override
        public ThreadLocal getInput() {
            return input;
        }

        @Override
        public ThreadLocal getOutput() {
            return output;
        }
    }

    @Slf4j
    private static class  HomoStreamEncoder extends FSTStreamEncoder{
        //读取父类clnames属性 ，提供其修改能力
        private Field clnames;
        public HomoStreamEncoder(FSTConfiguration conf) throws NoSuchFieldException {
            super(conf);
            clnames = FSTStreamEncoder.class.getDeclaredField("clnames");
            clnames.setAccessible(true);
        }

        @Override
        public void setConf(FSTConfiguration conf) {
            super.setConf(conf);
            FSTClazzNameRegistry nameRegistry = (FSTClazzNameRegistry)conf.getCachedObject(FSTClazzNameRegistry.class);
            if (nameRegistry == null){
                nameRegistry = new FSTClazzNameRegistry(conf.getClassRegistry());
            }else {
                nameRegistry.clear();
            }
            try {
                this.clnames.set(this,nameRegistry);//修改父类的clnames(FSTClazzNameRegistry)属性
                log.info("HomoStreamCoderFactory replace nameRegistry success");
            } catch (IllegalAccessException e) {
                log.error("HomoStreamCoderFactory replace nameRegistry error");
            }
        }
    }


}
