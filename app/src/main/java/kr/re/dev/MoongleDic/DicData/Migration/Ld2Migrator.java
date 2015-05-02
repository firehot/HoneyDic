package kr.re.dev.MoongleDic.DicData.Migration;

import android.content.Context;
import android.util.Log;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import io.realm.Realm;
import io.realm.RealmResults;
import kr.re.dev.MoongleDic.Commons.ProgressEvent;
import kr.re.dev.MoongleDic.DicData.Database.KeyWordColumns;
import kr.re.dev.MoongleDic.DicData.Database.WordColumns;
import rx.Observable;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;


/**
 * ld2 파일을 DB 로 마이그레이션한다.
 *
 * Created by ice3x2 on 15. 4. 15..
 */
public class Ld2Migrator  {

    private PublishSubject<String> mLogEvnetPublishSubject = PublishSubject.create();
    private int mLogEventInterval = 0;
    private int mCountJobCount = 0;

    public static Ld2Migrator newInstance() {
        return new Ld2Migrator();
    }

    public Observable<String> eventLogs(int interval) {
        mLogEventInterval = interval;
       return Observable.create((Observable.OnSubscribe<String>) sub -> {
           mLogEvnetPublishSubject.doOnNext(e -> sub.onNext(e)).subscribe();
        }).subscribeOn(Schedulers.newThread());
    }

    public Observable<ProgressEvent> migrateFromLd2(Context context, String ld2FileNameInAsserts) {
        return Observable.create((Observable.OnSubscribe<ProgressEvent>) sub -> {
            try {
                //File tmpFile = null;
                mLogEvnetPublishSubject.onNext("Copy from assets.");
                File ld2File = copyFromAssets(context, ld2FileNameInAsserts, context.getCacheDir());
                mLogEvnetPublishSubject.onNext("ld2 file to plain text file.");
                LingoesLd2Reader ld2Reader = new LingoesLd2Reader();
                String txtPath = ld2Reader.readLd2(ld2File.getAbsolutePath());
                mLogEvnetPublishSubject.onNext("create DataBase.");
                PublishSubject<ProgressEvent> publishSubject = PublishSubject.create();
                publishSubject.doOnNext(e -> {
                    sub.onNext(e);
                    Log.i("testio", "call do on");

                }).subscribe();
                File dbFile = new File(context.getFilesDir(), txtPath.replaceAll("[.](TXT|txt)$", ".db"));
                txtFileToDB(context, new File(txtPath), dbFile.getName(), publishSubject);

            } catch (Exception e) {
                sub.onError(e);
            }
            sub.onCompleted();
        }).subscribeOn(Schedulers.io());
    }


    public Observable<ProgressEvent> migrateFromTextFile(Context context, String wordsTXTFileNameInAsserts) {
         return Observable.create((Observable.OnSubscribe<ProgressEvent>) sub -> {
             try {
                 //File tmpFile = null;
                 mLogEvnetPublishSubject.onNext("create DataBase.");
                 File tmpFile = copyFromAssets(context, wordsTXTFileNameInAsserts, context.getCacheDir());

                 PublishSubject<ProgressEvent> publishSubject = PublishSubject.create();
                 publishSubject.doOnNext(e -> {
                     sub.onNext(e);
                     Log.i("testio", "call do on");

                 }).subscribe();
                 File dbFile = new File(context.getFilesDir(), wordsTXTFileNameInAsserts.replaceAll("[.](TXT|txt)$", ".db"));


                 txtFileToDB(context, tmpFile, dbFile.getName(), publishSubject);

             } catch (Exception e) {
                 sub.onError(e);
             }
             sub.onCompleted();
         }).subscribeOn(Schedulers.newThread());
    }

    private File copyFromAssets(Context context, String fromName, File toDirs) throws IOException {
        InputStream is = null;
        is = context.getAssets().open(fromName);
        File tmpFile = new File(toDirs, fromName);
        FileOutputStream fos = new FileOutputStream(tmpFile);
        byte[] buffer = new byte[1024];
        int n = 0;
        while ((n = is.read(buffer)) > 0) {
            fos.write(buffer, 0, n);
        }
        is.close();
        fos.close();
        return tmpFile;
    }


