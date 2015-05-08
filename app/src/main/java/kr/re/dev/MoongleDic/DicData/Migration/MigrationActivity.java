package kr.re.dev.MoongleDic.DicData.Migration;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
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
 *  클립보드 단어사전 HoenyDic::MigrationActivity class.
 *  Ld2 파일을 DB 로 마이그레이션하는 액티비티.
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
        //setContentView(R.layout.activity_main);



       // mLogTextView = (TextView)findViewById(R.id.textViewLog);
      //  mProgressTextView = (TextView) findViewById(R.id.textView);
       // mMemInfoTextView = (TextView)findViewById(R.id.textViewMemInfo);


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
