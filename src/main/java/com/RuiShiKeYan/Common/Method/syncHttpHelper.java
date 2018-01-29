package com.RuiShiKeYan.Common.Method;

/**
 * Created with IntelliJ IDEA
 * User:huangming
 * Date:2017/12/20
 * Time:上午10:38
 */

import okhttp3.*;
import okhttp3.Request.Builder;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class syncHttpHelper {
    private static OkHttpClient client = null;
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    public syncHttpHelper() {
    }

    private static OkHttpClient getOkClient() {
        if (client == null) {
            client = new OkHttpClient();
            client = client.newBuilder().connectTimeout(1L, TimeUnit.MINUTES).readTimeout(20L, TimeUnit.MINUTES).build();
        }

        return client;
    }

    public static String syncPost(String url, String json) throws IOException {
        RequestBody body = RequestBody.create(JSON, json);
        Request request = (new Builder()).url(url).post(body).build();
        Response response = getOkClient().newCall(request).execute();
        return response.body().string();
    }

    public static void aSynPost(String url, String json) throws IOException {
        RequestBody body = RequestBody.create(JSON, json);
        Request request = (new Builder()).url(url).post(body).build();
        getOkClient().newCall(request).enqueue(new Callback() {
            public void onResponse(Call call, Response response) throws IOException {
                System.out.println("" + response);
                response.body().close();
            }

            public void onFailure(Call call, IOException e) {
                System.err.println("aSynPost error" + e.toString());
            }
        });
    }
}
