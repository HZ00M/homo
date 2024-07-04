package com.homo.core.utils.origin;

import com.homo.core.utils.origin.okhttp.OkHttpClientConfig;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import okio.BufferedSink;
import okio.GzipSink;
import okio.Okio;

import javax.net.ssl.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * Created by galfordliu on 2016/11/19.
 */
@Slf4j
public class OkHttpUtil {

    public static OkHttpClient  getOkHttpClient(OkHttpClient.Builder builder, OkHttpClientConfig okHttpConfig){
        configClientBuilder(builder,okHttpConfig);
        OkHttpClient client= builder.build();
        client.dispatcher().setMaxRequests(okHttpConfig.getMaxRequests());
        client.dispatcher().setMaxRequestsPerHost(okHttpConfig.getMaxRequestsPerHost());
        return client;
    }
    public static OkHttpClient  getOkHttpClient(OkHttpClientConfig okHttpConfig){
        return getOkHttpClient(getOkHttpClientBuilder(),okHttpConfig);
    }
    public static OkHttpClient  getOkHttpClient(OkHttpClient.Builder builder){
        return getOkHttpClient(builder,new OkHttpClientConfig());
    }
    public static OkHttpClient  getOkHttpClient(){
        return getOkHttpClient(new OkHttpClientConfig());
    }


    public static OkHttpClient.Builder getOkHttpClientBuilder() {
        OkHttpClient.Builder builder=new OkHttpClient.Builder();
        configClientBuilder(builder,createEmptyX509TrustManager());
        return builder;
    }
    /**
     * 获取HTTPS安全的Client Builder
     * @param certificates   证书的流
     * @return
     */
    public static  OkHttpClient.Builder getOkHttpClientBuilder(InputStream[] certificates)
    {
        OkHttpClient.Builder builder=new OkHttpClient.Builder();
        configClientBuilder(builder,certificates);
        return builder;
    }
    public static OkHttpClient.Builder getOkHttpClientBuilder(X509TrustManager x509TrustManager) {
        OkHttpClient.Builder builder=new OkHttpClient.Builder();
        configClientBuilder(builder,x509TrustManager);
        return builder;
    }


    public static void configClientBuilder(OkHttpClient.Builder builder,OkHttpClientConfig okHttpConfig){
        ConnectionPool connectionPool=new ConnectionPool(okHttpConfig.getMaxIdleConnections(),okHttpConfig.getKeepAliveSeconds(), TimeUnit.SECONDS);
        builder.connectionPool(connectionPool)
                .readTimeout(okHttpConfig.getReadTimeOutSeconds(),TimeUnit.SECONDS)
                .writeTimeout(okHttpConfig.getWriteTimeOutSeconds(),TimeUnit.SECONDS)
                .connectTimeout(okHttpConfig.getConnectTimeoutSeconds(),TimeUnit.SECONDS);
    }


