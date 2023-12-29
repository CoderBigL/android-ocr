package com.nmw.ocrcamera;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Looper;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static com.nmw.ocrcamera.constant.OcrConstants.APP_KEY;
import static com.nmw.ocrcamera.constant.OcrConstants.APP_SECRET;
import static com.nmw.ocrcamera.constant.OcrConstants.HEADER_KEY_ACCESS_TOKEN;
import static com.nmw.ocrcamera.constant.OcrConstants.INTENT_ACCESS_TOKEN;
import static com.nmw.ocrcamera.constant.OcrConstants.INTENT_KEY_IMG_PATH;
import static com.nmw.ocrcamera.constant.OcrConstants.INTENT_KEY_IMG_URI;
import static com.nmw.ocrcamera.constant.OcrConstants.INTENT_KEY_REQUEST_CODE;
import static com.nmw.ocrcamera.constant.OcrConstants.MEDIA_TYPE_APPLICATION;
import static com.nmw.ocrcamera.constant.OcrConstants.REQUEST_CAMERA_OCR_ID_CARD;
import static com.nmw.ocrcamera.constant.OcrConstants.REQUEST_CAMERA_OCR_LICENSE_PLATE;
import static com.nmw.ocrcamera.constant.OcrConstants.REQUEST_CAMERA_OCR_TEXT;
import static com.nmw.ocrcamera.constant.OcrConstants.URL_ACCESS_TOKEN;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "OCR-MainActivity";

    private String accessToken;

    /**
     * 拍照的照片保存路径
     */
    private String currentPhotoPath;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        requestAccessToken();
    }

    @Override
    public void onClick(View view) {

        //文本识别
        if (view.getId() == R.id.btn_ocr_text) {
//            startTakePhoto(REQUEST_CAMERA_OCR_TEXT);
            startPickImage(REQUEST_CAMERA_OCR_TEXT);
            return;
        }

        //车牌识别
        if (view.getId() == R.id.btn_ocr_license_plate) {
            startTakePhoto(REQUEST_CAMERA_OCR_LICENSE_PLATE);
            return;
        }

        //身份证识别
        if (view.getId() == R.id.btn_ocr_id_card) {
            startTakePhoto(REQUEST_CAMERA_OCR_ID_CARD);
            return;
        }

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode != RESULT_OK) {
            return;
        }

        //跳转到扫描识别页面
        Intent intent = new Intent(this, OcrResultActivity.class);
        intent.putExtra(INTENT_KEY_REQUEST_CODE, requestCode);
        if (REQUEST_CAMERA_OCR_TEXT == requestCode) {
            intent.setData(data.getData());
        }else {
            intent.putExtra(INTENT_KEY_IMG_PATH, currentPhotoPath);
        }
        intent.putExtra(INTENT_ACCESS_TOKEN, accessToken);
        startActivity(intent);

    }


    private void startPickImage(int requestCode) {
        Intent intent = new Intent(
                Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        );
        intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI , "image/*");
        startActivityForResult(intent, requestCode);

//        File photoFile = null;
//        try {
//            photoFile = createImageFile();
//        } catch (IOException ex) {
//            ex.printStackTrace();
//        }
//        // 创建文件成功后，跳转到拍照界面
//        if (photoFile != null) {
//            Uri photoURI = FileProvider.getUriForFile(this,
//                    "com.nmw.ocrcamera.fileprovider",
//                    photoFile);
//            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
//            startActivityForResult(takePictureIntent, requestCode);
//        }
    }

    private void startTakePhoto(int requestCode) {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File photoFile = null;
        try {
            photoFile = createImageFile();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        // 创建文件成功后，跳转到拍照界面
        if (photoFile != null) {
            Uri photoURI = FileProvider.getUriForFile(this,
                    "com.nmw.ocrcamera.fileprovider",
                    photoFile);
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
            startActivityForResult(takePictureIntent, requestCode);
        }
    }


    private File createImageFile() throws IOException {
        //创建图片文件
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* 名称 */
                ".jpg",         /* 后缀 */
                storageDir      /* 目录 */
        );
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }


    /**
     * 请求access_token
     */
    private void requestAccessToken() {
        //拼接请求参数
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("appKey", APP_KEY);
            jsonObject.put("appSecret", APP_SECRET);
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }

        //设置media类型为application/json; charset=utf-8
        MediaType mediaType = MediaType.parse(MEDIA_TYPE_APPLICATION);
        OkHttpClient okHttpClient = new OkHttpClient.Builder().connectTimeout(10, TimeUnit.SECONDS).build();
        Request request = new Request.Builder()
                .post(RequestBody.create(jsonObject.toString(), mediaType))
                .url(URL_ACCESS_TOKEN)
                .build();

        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Log.e(TAG, "request onFailure URL:" + URL_ACCESS_TOKEN, e);
                showToast("网络请求异常");
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                String responseString = response.body().string();
                System.out.println("接口返回结果:" + responseString);
                try {
                    JSONObject resultJsonObject = new JSONObject(responseString);
                    JSONObject dataJsonObject = resultJsonObject.getJSONObject("data");
                    accessToken = dataJsonObject.getString("accessToken");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void showToast(String content) {
        if (Looper.myLooper() == null) {
            Looper.prepare();
        }
        Toast.makeText(this, content, Toast.LENGTH_SHORT).show();
        Looper.loop();
    }

}
