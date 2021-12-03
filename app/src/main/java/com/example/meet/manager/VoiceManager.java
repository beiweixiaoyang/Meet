package com.example.meet.manager;

import android.content.Context;
import android.text.TextUtils;

import com.example.meet.gson.VoiceBean;
import com.example.meet.utils.LogUtils;
import com.google.gson.Gson;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechUtility;
import com.iflytek.cloud.ui.RecognizerDialog;
import com.iflytek.cloud.ui.RecognizerDialogListener;

/**
 * 语音识别相关操作
 */
public class VoiceManager {
    private static volatile VoiceManager instance;
    private RecognizerDialog mIatDialog;

    private VoiceManager(Context context) {
        LogUtils.i("initVoiceManager");
        SpeechUtility.createUtility(context, SpeechConstant.APPID + "=9e29bcbf");
        mIatDialog = new RecognizerDialog(context, new InitListener() {
            @Override
            public void onInit(int i) {

            }
        });
        //清空所有属性
        mIatDialog.setParameter(SpeechConstant.CLOUD_GRAMMAR, null);
        mIatDialog.setParameter(SpeechConstant.SUBJECT, null);
        //设置返回格式
        mIatDialog.setParameter(SpeechConstant.RESULT_TYPE, "json");
        //此处engineType为“cloud”
        mIatDialog.setParameter(SpeechConstant.ENGINE_TYPE,SpeechConstant.TYPE_CLOUD );
        //设置语音输入语言，zh_cn为简体中文
        mIatDialog.setParameter(SpeechConstant.LANGUAGE, "zh_cn");
        //设置结果返回语言
        mIatDialog.setParameter(SpeechConstant.ACCENT, "mandarin");
        // 设置语音前端点:静音超时时间，单位ms，即用户多长时间不说话则当做超时处理
        //取值范围{1000～10000}
        mIatDialog.setParameter(SpeechConstant.VAD_BOS, "4000");
        //设置语音后端点:后端点静音检测时间，单位ms，即用户停止说话多长时间内即认为不再输入，
        //自动停止录音，范围{0~10000}
        mIatDialog.setParameter(SpeechConstant.VAD_EOS, "1000");
        //设置标点符号,设置为"0"返回结果无标点,设置为"1"返回结果有标点
        mIatDialog.setParameter(SpeechConstant.ASR_PTT, "1");
    }

    public static VoiceManager getInstance(Context context) {
        if (instance == null) {
            synchronized (VoiceManager.class) {
                if (instance == null) {
                    instance = new VoiceManager(context);
                }
            }
        }
        return instance;
    }

    /**
     * 语音转文字
     * @return 返回识别结果
     */
    public String getSpeakText(){
        StringBuffer sb = new StringBuffer();
        mIatDialog.setListener(new RecognizerDialogListener() {
            @Override
            public void onResult(RecognizerResult recognizerResult, boolean b) {
                String result=recognizerResult.getResultString();
                if(!TextUtils.isEmpty(result)){
                    VoiceBean voiceBean=new Gson().fromJson(result, VoiceBean.class);
                    if (voiceBean.isLs()) {
                        for (int i = 0; i < voiceBean.getWs().size(); i++) {
                            VoiceBean.WsBean wsBean = voiceBean.getWs().get(i);
                            String sResult = wsBean.getCw().get(0).getW();
                            sb.append(sResult);
                        }
                        LogUtils.i("result:" + sb.toString());
                    }
                }
            }

            @Override
            public void onError(SpeechError speechError) {

            }
        });
        mIatDialog.show();
        return sb.toString();
    }
}
