package kr.re.dev.MoongleDic;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import rx.Scheduler;
import rx.schedulers.Schedulers;

/**
 * Created by ice3x2 on 15. 5. 1..
 */
public class SingleSchedulers {
    private static final ExecutorService sSingleThreadExecutor = Executors.newSingleThreadExecutor();
    private static final Scheduler SINGLE_THREAD_SCHEDULER = Schedulers.from(sSingleThreadExecutor);

    public static Scheduler singleThread() {
        return SINGLE_THREAD_SCHEDULER;
    }

}
