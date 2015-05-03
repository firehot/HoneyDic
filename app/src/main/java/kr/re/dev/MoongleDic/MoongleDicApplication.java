package kr.re.dev.MoongleDic;

import android.app.Application;
import android.content.Intent;
import android.util.Log;

import kr.re.dev.MoongleDic.DicService.DicService;

/**
 * 
 * Created by ice3x2 on 15. 4. 15..
 */
public class MoongleDicApplication extends Application {

    @Override
    public void onCreate() {
        Log.i("testio", "start application");
        Intent service = new Intent(getApplicationContext(), DicService.class);
        startService(service);
        super.onCreate();
    }

}
