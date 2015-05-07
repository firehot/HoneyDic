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
 * Created by ice3x2 on 15. 5. 8..
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
