package kr.re.dev.MoongleDic.DicService;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;


/**
 * ClipboardDicService 를 시작하는 리시버.
 */
public class BootReceiver extends BroadcastReceiver {

    public final static String ACTION_START_CLIPBOARDDIC = "kr.re.dev.MoongleDic.StartClipboardDic";
    public BootReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Settings settings =  Settings.getSettings(context);
        if(intent != null && ACTION_START_CLIPBOARDDIC.equals(intent.getAction())) {
            startService(context);
        } else if(settings.isUseClipboardDic()) {
            startService(context);
        }
    }

    private  void startService(Context context) {
        Log.i("testio", "Start ClipboradDicService by BootReceiver.");
        Intent service = new Intent(context, ClipboardDicService.class);
        context.startService(service);
    }
}
