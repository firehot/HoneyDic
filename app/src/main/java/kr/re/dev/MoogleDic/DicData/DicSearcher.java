package kr.re.dev.MoogleDic.DicData;

import android.content.Context;
import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


import io.realm.Realm;
import io.realm.RealmResults;
import kr.re.dev.MoogleDic.Commons.ProgressEvent;
import rx.Observable;
import rx.Subscriber;
import rx.schedulers.Schedulers;
import rx.subjects.ReplaySubject;

/**
 * 사전 검색.
 * Created by ice3x2 on 15. 4. 15..
 */
public class DicSearcher {

    private volatile Observable<Realm> mInitRealObservable;
    private ReplaySubject<ProgressEvent> mProgressSubject = ReplaySubject.create();
    private ExecutorService mSingleExecutor = Executors.newSingleThreadExecutor();
    private String mDicName = "";


    /**
     * DicSearcher 생성.
     * @param context 뭔지 알지?
     * @param dbName Assets 폴더에 들어있는 DB 파일 이름.
     * @return DicSearcher 의 인스턴스.
     * @throws IOException 만약 Assets 폴더에 두 번째 인자로 입력한 DB 이름에 해당하는 파일이 없을 경우 발생한다.
     */
    public static DicSearcher newInstance(Context context, String dbName) throws IOException {
        return  new DicSearcher(context, dbName);
    }

    /**
     * 사전 초기화 이벤트를 받아온다.
     * 사전 최기화가 완료 될 경우 onComplete() 발생.
     * @return 만약 에러 발생시
     */
    public Observable<ProgressEvent> eventFromLoadDB() {
        return Observable.amb(mProgressSubject, mProgressSubject).subscribeOn(Schedulers.newThread()).onErrorReturn(e -> ProgressEvent.obtain(e));
    }

    /**
     * 단어를 검색한다.
     * @param word 앞뒤에 공백이 있어도 상관없다. 대소문자 가리지 않는다.
     * @return 검색한 사전 내용을 받아올 수 있는 이벤트.
     */
    public Observable<DictionaryWord> search(String word) {
        return null;
        /*return mInitRealObservable.flatMap(realm -> Observable.just(realm)
                .map(realm1 -> searchWord(realm1, word)).subscribeOn(Schedulers.from(mSingleExecutor)));*/
    }

    /**
     * 사전을 닫아준다.
     */
    public void close() {

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
                onInitDBComplete(sub, fileCoptySubject, context, dbFile);
                return;
            }
            dbFile.delete();
            copyDBFromAssetsInputStream(sub, fileCoptySubject, context, DBName);
            onInitDBComplete(sub, fileCoptySubject, context, dbFile);
        } catch (Exception e) {
            dbFile.delete();
            e.printStackTrace();
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

    private void onInitDBComplete(Subscriber<? super Realm> sub,ReplaySubject<ProgressEvent> fileCoptySubject,Context context,  File dbFile) {
        fileCoptySubject.onNext(ProgressEvent.obtain(100, 100, true));
        fileCoptySubject.onCompleted();
        sub.onNext(Realm.getInstance(context, dbFile.getName()));
        sub.onCompleted();
    }


    private List<WordCard> searchWord(Realm realm, String word) {
        word =  word.replaceAll("^[ \\t\\r\\n\\f]{0,}","").replaceAll("[ \\t\\r\\n\\f]{0,}$","");
        DictionaryWord emptyDBColumn =  new DictionaryWord();
        emptyDBColumn.setDicName("");
        /*if(word.isEmpty()) {
            return emptyDBColumn;
        }

        Log.i("testio","search by : " + word);
        RealmResults<DictionaryWord> results =  realm.where(DictionaryWord.class).equalTo("keyword", word, false).findAll();
        Log.i("testio", "result size : " + results.size() + "");
        if(!results.isEmpty()) {
            Log.i("testio","result!!");
            return results.get(0);
        }

        emptyDBColumn.setWord("");
        return emptyDBColumn;*/
        return null;
    }



}
