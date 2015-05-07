package kr.re.dev.MoongleDic.DicService;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import kr.re.dev.MoongleDic.Constants;

/**
 * Created by ice3x2 on 15. 5. 6..
 */
public class Settings {

    private final static String SETTING = "SETTING";
    private final static String SETTING_USE_CLIPBOARD_DIC = "SETTING_USE_CLIPBOARD_DIC";
    private final static String SETTING_WORDCARD_NONE_FORCE_CLOSE = "SETTING_WORDCARD_NONE_FORCE_CLOSE";
    private final static String SETTING_USE_TTS = "SETTING_USE_TTS";
    private final static String SETTING_SOUND_EFFECT = "SETTING_SOUND_EFFECT";
    private final static String SETTING_WORDCARD_KEEP_TIME = "SETTING_WORDCARD_KEPP_TIME";

    private boolean mUseClipboardDic = true;
    private boolean mWordCardNoneForceClose = false;
    private boolean mUseTTS = true;
    private boolean mSoundEffect = true;
    private int mWordCardKeepTime = 0;


    public static Settings getSettings(Intent intent) {
        return new Settings(intent);
    }

    public static Settings getSettings(Context context) {
        return new Settings(context);
    }

    private Settings(Intent intent) {
        mUseClipboardDic =  intent.getBooleanExtra(SETTING_USE_CLIPBOARD_DIC, mUseClipboardDic);
        mWordCardNoneForceClose =  intent.getBooleanExtra(SETTING_WORDCARD_NONE_FORCE_CLOSE, mWordCardNoneForceClose);
        mWordCardKeepTime = intent.getIntExtra(SETTING_WORDCARD_KEEP_TIME, mWordCardKeepTime);
        mUseTTS = intent.getBooleanExtra(SETTING_USE_TTS, mUseTTS);
        mSoundEffect = intent.getBooleanExtra(SETTING_SOUND_EFFECT, mSoundEffect);
    }

    private Settings(Context context) {
        SharedPreferences sharedPreferences =  context.getSharedPreferences(SETTING, Context.MODE_PRIVATE);
        mUseClipboardDic =  sharedPreferences.getBoolean(SETTING_USE_CLIPBOARD_DIC, mUseClipboardDic);
        mWordCardNoneForceClose =  sharedPreferences.getBoolean(SETTING_WORDCARD_NONE_FORCE_CLOSE, mWordCardNoneForceClose);
        mWordCardKeepTime = sharedPreferences.getInt(SETTING_WORDCARD_KEEP_TIME, mWordCardKeepTime);
        mUseTTS = sharedPreferences.getBoolean(SETTING_USE_TTS, mUseTTS);
        mSoundEffect = sharedPreferences.getBoolean(SETTING_SOUND_EFFECT, mSoundEffect);
    }
    public void commit(Context context) {
        saveBySharePreferences(context);
        sendBroadcast(context);
    }

    private void saveBySharePreferences(Context context) {
        SharedPreferences sharedPreferences =  context.getSharedPreferences(SETTING, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(SETTING_USE_CLIPBOARD_DIC, mUseClipboardDic);
        editor.putBoolean(SETTING_WORDCARD_NONE_FORCE_CLOSE, mWordCardNoneForceClose);
        editor.putInt(SETTING_WORDCARD_KEEP_TIME, mWordCardKeepTime);
        editor.putBoolean(SETTING_USE_TTS, mUseTTS);
        editor.putBoolean(SETTING_SOUND_EFFECT, mSoundEffect);
        editor.commit();
    }

    private void sendBroadcast(Context context) {
        Intent intent = new Intent(Constants.ACTION.BROADCAST_SETTING);
        intent.putExtra(SETTING_USE_CLIPBOARD_DIC, mUseClipboardDic);
        intent.putExtra(SETTING_WORDCARD_NONE_FORCE_CLOSE, mWordCardNoneForceClose);
        intent.putExtra(SETTING_WORDCARD_KEEP_TIME, mWordCardKeepTime);
        intent.putExtra(SETTING_USE_TTS, mUseTTS);
        intent.putExtra(SETTING_SOUND_EFFECT, mSoundEffect);
        context.sendBroadcast(intent);
    }


    public boolean isUseClipboardDic() {
        return mUseClipboardDic;
    }

    public void setUseClipboardDic(boolean mUseClipboardDic) {
        this.mUseClipboardDic = mUseClipboardDic;
    }

    public boolean isWordCardNoneForceClose() {
        return mWordCardNoneForceClose;
    }

    public void setWordCardNoneForceClose(boolean mWordCardNoneForceClose) {
        this.mWordCardNoneForceClose = mWordCardNoneForceClose;
    }

    public boolean isUseTTS() {
        return mUseTTS;
    }

    public void setUseTTS(boolean mUseTTS) {
        this.mUseTTS = mUseTTS;
    }

    public boolean isSoundEffect() {
        return mSoundEffect;
    }

    public void setSoundEffect(boolean mSoundEffect) {
        this.mSoundEffect = mSoundEffect;
    }

    public int getWordCardKeepTime() {
        return mWordCardKeepTime;
    }

    /**
     * 단위는 ms 다.
     * @param WordCardKeepTimeMs
     */
    public void setWordCardKeepTime(int WordCardKeepTimeMs) {
        this.mWordCardKeepTime = WordCardKeepTimeMs;
    }





}