    /**
     * 어차피 일회성 코드라 한 곳에 다 때려 박는다.... ;;;
     * 대신 가독성은 좋게.
     * @param context
     * @param textFile
     * @param DBName
     * @param subject
     * @throws IOException
     */
    private void txtFileToDB(Context context, File textFile, String DBName, PublishSubject<ProgressEvent> subject) throws IOException, NoSuchFieldException, IllegalAccessException, SQLException {
        /**
         * txt 파일 구조.
         *
         * 단어 개수 (숫자)
         * ---------------
         * \n
         * 단어
         * [base] | [derivation] (원형 | 파생형)
         * 단어 뜻 (원형일 경우에만 존재)
         * 참조 단어 개수
         * 참조 단어(단어 개수만큼 반복)
         * -----------------
         */

        FileInputStream txtIs =  new FileInputStream(textFile);
        Realm realm = Realm.getInstance(context, DBName);


        InputStreamReader isr = new InputStreamReader(txtIs);
        BufferedReader bi = new BufferedReader(isr);


        int total =  Integer.parseInt(bi.readLine());
        int count = 0, lastProgress = 0;
        subject.onNext(ProgressEvent.obtain(100, 0, false));
        HashMap<String, ArrayList<String>>  refWordMap = new HashMap<>();
        for(int idx = 0; idx < total; ++idx) {
            bi.readLine();
            String word = bi.readLine();


            mCountJobCount++;
            if(mCountJobCount < 0) mCountJobCount = 0;
            if(mCountJobCount % mLogEventInterval == 0) {
                mLogEvnetPublishSubject.onNext("push word(" + idx + "/" + total + ")  : " + word);
            }

            realm.beginTransaction();

            KeyWordColumns keyWordColumns =  realm.createObject(KeyWordColumns.class);
            keyWordColumns.setKeyword(word);
            keyWordColumns.setDicName(DBName);

            String type = bi.readLine();
            boolean isBase = type.contains("[base]"); //  원형일 경우
            if(isBase) {
                WordColumns wordColumns = realm.createObject(WordColumns.class);
                wordColumns.setWord(word);
                String description = bi.readLine();
                wordColumns.setDescription(description);
                keyWordColumns.getWords().add(wordColumns);
                keyWordColumns.setIsBase(true);
            } else {
                keyWordColumns.setIsBase(false);
            }

            String strRefs = bi.readLine();
            int refs = Integer.parseInt(strRefs);  // 참조 카운터
            keyWordColumns.setRefs(refs);
            realm.commitTransaction();


            ArrayList<String> refWordList = new ArrayList<>();
            refWordMap.put(word, refWordList);

            for(int i = 0; i < refs; ++i) {
                String refWord = bi.readLine();  // 레퍼런스 단어
                refWordList.add(refWord);
            }

            count = idx;
            int progress = (int)(((float)idx / (float)total) * 50.0f);
            if(lastProgress != progress) {
                lastProgress = progress;
                subject.onNext(ProgressEvent.obtain(100, lastProgress, false));
            }

        }

        Set<String> wordKeys = refWordMap.keySet();
        total = wordKeys.size();
        count = 0;
        for(String wordKey : wordKeys) {
            List<String> refWords = refWordMap.get(wordKey);

            realm.beginTransaction();
            RealmResults<KeyWordColumns> keyWordColumnses =  realm.where(KeyWordColumns.class).equalTo("keyword", wordKey).findAll();
            KeyWordColumns keyWordColumns =  keyWordColumnses.get(0);
            int realRefs = 0;

            mCountJobCount++;
            if(mCountJobCount < 0) mCountJobCount = 0;
            if(mCountJobCount % mLogEventInterval == 0) {
                mLogEvnetPublishSubject.onNext("reference word(" + count + "/" + total + ")  : " + wordKey);
            }

            for(String refWord : refWords) {
                RealmResults<WordColumns> wordColumnses =  realm.where(WordColumns.class).equalTo("word", refWord).findAll();
                if(wordColumnses.isEmpty())
                    continue;
                WordColumns wordColumns =  wordColumnses.get(0);
                keyWordColumns.getWords().add(wordColumns);
                realRefs++;
            }
            keyWordColumns.setRefs(realRefs);

            realm.commitTransaction();

            count++;
            int progress = (int)(((float)count / (float)total) * 50.0f) + 50;
            if(lastProgress != progress) {
                lastProgress = progress;
                subject.onNext(ProgressEvent.obtain(100, lastProgress, false));
            }
        }
        subject.onNext(ProgressEvent.obtain(100, 100, true));


        Log.i("testio", "Words was inserted : " + (count + 1));


        subject.onNext(ProgressEvent.obtain(100, 100, true));
        subject.onCompleted();
        realm.close();
    }








}
