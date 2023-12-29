package com.nmw.ocrcamera;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static com.nmw.ocrcamera.constant.OcrConstants.HEADER_KEY_ACCESS_TOKEN;
import static com.nmw.ocrcamera.constant.OcrConstants.INTENT_ACCESS_TOKEN;
import static com.nmw.ocrcamera.constant.OcrConstants.INTENT_KEY_IMG_PATH;
import static com.nmw.ocrcamera.constant.OcrConstants.INTENT_KEY_REQUEST_CODE;
import static com.nmw.ocrcamera.constant.OcrConstants.MEDIA_TYPE_APPLICATION;
import static com.nmw.ocrcamera.constant.OcrConstants.REQUEST_API_SUCCESS_CODE;
import static com.nmw.ocrcamera.constant.OcrConstants.REQUEST_CAMERA_OCR_ID_CARD;
import static com.nmw.ocrcamera.constant.OcrConstants.REQUEST_CAMERA_OCR_LICENSE_PLATE;
import static com.nmw.ocrcamera.constant.OcrConstants.REQUEST_CAMERA_OCR_TEXT;
import static com.nmw.ocrcamera.constant.OcrConstants.URL_OCR_ID_CARD;
import static com.nmw.ocrcamera.constant.OcrConstants.URL_OCR_LICENSE_PLATE;
import static com.nmw.ocrcamera.constant.OcrConstants.URL_OCR_TEXT;

/**
 * @author ljq
 * @description 扫描结果识别页面
 * @date 2023-09
 */
