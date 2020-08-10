package com.example.transitsystem.test;

import okhttp3.*;

import java.io.File;
import java.io.IOException;

public class OKHttpTest {

    private static final MediaType MEDIA_TYPE_PNG = MediaType.parse("image/png/txt/log");


    public static void main(String[] args) {

        OkHttpClient mOkHttpClient = new OkHttpClient();

        MultipartBody.Builder multipartBodyBuilder = new MultipartBody.Builder();
        multipartBodyBuilder.setType(MultipartBody.FORM);
        multipartBodyBuilder.addFormDataPart("sno", "123456789");
        multipartBodyBuilder.addFormDataPart("mac", "AA-BB-CC-DD");
        multipartBodyBuilder.addFormDataPart("name", "yaoge");
        multipartBodyBuilder.addFormDataPart("headPicName", "import.txt");
        //遍历paths中所有图片绝对路径到builder，并约定key如“upload”作为后台接受多张图片的key

        multipartBodyBuilder.addFormDataPart("files", "import.txt", RequestBody.create(MEDIA_TYPE_PNG, new File("/home/yao/test/import.txt")));
        multipartBodyBuilder.addFormDataPart("files", "head_test.log", RequestBody.create(MEDIA_TYPE_PNG, new File("/home/yao/test/head_test.log")));
        //构建请求体
        RequestBody requestBody = multipartBodyBuilder.build();

        Request.Builder RequestBuilder = new Request.Builder();
        RequestBuilder.url(HttpUrl.parse("http://139.199.189.158:8080/file/upload"));// 添加URL地址
        RequestBuilder.post(requestBody);
        Request request = RequestBuilder.build();
        mOkHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                System.out.println("failure");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                System.out.println("success");
            }
        });
    }

}
