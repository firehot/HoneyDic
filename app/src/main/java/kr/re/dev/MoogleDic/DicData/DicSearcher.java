package kr.re.dev.MoogleDic.DicData;

import android.content.Context;
import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.realm.Realm;
import rx.Observable;
import rx.Subscriber;
import rx.schedulers.Schedulers;
import rx.subjects.ReplaySubject;

/**
 * 사전 정보를 조회하고 검색한다.
 * Created by ice3x2 on 15. 4. 15..
 */
public class DicSearcher {


    private volatile Observable<Realm> mInitRealObservable;
    private ReplaySubject<ProgressEvent> mProgressSubject = ReplaySubject.create();
    private ExecutorService mSingleExecutor = Executors.newSingleThreadExecutor();
    private String mDicName = "";


    public static DicSearcher newInstance(Context context, String dbName) throws IOException {
        return  new DicSearcher(context, dbName);
    }

    private DicSearcher(Context context, String DBName) throws IOException {
        mDicName = DBName;
        InputStream assetsIs =  context.getAssets().open(DBName);
        assetsIs.close();
        mInitRealObservable = Observable.create((Observable.OnSubscribe<Realm>) sub -> initDBFromAssets(sub, mProgressSubject, context, DBName))
        .subscribeOn(Schedulers.from(mSingleExecutor)).cache();
        mInitRealObservable.subscribe();
    }

    private void initDBFromAssets(Subscriber<? super Realm> sub,ReplaySubject<ProgressEvent> fileCoptySubject, Context context,String DBName) {
        File dbFile = new File(context.getFilesDir(), DBName);
        try {
            InputStream assetsIs = null;

            if(dbFile.isFile()) {
                Log.i("testio", "pre load");
                doOnInitDBComplete(sub, fileCoptySubject, context, dbFile);
                return;
            }

            dbFile.delete();
            copyDBFromAssetsInputStream(sub, fileCoptySubject, context, DBName);
            doOnInitDBComplete(sub, fileCoptySubject, context, dbFile);
            Log.i("testio", "tk2");

        } catch (Exception e) {
            Log.i("testio", "Exception");
            dbFile.delete();
            e.printStackTrace();
            sub.onError(e);
            fileCoptySubject.onError(e);
        }
    }

    private void copyDBFromAssetsInputStream(Subscriber<? super Realm> sub,ReplaySubject<ProgressEvent> fileCoptySubject, Context context,String DBName) throws IOException {
        InputStream assetsIs = context.getAssets().open(DBName);
        File dbFile = new File(context.getFilesDir(), DBName);
        FileOutputStream fos = null;
        File tmpFile = new File(dbFile.getParentFile(), DBName + "_");
        tmpFile.delete();
        Log.i("testio", "tk1");
        fos = new FileOutputStream(tmpFile);
        BufferedOutputStream bos = new BufferedOutputStream(fos);
        byte[] buffer = new byte[2048];
        int len = 0, lastProgress = -1;
        long count = 0, fileSize = assetsIs.available();
        while((len = assetsIs.read(buffer)) > 0) {
            bos.write(buffer,0,len);
            count += len;
            int progress = (int)(((float)count / (float)fileSize) * 99.0f);
            if(lastProgress != progress) {
                Log.i("testio", "progress : " + count);
                lastProgress = progress;
                fileCoptySubject.onNext(ProgressEvent.obtain(100, progress, false));
            }
        }
        fos.close();
        tmpFile.renameTo(dbFile);
    }

    private void doOnInitDBComplete(Subscriber<? super Realm> sub,ReplaySubject<ProgressEvent> fileCoptySubject,Context context,  File dbFile) {
        fileCoptySubject.onNext(ProgressEvent.obtain(100, 100, true));
        fileCoptySubject.onCompleted();
        sub.onNext(Realm.getInstance(context, dbFile.getName()));
        sub.onCompleted();
    }

    public Observable<ProgressEvent> eventFromLoadDB() {
        return Observable.amb(mProgressSubject, mProgressSubject).subscribeOn(Schedulers.newThread());
    }
    public Observable<DicDBColumn> search(String word) {
        word =  word.replaceAll("^[ \\t\\r\\n\\f]{0,}","").replaceAll("[ \\t\\r\\n\\f]{0,}$","");
        final String finalWord = word;
        return mInitRealObservable.flatMap(realm -> Observable.just(realm)
                .map(realm1 -> realm1.where(DicDBColumn.class).equalTo("word", finalWord, false).findAll())
                .map(result -> {
                    if (result.isEmpty()) return new DicDBColumn(finalWord, "", mDicName);
                    return result.get(0);
                }).subscribeOn(Schedulers.from(mSingleExecutor)));
    }

    public void close() {
        mInitRealObservable.subscribe(realm -> realm.close()).unsubscribe();
    }

}