public class OcrResultActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "OCR-OcrResultActivity";
    /**
     * 拍照后显示照片的ImageView
     */
    private ImageView ivShowPicture;

    /**
     * 显示ocr识别结果返回的文本
     */
    private TextView tvOcrResult;

    /**
     * 扫描动画ImageView
     */
    private ImageView ivScan;

    /**
     * 剪贴板管理器
     */
    private ClipboardManager cm;

    /**
     * 扫描动画
     */
    private Animation scanAnimation;

    private Context context;

    /**
     * 从相册选取的图片
     */
    private Uri pickImgUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ocr_result);
        context = this;

        ivShowPicture = findViewById(R.id.iv_show_picture);
        tvOcrResult = findViewById(R.id.tv_ocr_result);

        //扫描动画播放
        scanAnimation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.anim_scan_up_down);
        ivScan = findViewById(R.id.iv_scan);
        ivScan.setVisibility(View.VISIBLE);
        ivScan.startAnimation(scanAnimation);

        //剪贴板管理器
        cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);

        //获取参数，进行ocr识别
        Intent intent = getIntent();
        int requestCode = intent.getIntExtra(INTENT_KEY_REQUEST_CODE, REQUEST_CAMERA_OCR_TEXT);
        String imgPath = intent.getStringExtra(INTENT_KEY_IMG_PATH);
        String accessToken = intent.getStringExtra(INTENT_ACCESS_TOKEN);
        if (REQUEST_CAMERA_OCR_TEXT == requestCode) {
            pickImgUri = intent.getData();
        }
        startOcr(requestCode,imgPath,accessToken);

    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_back) {
            onBackPressed();
            return;
        }

        if (v.getId() == R.id.btn_copy) {
            //创建普通字符型ClipData
            ClipData mClipData = ClipData.newPlainText("ocr-content", tvOcrResult.getText());
            //将ClipData内容放到系统剪贴板里。
            cm.setPrimaryClip(mClipData);
            showToast("复制到剪贴板成功");
        }
    }


    /**
     * 压缩图片，并且请求ocr识别接口
     * @param requestCode
     * @param currentPhotoPath
     * @param accessToken
     */
    private void startOcr(int requestCode, String currentPhotoPath, String accessToken) {

        byte[] imgBytes;
        //通过文件的方式获取拍摄的图片，这种方式获取的图片质量较高
        if (requestCode == REQUEST_CAMERA_OCR_TEXT) {
            imgBytes = getCompressImgBytesByUri(pickImgUri);
        }else {
            imgBytes = getAndCompressImg(requestCode == REQUEST_CAMERA_OCR_LICENSE_PLATE ? 5 : 15, currentPhotoPath);
        }

        if (imgBytes == null) {
            logInfo(TAG, "获取和压缩图片 %s 获取的字节数为null", currentPhotoPath);
            return;
        }

        ByteArrayInputStream bis = new ByteArrayInputStream(imgBytes);
        Bitmap bitmap = BitmapFactory.decodeStream(bis, null, null);
        ivShowPicture.setImageBitmap(bitmap);

        //转换成base64编码请求接口
        String base64String = Base64.encodeToString(imgBytes, Base64.NO_WRAP);
        logInfo(TAG, "转成base64后-------->%s", base64String);


        //请求ocr接口
        if (requestCode == REQUEST_CAMERA_OCR_TEXT) {
            ocrHttpPostRequest(URL_OCR_TEXT, base64String, accessToken);
            return;
        }

        if (requestCode == REQUEST_CAMERA_OCR_LICENSE_PLATE) {
            ocrHttpPostRequest(URL_OCR_LICENSE_PLATE, base64String, accessToken);
            return;
        }

        if (requestCode == REQUEST_CAMERA_OCR_ID_CARD) {
            ocrHttpPostRequest(URL_OCR_ID_CARD, base64String, accessToken);
            return;
        }
    }


    private void stopScanAnimation() {
        ivScan.setVisibility(View.GONE);
        ivScan.clearAnimation();
    }

    public void showToast(final String content) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(context, content, Toast.LENGTH_SHORT).show();
            }
        });
    }


    private byte[] getCompressImgBytesByUri(Uri uri) {
        try {
            Bitmap bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(uri));

            //压缩图片
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 30, outputStream);
            byte[] compressBytes = outputStream.toByteArray();
            logInfo(TAG, "识别文本压缩后图片大小-------->%s", compressBytes.length / 1024);
            return compressBytes;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 通过文件的方式获取拍摄的原始图片，并进行图片压缩
     *
     * @param compressQuality 压缩质量
     * @return
     */
    private byte[] getAndCompressImg(int compressQuality, String currentPhotoPath) {
        try {
            File imgFile = new File(currentPhotoPath);
            Bitmap mbtp = BitmapFactory.decodeStream(getContentResolver().openInputStream(Uri.fromFile(imgFile)));

            //图片压缩前获取图片的大小
            /*ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            mbtp.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);

            byte[] compressBytes = outputStream.toByteArray();
            System.out.println("压缩前 bytes图片大小----------->"+ compressBytes.length/1024);*/


            //压缩图片
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

            //车牌、大点的文字可以调成5，小文字场景可以调成10或在高点
            mbtp.compress(Bitmap.CompressFormat.JPEG, compressQuality, outputStream);

            byte[] compressBytes = outputStream.toByteArray();

            logInfo(TAG, "压缩后图片大小-------->%s", compressBytes.length / 1024);

            return compressBytes;

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }


    /**
     * 通过post请求ocr识别
     *
     * @param url         接口地址
     * @param base64      图片转换后的base64字符串
     * @param accessToken
     */
    private void ocrHttpPostRequest(final String url, String base64, String accessToken) {
        if (accessToken == null) {
            showToast("网络异常,未获取到access_token");
            return;
        }

        //构建参数对象
        JSONObject param = new JSONObject();
        try {
            param.put("imgBase64", base64);
            param.put("imgType", "jpg");
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }

        //设置请求media类型为application/json; charset=utf-8
        MediaType mediaType = MediaType.parse(MEDIA_TYPE_APPLICATION);
        //连接超时时间为10s,将access_token添加到header中，对应key为access_token
        OkHttpClient okHttpClient = new OkHttpClient.Builder().connectTimeout(10, TimeUnit.SECONDS).build();
        Request request = new Request.Builder()
                .addHeader(HEADER_KEY_ACCESS_TOKEN, accessToken)
                .post(RequestBody.create(param.toString(), mediaType))
                .url(url)
                .build();

        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Log.e(TAG, "request onFailure URL:" + url, e);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        stopScanAnimation();
                        tvOcrResult.setText("");
                    }
                });
                showToast("请求接口失败："+e.getMessage());
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                String responseString = response.body().string();

                logInfo(TAG, "接口返回结果:%s", responseString);

                //OCR结果进行解析
                final String result = decodeOcrResult(responseString);

                //android要求要在主线程更新控件（界面）
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        stopScanAnimation();
                        tvOcrResult.setText(result);
                    }
                });
            }
        });
    }

    /**
     * 解析ocr接口返回的结果
     *
     * @param ocrResult
     * @return
     */
    private String decodeOcrResult(String ocrResult) {
        /**
         * 简单解析，不在进行返回结果中 code、msg等字段的解析
         */
        StringBuilder sb = new StringBuilder();
        try {
            JSONObject resultJsonObject = new JSONObject(ocrResult);
            int code = resultJsonObject.getInt("code");
            if (REQUEST_API_SUCCESS_CODE != code) {
                showToast("图片识别错误:"+resultJsonObject.getString("msg"));
                return "";
            }
            JSONArray dataJsonArray = resultJsonObject.getJSONArray("data");
            for (int i = 0; i < dataJsonArray.length(); i++) {
                sb.append(dataJsonArray.getString(i)).append("\n");
            }
            return sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    private void logInfo(String tag, String format, Object... args) {
        Log.i(tag, String.format(format, args));
    }

}
