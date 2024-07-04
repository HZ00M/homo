package com.homo.core.utils.origin.okhttp;

import com.homo.core.utils.origin.OkHttpUtil;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;

/**
 * @author liujun
 * @create 2020/12/19 17:36
 */
public class GzipRequestInterceptor implements Interceptor {
    @Override
    public Response intercept(Chain chain) throws IOException {
        Request originalRequest = chain.request();
        if (originalRequest.body() == null || originalRequest.header("Content-Encoding") != null) {
            return chain.proceed(originalRequest);
        }

        Request compressedRequest = originalRequest.newBuilder()
                .header("Content-Encoding", "gzip")
                .method(originalRequest.method(), OkHttpUtil.gzip(originalRequest.body()))
                .build();
        return chain.proceed(compressedRequest);
    }
}
