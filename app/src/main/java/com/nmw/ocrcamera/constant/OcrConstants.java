package com.nmw.ocrcamera.constant;

/**
 * 本应用中相关常量
 */
public final class OcrConstants {
//    public static final String URL_BASE = "http://139.224.106.178:8088/ocr";
    public static final String URL_BASE = "http://192.168.31.113:8088/ocr";

    /**
     * 获取access_token接口地址
     */
    public static final String URL_ACCESS_TOKEN = URL_BASE + "/auth/access-token";

    /**
     * 文本识别接口地址
     */
    public static final String URL_OCR_TEXT = URL_BASE + "/text/text-only";

    /**
     * 车牌识别接口
     */
    public static final String URL_OCR_LICENSE_PLATE = URL_BASE + "/license-plate/text";

    /**
     * 身份证识别接口
     */
    public static final String URL_OCR_ID_CARD = URL_BASE + "/id-card/text";

    /**
     * 用来进行演示校验过程的虚拟 APP_KEY和 APP_SECRET
     */
    public final static String APP_KEY = "android_client";
    public final static String APP_SECRET = "android_client";

    /**
     * 请求接口成功，返回数据中，code对应0
     */
    public static final int REQUEST_API_SUCCESS_CODE = 0;

    /**
     * 文本识别请求码
     */
    public static final int REQUEST_CAMERA_OCR_TEXT = 1;

    /**
     * 车牌识别返回码
     */
    public static final int REQUEST_CAMERA_OCR_LICENSE_PLATE = 2;

    /**
     * 身份证识别方面
     */
    public static final int REQUEST_CAMERA_OCR_ID_CARD = 3;

    /**
     * 接口请求MediaType
     */
    public static final String MEDIA_TYPE_APPLICATION ="application/json; charset=utf-8";

    /**
     * 请求接口时header中要加入access_token对应的key
     */
    public static final String HEADER_KEY_ACCESS_TOKEN = "access_token";

    /**
     * Intent传递数据 请求码、图片路径、access_token 对应的key
     */
    public static final String INTENT_KEY_REQUEST_CODE = "requestCode";
    public static final String INTENT_KEY_IMG_PATH = "imgPath";
    public static final String INTENT_ACCESS_TOKEN = "access_token";
    public static final String INTENT_KEY_IMG_URI = "imgUri";
}