    public static void configClientBuilder(OkHttpClient.Builder builder,X509TrustManager x509TrustManager) {
        try {

            // Install the all-trusting trust manager
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, new TrustManager[]{x509TrustManager}, new SecureRandom());
            // Create an ssl socket factory with our all-trusting manager
            final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();


            builder.sslSocketFactory(sslSocketFactory,x509TrustManager);
            builder.hostnameVerifier(createIgnoreHostnameVerifier());

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    /**
     * 设置HTTPS安全的Client Builder
     * @param certificates   证书的流
     * @return
     */
    public static void configClientBuilder(OkHttpClient.Builder builder ,InputStream... certificates)
    {
        try {

            TrustManagerFactory trustManagerFactory = createTrustManagerFactory(certificates);
            TrustManager[] trustManagers = trustManagerFactory.getTrustManagers();
            X509TrustManager x509TrustManager = (X509TrustManager) trustManagers[0];
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, trustManagers, new SecureRandom());
            builder.sslSocketFactory(sslContext.getSocketFactory(), x509TrustManager);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }


    public static void configClientBuilder(OkHttpClient.Builder builder,SSLSocketFactory sslSocketFactory,HostnameVerifier hostnameVerifier)
    {
        try
        {
            builder.sslSocketFactory(sslSocketFactory,createEmptyX509TrustManager());
            builder.hostnameVerifier(hostnameVerifier);
        } catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }
    public static void configClientBuilder(OkHttpClient.Builder builder, KeyManager[] keyManagers, X509TrustManager[] x509TrustManagers, SecureRandom secureRandom)
    {
        try {
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(keyManagers, x509TrustManagers, secureRandom);
            builder.sslSocketFactory(sslContext.getSocketFactory(),x509TrustManagers[0]);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    /**
     * 创建一个不做处理的X509TrustManager
     * @return
     */
    public static X509TrustManager  createEmptyX509TrustManager(){
        X509TrustManager x509TrustManager= new X509TrustManager() {
            @Override
            public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
            }

            @Override
            public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
            }

            @Override
            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                return new java.security.cert.X509Certificate[]{};
            }
        };
        return x509TrustManager;
    }
    public static HostnameVerifier createIgnoreHostnameVerifier(){
        return new HostnameVerifier() {
            @Override
            public boolean verify(String hostname, SSLSession session) {
                return true;
            }
        };
    }
    public static SSLSocketFactory getSSLSocketFactory(InputStream ...certificates)throws GeneralSecurityException {
        TrustManagerFactory trustManagerFactory=createTrustManagerFactory(certificates);
        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null,trustManagerFactory.getTrustManagers(),  new SecureRandom());
        return sslContext.getSocketFactory();
    }
    /**
     * 以流的方式添加信任证书
     */
    /**
     * Returns a trust manager that trusts {@code certificates} and none other. HTTPS services whose
     * certificates have not been signed by these certificates will fail with a {@code
     * SSLHandshakeException}.
     * <p>
     * <p>This can be used to replace the host platform's built-in trusted certificates with a custom
     * set. This is useful in development where certificate authority-trusted certificates aren't
     * available. Or in production, to avoid reliance on third-party certificate authorities.
     * <p>
     * <p>
     * <h3>Warning: Customizing Trusted Certificates is Dangerous!</h3>
     * <p>
     * <p>Relying on your own trusted certificates limits your server team's ability to update their
     * TLS certificates. By installing a specific set of trusted certificates, you take on additional
     * operational complexity and limit your ability to migrate between certificate authorities. Do
     * not use custom trusted certificates in production without the blessing of your server's TLS
     * administrator.
     */
    public static TrustManagerFactory createTrustManagerFactory(InputStream ...certificates) throws GeneralSecurityException {
        CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
        List<Certificate> certificateList = new ArrayList<Certificate>(certificates.length);
        for(InputStream in:certificates){
            Certificate certificate=certificateFactory.generateCertificate(in);
            if(certificate!=null){
                certificateList.add(certificate);
            }
        }
        if (certificateList.isEmpty()) {
            throw new IllegalArgumentException("expected non-empty set of trusted certificates");
        }

        // Put the certificates a key store.
        char[] password = "password".toCharArray(); // Any password will work.
        KeyStore keyStore = newEmptyKeyStore(password);
        int index = 0;
        for (Certificate certificate : certificateList) {
            String certificateAlias = Integer.toString(index++);
            keyStore.setCertificateEntry(certificateAlias, certificate);
        }

        // Use it to build an X509 trust manager.
        KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(
                KeyManagerFactory.getDefaultAlgorithm());
        keyManagerFactory.init(keyStore, password);
        TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(
                TrustManagerFactory.getDefaultAlgorithm());
        trustManagerFactory.init(keyStore);
        return trustManagerFactory;
    }
    /**
     * 添加password
     * @param password
     * @return
     * @throws GeneralSecurityException
     */
    public static KeyStore newEmptyKeyStore(char[] password) throws GeneralSecurityException {
        try {
            KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType()); // 这里添加自定义的密码，默认
            InputStream in = null; // By convention, 'null' creates an empty key store.
            keyStore.load(in, password);
            return keyStore;
        } catch (IOException e) {
            throw new AssertionError(e);
        }
    }
    //=====================================================单例方法调用

    /**
     * 请求体构造
     * @param url
     * @param header
     * @param query
     * @return
     */
    public static Request createGetRequest(final String url, final Map<String, String> header,final Map<String, String> query){

        // 创建一个请求 Builder
        Request.Builder builder = new Request.Builder();
        // 装载请求头参数
        if(CollectionUtil.isNotEmpty(header)) {
            Headers headers = Headers.of(header);
            builder.headers(headers);
        }
        // 创建一个 HttpUrl.Builder
        HttpUrl.Builder urlBuilder = HttpUrl.get(url).newBuilder();
        if(CollectionUtil.isNotEmpty(query)) {
            // 装载请求的参数
            Iterator<Map.Entry<String, String>> queryIterator = query.entrySet().iterator();
            queryIterator.forEachRemaining(e -> {
                urlBuilder.addQueryParameter(e.getKey(), e.getValue());
            });
        }
        // 设置自定义的 builder
        // 因为 get 请求的参数，是在 URL 后面追加  http://xxxx:8080/user?name=xxxx?sex=1
        builder.url(urlBuilder.build());

        // 创建一个 request
        return builder.build();
    }
    public static Request createGetRequest(final String url, final Map<String, String> query){
        return createGetRequest(url,null,query);
    }
    public static Request createGetRequest(final String url){
        return createGetRequest(url,null,null);
    }



    /**
     * post提交参数，mediaType为 application/x-www-form-urlencoded
     * @param url
     * @param header
     * @param parameter
     * @return
     */
    public static Request createPostRequest(final String url, final Map<String, String> header, final Map<String, String> parameter) {
        // 创建一个请求 Builder
        Request.Builder builder = new Request.Builder();
        // 装载请求头参数
        if (CollectionUtil.isNotEmpty(header)) {
            Headers headers = Headers.of(header);
            builder.headers(headers);
        }
        // application/octet-stream 等
        FormBody.Builder formBodyBuilder=new FormBody.Builder(StandardCharsets.UTF_8);
        if(CollectionUtil.isNotEmpty(parameter)){
            Iterator<Map.Entry<String, String>> queryIterator = parameter.entrySet().iterator();
            queryIterator.forEachRemaining(e -> {
                formBodyBuilder.add(e.getKey(), e.getValue());
            });
        }
        RequestBody requestBody = formBodyBuilder.build();
        // 设置自定义的 builder
        builder.url(url).post(requestBody);

        return builder.build();
    }
    public static Request createPostRequest(final String url, final Map<String, String> parameter){
        return createPostRequest(url,null,parameter);
    }
    /**
     * 创建提交FormBody为字节数组的Post提交，mediaType可能为application/octet-stream或application/json等等
     * @param url
     * @param header
     * @param body
     * @param mediaType
     * @return
     */
    public static Request createPostRequest(final String url, final Map<String, String> header, final byte[] body,final MediaType mediaType) {
        // 创建一个请求 Builder
        Request.Builder builder = new Request.Builder();
        // 装载请求头参数
        if (CollectionUtil.isNotEmpty(header)) {
            Headers headers = Headers.of(header);
            builder.headers(headers);
        }
        // application/octet-stream 等
        RequestBody requestBody = FormBody.create(body, mediaType);
        // 设置自定义的 builder
        builder.url(url).post(requestBody);
        return builder.build();
    }

    /**
     * 创建一个Post，上传JSON
     * mediaType为application/json
     * @param url
     * @param header
     * @param json
     * @return
     */
    public static Request createJsonRequest(final String url, final Map<String, String> header, final String json) {
        return createPostRequest(url,header,json.getBytes(StandardCharsets.UTF_8),MEDIA_TYPE_JSON);
    }

    /**
     * 创建一个Post，上传JSON并开启Gzip压缩
     * mediaType为application/octet-stream
     * @param url
     * @param header
     * @param json
     * @return
     * @throws IOException
     */
    public static Request createGzipJsonRequest(final String url, final Map<String, String> header, final String json)  throws IOException {
        Map<String, String> realHeader=new HashMap<>();
        if (CollectionUtil.isNotEmpty(header)) {
            realHeader.putAll(header);
        }
        realHeader.put("Content-Encoding", "gzip");
        byte[] gzipBytes=gzipCompress(json);
        return createPostRequest(url,realHeader,gzipBytes,MEDIA_TYPE_OCTET);
    }



    /**
     * 创建一个Post请求，不但包含参数对，也包含上传的文件体。
     * mediaType为multipart/form-data
     * @param url
     * @param header
     * @param parameter
     * @param file
     * @param fileFormName
     * @return
     */
    public static Request createFormRequest(final String url, final Map<String, String> header, final Map<String, String> parameter,final File file, final String fileFormName)   {

        // 创建一个请求 Builder
        Request.Builder builder = new Request.Builder();
        // 装载请求头参数
        if(CollectionUtil.isNotEmpty(header)) {
            Headers headers = Headers.of(header);
            builder.headers(headers);
        }

        // 或者 FormBody.create 方式，只适用于接口只接收文件流的情况
        // RequestBody requestBody = FormBody.create(MediaType.parse("application/octet-stream"), file);
        MultipartBody.Builder requestBuilder = new MultipartBody.Builder();
        requestBuilder.setType(MultipartBody.FORM); //上传文件并带参数需要指定为此类型
        if(CollectionUtil.isNotEmpty(parameter)) {
            // 状态请求参数
            Iterator<Map.Entry<String, String>> queryIterator = parameter.entrySet().iterator();
            queryIterator.forEachRemaining(e -> {
                requestBuilder.addFormDataPart(e.getKey(), e.getValue());
            });
        }

        if (null != file) {
            // application/octet-stream
            RequestBody requestBody=RequestBody.create(file,MEDIA_TYPE_OCTET);
            requestBuilder.addFormDataPart(StringUtil.isNotBlank(fileFormName) ? fileFormName : "file", file.getName(), requestBody);
        }

        // 设置自定义的 builder
        builder.url(url).post(requestBuilder.build());

        // 创建一个 request
        return builder.build();
    }


    public static Request createFormRequest(final String url, final Map<String, String> header, final Map<String, String> parameter,final String fileName, final byte [] fileByte, final String fileFormName)   {

        // 创建一个请求 Builder
        Request.Builder builder = new Request.Builder();
        // 装载请求头参数
        if(CollectionUtil.isNotEmpty(header)) {
            Headers headers = Headers.of(header);
            builder.headers(headers);
        }

        MultipartBody.Builder requestBuilder = new MultipartBody.Builder();
        requestBuilder.setType(MultipartBody.FORM); //上传文件并带参数需要指定为此类型
        if(CollectionUtil.isNotEmpty(parameter)) {
            // 状态请求参数
            Iterator<Map.Entry<String, String>> queryIterator = parameter.entrySet().iterator();
            queryIterator.forEachRemaining(e -> {
                requestBuilder.addFormDataPart(e.getKey(), e.getValue());
            });
        }
        if (ArrayUtil.isNotEmpty(fileByte)) {
            // application/octet-stream
            RequestBody requestBody=RequestBody.create(fileByte,MEDIA_TYPE_OCTET);
            requestBuilder.addFormDataPart(StringUtil.isNotBlank(fileFormName) ? fileFormName : "file", fileName, requestBody);
        }

        // 设置自定义的 builder
        builder.url(url).post(requestBuilder.build());

        // 创建一个 request
        return builder.build();
    }
    public static Request createFormRequest(final String url, final Map<String, String> parameter){
        return createFormRequest(url,null,parameter,null,null);
    }
    public static Request createFormRequest(final String url, final Map<String, String> header,final Map<String, String> parameter){
        return createFormRequest(url,header,parameter,null,null);
    }

    public static Request createFormRequest(final String url, final Map<String, String> parameter, final File file, final String fileFormName)   {
        return createFormRequest(url,null,parameter,file,fileFormName);
    }
    public static Request createFormRequest(final String url, final File file, final String fileFormName)   {
        return createFormRequest(url,null,null,file,fileFormName);
    }
    public static Request createFormRequest(final String url, final Map<String, String> parameter,final String fileName, final byte [] fileByte, final String fileFormName)   {
        return createFormRequest(url,null,parameter,fileName,fileByte,fileFormName);
    }
    public static Request createFormRequest(final String url,  final String fileName, final byte [] fileByte, final String fileFormName)   {
        return createFormRequest(url,null,null,fileName,fileByte,fileFormName);
    }

    public static Request createJsonRequest(final String url, final String json) {
        return createJsonRequest(url, null, json);
    }
    public static Request createGzipJsonRequest(final String url, final String json) throws IOException {
        return createGzipJsonRequest(url, null, json);
    }


    /**
     * 自定义网络回调接口
     */
    public interface HttpCallback{
        void success(Call call, Response response) throws IOException;
        void failed(Call call, IOException e);
    }
    /**
     * 同步方式执行请求
     * @param okHttpClient
     * @param request
     * @return
     */
    public static Response httpDataSync(final OkHttpClient okHttpClient,final Request request) throws IOException {
        Assert.notNull(okHttpClient,"okHttpClient it must not be null");
        Assert.notNull(request,"request it must not be null");
        //  将Request封装为Call
        Call call = okHttpClient.newCall(request);
        // 执行Call，得到response
        return call.execute();
    }
    public static String httpStrSync(final OkHttpClient okHttpClient,final Request request) throws IOException {
        try(Response response=httpDataSync(okHttpClient,request)){
            if(response.body()!=null) {
                return response.body().string();
            }else{
                return null;
            }
        }
    }
    /**
     * 异步方式执行请求，获取网络数据，是在子线程中执行的，需要切换到主线程才能更新UI
     * @param callback
     * @return
     */
    public static Call httpDataAsync(final OkHttpClient okHttpClient,final Request request, final HttpCallback callback) {
        Assert.notNull(okHttpClient,"okHttpClient it must not be null");
        Assert.notNull(request,"request it must not be null");
        Assert.notNull(callback,"callback it must not be null");
        //  将Request封装为Call
        Call call = okHttpClient.newCall(request);
        //3 执行Call
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.failed(call, e);
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                callback.success(call, response);
            }
        });
        return call;
    }
    public static RequestBody gzip(final RequestBody body) {
        return new RequestBody() {
            @Override
            public MediaType contentType() {
                return body.contentType();
            }

            @Override
            public long contentLength() {
                return -1; // We don&#39;t know the compressed length in advance!
            }

            @Override
            public void writeTo(BufferedSink sink) throws IOException {
                BufferedSink gzipSink = Okio.buffer(new GzipSink(sink));
                body.writeTo(gzipSink);
                gzipSink.close();
            }
        };
    }
    public static RequestBody getGzipRequest(String body) {
        RequestBody request = null;
        try {
            request = RequestBody.create(gzipCompress(body), MEDIA_TYPE_OCTET);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return request;
    }
    public static byte[] gzipCompress(String str) throws IOException {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            try (GZIPOutputStream gzip = new GZIPOutputStream(out)) {
                gzip.write(str.getBytes(StandardCharsets.UTF_8));
            }
            return out.toByteArray();
            //return out.toString(StandardCharsets.ISO_8859_1);
            // Some single byte encoding
        }
    }

    public static String gzipUnCompress(byte[] str) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (GZIPInputStream gis = new GZIPInputStream(new ByteArrayInputStream(str))) {
            int b;
            while ((b = gis.read()) != -1) {
                baos.write((byte) b);
            }
        }
        return new String(baos.toByteArray(), StandardCharsets.UTF_8);
    }

    public final static MediaType  MEDIA_TYPE_OCTET=MediaType.parse("application/octet-stream");
    public final static MediaType  MEDIA_TYPE_JSON=MediaType.parse("application/json");


}
