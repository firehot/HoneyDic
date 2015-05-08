package kr.re.dev.MoongleDic.DicData;

import android.content.Context;
import android.util.Log;

import com.google.common.collect.Lists;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;


import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmResults;
import kr.re.dev.MoongleDic.Commons.ProgressEvent;
import kr.re.dev.MoongleDic.DicData.Database.KeyWordColumns;
import kr.re.dev.MoongleDic.DicData.Database.WordColumns;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subjects.ReplaySubject;

/**
 *  클립보드 단어사전 HoenyDic::DicSearcher class.
 *  사전 데이터베이스로부터 단어를 검색하고 뜻과 정보를 가져온다.
 *  Copyright (C) 2015 ice3x2@gmail.com [https://github.com/ice3x2/HoneyDic]
 *  </br></br>
 *
 *  This program is free software:
 *  you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *  You should have received a copy of the GNU General Public License along with this program. If not, see < http://www.gnu.org/licenses/ >.
 *
 *  </br></br>
 *  [한글. 번역 출처 : https://wiki.kldp.org/wiki.php/GNU/GPLV3Translation]</br>
 *
 *  이 프로그램은 자유 소프트웨어입니다:
 *  당신은 이것을 자유 소프트웨어 재단이 발표한 GNU 일반 공중 사용허가서의 제3 버전이나 (선택에 따라) 그 이후 버전의 조항 아래 재배포하거나 수정할 수 있습니다.
 *  이 프로그램은 유용하게 쓰이리라는 희망 아래 배포되지만, 특정한 목적에 대한 프로그램의 적합성이나 상업성 여부에 대한 보증을 포함한 어떠한 형태의 보증도 하지 않습니다.
 *  세부 사항은 GNU 일반 공중 사용허가서를 참조하십시오.
 *  당신은 이 프로그램과 함께 GNU 일반 공중 사용허가서를 받았을 것입니다. 만약 그렇지 않다면, < http://www.gnu.org/licenses/ > 를 보십시오.
 *
 */
public class DicSearcher {

    private volatile Observable<Realm> mInitRealObservable;
    private ReplaySubject<ProgressEvent> mProgressSubject = ReplaySubject.create();
    private ExecutorService mSingleExecutor = Executors.newSingleThreadExecutor();
    private AtomicBoolean mIsRealmClosed = new AtomicBoolean(false);
    private String mDicName = "";

    /**
     * DicSearcher 생성.
     * @param context 뭔지 알지?
     * @param dbName Assets 폴더에 들어있는 DB 파일 이름.
     * @return DicSearcher 의 인스턴스.
     * @throws IOException 만약 Assets 폴더에 두 번째 인자로 입력한 DB 이름에 해당하는 파일이 없을 경우 발생한다.
     */
    public static DicSearcher newInstance(Context context, String dbName) {
        try {
            return  new DicSearcher(context, dbName);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
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
     * 반드시 onErrorResumeNext 를 통하여 에러 발생시 대처를 해야 한다.
     * @param word 앞뒤에 공백이 있어도 상관없다. 대소문자 가리지 않는다.
     * @return 검색한 사전 내용을 받아올 수 있는 이벤트.
     */
    public Observable<List<WordCard>> search(String word) {
        return mInitRealObservable
                .flatMap(realm ->
                        Observable.just(realm).map(realm1 -> searchWordSafe(realm1, word)).subscribeOn(Schedulers.from(mSingleExecutor))).observeOn(AndroidSchedulers.mainThread());
    }

    /**
     * 사전을 닫아준다.
     */
    public void close() {
        mInitRealObservable.subscribe(realmIn -> {
                mSingleExecutor.shutdown();
                mIsRealmClosed.set(true);
                realmIn.close();
                Log.i("testio", "DicSearcher is released");
        });
    }

    public String getDicname() {
        return mDicName;
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


    private List<WordCard> searchWordSafe(Realm realm, String word) {
        if(mIsRealmClosed.get()) {
            throw  new RuntimeException(new AlreadyClosedDBException(mDicName));
        }
        word = word.replaceAll("^[ \\t\\r\\n\\f]{0,}","").replaceAll("[ \\t\\r\\n\\f]{0,}$","");
        List<WordCard> wordCards = searchWord(realm,word);

        if(wordCards.isEmpty() && word.matches("ies$")) {
            wordCards = searchWord(realm, word.replace("ies$", "y"));
        }
        if(wordCards.isEmpty() && word.matches("ier$")) {
            wordCards = searchWord(realm, word.replace("ier$", "y"));
        }
        if(wordCards.isEmpty() && word.matches("es$")) {
            wordCards = searchWord(realm, word.replace("es$", ""));
        }
        if(wordCards.isEmpty() && word.matches("s$")) {
            wordCards = searchWord(realm, word.replace("s$", ""));
        }
        if(wordCards.isEmpty() && word.matches("ed$")) {
            wordCards = searchWord(realm, word.replace("ed$", ""));
        }
        if(wordCards.isEmpty() && word.matches("d$")) {
            wordCards = searchWord(realm, word.replace("d$", ""));
        }
        if(wordCards.isEmpty() && word.matches("ing$")) {
            wordCards = searchWord(realm,word.replace("ing$", ""));
        }
        return moveFirstSameWord(wordCards, word);
    }

    private List<WordCard> moveFirstSameWord(List<WordCard> wordCards, String word) {
        List<WordCard> results = Lists.newArrayListWithCapacity(wordCards.size());
        Iterator<WordCard> iter =  wordCards.iterator();
        while(iter.hasNext()) {
            WordCard wordCard = iter.next();
            if(wordCard.word().equals(word)) {
                iter.remove();
                results.add(wordCard);
                break;
            }
        }
        results.addAll(wordCards);
        return results;
    }


    private List<WordCard> searchWord(Realm realm, String word) {

        List<WordCard> results = Lists.newArrayList();
        if(word.isEmpty()) {
            return results;
        }
        RealmResults<KeyWordColumns> resultRealm =  realm.where(KeyWordColumns.class).equalTo("keyword", word, false).findAll();
        if(resultRealm.isEmpty()) {
            return results;
        }
        for(KeyWordColumns keywordColumns : resultRealm) {
            RealmList<WordColumns> wordColumnses = keywordColumns.getWords();
            if(wordColumnses == null) continue;
            for(WordColumns wordColumns : wordColumnses) {
                WordCard wordCard =  WordCard.newInstance(wordColumns);
                results.add(wordCard);
            }
        }
        return results;
    }


    public static class AlreadyClosedDBException extends Exception{
        private  AlreadyClosedDBException(String DBName) {
            super("Already closed DB : " + DBName);
        }
    }





}
