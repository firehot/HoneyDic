package kr.re.dev.MoogleDic.DicService;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class DicService extends Service {
    public DicService() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {



        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.

        throw new UnsupportedOperationException("Not yet implemented");
    }
}
