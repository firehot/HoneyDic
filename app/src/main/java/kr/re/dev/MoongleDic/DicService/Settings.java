package kr.re.dev.MoongleDic.DicService;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import kr.re.dev.MoongleDic.Constants;

/**
 *  클립보드 영어사전 HoenyDic::Settings class.
 *  설정 정보를 갖고 있다.
 *  Copyright (C) 2015 ice3x2@gmail.com [https://github.com/ice3x2/HoneyDic]
 *  </br></br>
 *
 *  This program is free software:
 *  you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *  You should have received a copy of the GNU General Public License along with this program. If not, see < http://www.gnu.org/licenses/ >.
 *
 *  </br></br>
 *  [한글. 번역 출처 : https://wiki.kldp.org/wiki.php/GNU/GPLV3Translation]</br>
 *
 *  이 프로그램은 자유 소프트웨어입니다:
 *  당신은 이것을 자유 소프트웨어 재단이 발표한 GNU 일반 공중 사용허가서의 제3 버전이나 (선택에 따라) 그 이후 버전의 조항 아래 재배포하거나 수정할 수 있습니다.
 *  이 프로그램은 유용하게 쓰이리라는 희망 아래 배포되지만, 특정한 목적에 대한 프로그램의 적합성이나 상업성 여부에 대한 보증을 포함한 어떠한 형태의 보증도 하지 않습니다.
 *  세부 사항은 GNU 일반 공중 사용허가서를 참조하십시오.
 *  당신은 이 프로그램과 함께 GNU 일반 공중 사용허가서를 받았을 것입니다. 만약 그렇지 않다면, < http://www.gnu.org/licenses/ > 를 보십시오.
 *
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
