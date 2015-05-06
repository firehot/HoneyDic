package kr.re.dev.MoongleDic;

import android.app.Application;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.google.common.collect.Maps;

import java.util.HashMap;

import kr.re.dev.MoongleDic.DicService.ClipboardDicService;

/**
 * 
 * Created by ice3x2 on 15. 4. 15..
 */
public class MoongleDicApplication extends Application {

    @Override
    public void onCreate() {
        Log.i("testio", "start application");
        Intent service = new Intent(getApplicationContext(), ClipboardDicService.class);
        startService(service);
        super.onCreate();
    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        HashMap<Integer,String> map = Maps.newHashMap();
        //map.put(15,"TRIM_MEMORY_RUNNING_CRITICAL" );
        //map.put(10,"TRIM_MEMORY_RUNNING_LOW" );
        //map.put(5,"TRIM_MEMORY_RUNNING_MODERATE" );
        //map.put(20,"TRIM_MEMORY_UI_HIDDEN" );
        //Toast.makeText(getApplicationContext(), map.get(level), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        //Toast.makeText(getApplicationContext(),"Low memory!", Toast.LENGTH_SHORT).show();
    }

}
