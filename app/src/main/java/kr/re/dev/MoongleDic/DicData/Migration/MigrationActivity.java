package kr.re.dev.MoongleDic.DicData.Migration;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.widget.TextView;

import com.google.common.io.Files;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.CountDownLatch;

import kr.re.dev.MoongleDic.Commons.ProgressEvent;
import kr.re.dev.MoongleDic.DicService.PhoneticPlayer;
import kr.re.dev.MoongleDic.R;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;

/**
 * Created by ice3x2 on 15. 5. 1..
 */
public class MigrationActivity  extends Activity {

    PhoneticPlayer mPhoneticPlayer;
    TextView mLogTextView;
    TextView mProgressTextView;
    TextView mMemInfoTextView;

    ActivityManager.MemoryInfo mMemInfo = new ActivityManager.MemoryInfo();
    ActivityManager mActivityManager;
    Handler mHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        finish();


        setContentView(R.layout.activity_main);
        mLogTextView = (TextView)findViewById(R.id.textViewLog);
        mProgressTextView = (TextView) findViewById(R.id.textView);
        mMemInfoTextView = (TextView)findViewById(R.id.textViewMemInfo);


        //mUseMemorySeries.setSpacing(50);



        mActivityManager = (ActivityManager)getSystemService(ACTIVITY_SERVICE);

        File fromFile = new File(this.getFilesDir(), "Vicon English-Korean Dictionary.ld2");
        File toFile = new File(Environment.getExternalStorageDirectory(), "Vicon English-Korean Dictionary.ld2");


        mProgressTextView.setText("Complete!");
        try {
            Files.copy(fromFile, toFile);
        } catch (IOException e) {
            e.printStackTrace();
        }



        /**
         * ld2 를 Realm DB 에 쑤셔넣기 위한 코드.
         * 이 것을 실행하기 위해서는 매니패스트의 Application 속성을 android:largeHeap="true" 을 줘야한다.
         * 어차피 일회성 코드라 퍼포먼스 위주로(대충) 작성했기 때문이다.
         */
        Ld2Migrator migrator =  Ld2Migrator.newInstance();
        Context context = getApplicationContext();
        String file = "Vicon English-Korean Dictionary.ld2";
        String db = "Vicon English-Korean Dictionary.db";
        new File(context.getFilesDir(), db).delete();
        CountDownLatch latch = new CountDownLatch(1);
        Ld2Migrator ld2Migrator = Ld2Migrator.newInstance();
        Observable<ProgressEvent> observable =  ld2Migrator.migrateFromLd2(context, file);
        observable.observeOn(AndroidSchedulers.mainThread())
                .doOnError(e -> {
                    Log.d("testio", e.getMessage());
                    latch.countDown();
                })
                .doOnCompleted(() -> {
                    File fromFileA = new File(context.getFilesDir(), "Vicon English-Korean Dictionary.db");
                    File toFileA = new File(Environment.getExternalStorageDirectory(), "Vicon English-Korean Dictionary.db");
                    mProgressTextView.setText("Complete!");
                    try {
                        Files.copy(fromFileA, toFileA);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                })
                .subscribe(event -> {
                    Log.d("testio", event.getProgress() + "%");
                    if (!event.isComplete()) {
                        Log.d("testio", event.getProgress() + "%");
                        mProgressTextView.setText(event.getProgress() + "%");
                    } else {
                        mProgressTextView.setText("OK!");
                        Log.d("testio", "OK!!");
                        latch.countDown();
                    }
                });
        ld2Migrator.eventLogs(7).observeOn(AndroidSchedulers.mainThread()).subscribe(log -> mLogTextView.setText(log));


    }

    long mAllSec = 0;


}
