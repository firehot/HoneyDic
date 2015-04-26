package kr.re.dev.MoogleDic;

import android.app.ActivityManager;
import android.content.Context;
import android.graphics.Color;
import android.os.Debug;
import android.os.Environment;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.widget.TextView;

import com.google.common.io.Files;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.LegendRenderer;
import com.jjoe64.graphview.series.BarGraphSeries;
import com.jjoe64.graphview.series.DataPoint;

import java.io.File;
import java.io.IOException;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.logging.LogRecord;

import kr.re.dev.MoogleDic.Commons.ProgressEvent;
import kr.re.dev.MoogleDic.DicData.Migration.Ld2Migrator;
import kr.re.dev.MoogleDic.DicService.PhoneticPlayer;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;


public class MainActivity extends ActionBarActivity {

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
        setContentView(R.layout.activity_main);
        mLogTextView = (TextView)findViewById(R.id.textViewLog);
        mProgressTextView = (TextView) findViewById(R.id.textView);
        mMemInfoTextView = (TextView)findViewById(R.id.textViewMemInfo);


        //mUseMemorySeries.setSpacing(50);



        mActivityManager = (ActivityManager)getSystemService(ACTIVITY_SERVICE);

        File[] files = this.getFilesDir().listFiles();
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
/*        Ld2Migrator migrator =  Ld2Migrator.newInstance();
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
                    File fromFile = new File(context.getFilesDir(), "Vicon English-Korean Dictionary.db");
                    File toFile = new File(Environment.getExternalStorageDirectory(), "Vicon English-Korean Dictionary.db");
                    mProgressTextView.setText("Complete!");
                    try {
                        Files.copy(fromFile, toFile);
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
*/

    }

    long mAllSec = 0;




    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        Observable<String> observable = Observable.create((sub) -> {
            sub.onNext("Hello, World!");
            sub.onCompleted();
        });





        return super.dispatchTouchEvent(ev);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
