package com.homo.core.utils.origin;

import io.protostuff.LinkedBuffer;
import io.protostuff.ProtobufIOUtil;
import io.protostuff.Schema;
import io.protostuff.runtime.RuntimeSchema;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;



/*
 * Copyright 2010-2016 www.oppo.com Inc. All rights reserved.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
public class PBUtil {

    public static <T> T deserialize(InputStream entityStream, Class<T> clz) throws IOException, IllegalAccessException, InstantiationException {
        Schema schema = RuntimeSchema.getSchema(clz);
        T t = clz.newInstance();
        ProtobufIOUtil.mergeFrom(entityStream, t, schema);
        return t;
    }

    public static <T> T deserialize(byte[] bytes, Class<T> clz) throws IOException, IllegalAccessException, InstantiationException {
        return deserialize(new ByteArrayInputStream(bytes), clz);
    }

    public static byte[] serialize(Object obj) {
        if (obj == null) {
            return null;
        }
        LinkedBuffer buffer = LinkedBuffer.allocate(1024);
       /* byte[] byt = new byte[input.available()];
        input.read(byt);*/
        Schema schema;
        if (List.class.isInstance(obj)) {
            schema = RuntimeSchema.getSchema(getListGenericType((List) obj, obj.getClass()));
        } else {
            schema = RuntimeSchema.getSchema(obj.getClass());
        }
        return ProtobufIOUtil.toByteArray(obj, schema, buffer);
    }

    protected static Class getListGenericType(List list, Type genericType) {
        if (genericType instanceof ParameterizedType) {
            return (Class<?>) ((ParameterizedType) genericType).getActualTypeArguments()[0];
        } else if (list != null) {
            if (list.size() == 0) return Object.class;

            for (Object o : list) {
                if (o != null) {
                    return o.getClass();
                }
            }
        }
        return Object.class;
    }

    /*public static <T> List<T> decodeList(byte[] bytes,Class<T> clz) throws IOException {
        return decodeList(new ByteArrayInputStream(bytes),clz);
    }

    public static <T> List<T> decodeList(InputStream entityStream,Class<T> clz) throws IOException {
        Schema schema = RuntimeSchema.getSchema(getListGenericType(null, clz));
        return ProtobufIOUtil.parseListFrom(entityStream, schema);
    }*/

   /* public static <T> byte[] encodeList(List<T> obj){
        if(obj == null || obj.isEmpty()){
            return null;
        }
        LinkedBuffer buffer = LinkedBuffer.allocate(1024);
       *//* byte[] byt = new byte[input.available()];
        input.read(byt);*//*
        Schema schema ;
        if (List.class.isInstance(obj)) {
            schema = RuntimeSchema.getSchema(getListGenericType((List) obj, obj.getClass()));
        } else {
            schema = RuntimeSchema.getSchema(obj.getClass());
        }
        return ProtostuffIOUtil.toByteArray(obj, schema, buffer);
    }*/


}
