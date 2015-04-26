package kr.re.dev.MoogleDic.DicService;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;

/**
 * Created by ice3x2 on 15. 4. 22..
 */
public class PhoneticFileDownloader extends BroadcastReceiver {

    private PhoneticFileDownloader(Context context) {

    }

    private void receiveWifiStatus(Context context) {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION);
        context.registerReceiver(this, intentFilter);
    }


    @Override
    public void onReceive(Context context, Intent intent) {
        String action  = intent.getAction();
        if(action.equals(WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION)) {
            if(intent.getBooleanExtra(WifiManager.EXTRA_SUPPLICANT_CONNECTED, false)) {

            } else{

            }
        }
    }
}
