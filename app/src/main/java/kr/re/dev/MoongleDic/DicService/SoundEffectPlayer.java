package kr.re.dev.MoongleDic.DicService;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;
import android.util.Log;

import com.google.common.collect.Maps;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import kr.re.dev.MoongleDic.R;

/**
 *  클립보드 영어사전 HoenyDic::SoundEffectPlayer class.
 *  효과음을 재생한다.
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
public class SoundEffectPlayer {
    private final static int MAX_STREAMS = 2;
    public enum SoundEffect{ TrashWordCard,RememberWordCard }
    private Map<SoundEffect,Integer> mMapSoundIds = Maps.newHashMap();
    private boolean mIsReleased = false;

    private static AtomicInteger sReferenceCounter = new AtomicInteger();
    private static SoundPool sSoundPool;



    public static SoundEffectPlayer getInstance(Context context) {
        SoundEffectPlayer soundEffectPlayer = new SoundEffectPlayer(context);
        sReferenceCounter.incrementAndGet();
        return soundEffectPlayer;
    }

    private SoundEffectPlayer(Context context) {
        if(sSoundPool == null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            sSoundPool = new SoundPool.Builder().setAudioAttributes(new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()).setMaxStreams(MAX_STREAMS).build();
        } else if(sSoundPool == null) {

            sSoundPool = new SoundPool(MAX_STREAMS, AudioManager.STREAM_RING, 0);
        }
        mMapSoundIds.put(SoundEffect.TrashWordCard, sSoundPool.load(context, R.raw.sheck, 1));
        mMapSoundIds.put(SoundEffect.RememberWordCard, sSoundPool.load(context, R.raw.ttitting, 1));
    }

    public void play(SoundEffect effect) {
        if(isReleased()) return;
        if(!mMapSoundIds.containsKey(effect)) return;
        sSoundPool.play(mMapSoundIds.get(effect), 1.0f, 1.0f, 0, 0, 1);
    }


    public boolean isReleased() {
        return mIsReleased;
    }



    /**
     * @return 남아있는 레퍼런스 카운터.
     */
    public int release() {
        if(isReleased()) return sReferenceCounter.get();
        if(sReferenceCounter.decrementAndGet() <= 0) {
            sSoundPool.release();
            sSoundPool = null;
            Log.i("testio", "released SoundEffectPlayer");
        }
        mIsReleased = true;
        Log.i("testio", "decrease ref count : " + sReferenceCounter.get());
        return sReferenceCounter.get();
    }
}
