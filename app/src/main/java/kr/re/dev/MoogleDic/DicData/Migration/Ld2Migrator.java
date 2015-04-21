package kr.re.dev.MoogleDic.DicData.Migration;

import android.content.Context;
import android.util.Log;

//import com.google.common.annotations.VisibleForTesting;
//import com.j256.ormlite.dao.Dao;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import io.realm.Realm;
import kr.re.dev.MoogleDic.DicData.DicDBColumn;
import rx.Observable;
import rx.Subscriber;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;


/**
 * ld2 파일을 DB 로 마이그레이션한다.
 *
 * Created by ice3x2 on 15. 4. 15..
 */
public class Ld2Migrator  {

    public static Ld2Migrator newInstance() {
        return new Ld2Migrator();
    }

    public Observable<MigrationEvent> migrate(Context context, String ld2FileNameInAsserts) {
         return Observable.create((Observable.OnSubscribe<MigrationEvent>) sub -> {
            executeMigrate(context, ld2FileNameInAsserts, sub);
         });
    }


    private void executeMigrate(Context context, String ld2FileNameInAsserts, Subscriber<? super MigrationEvent> sub) {
        try {
            File ld2File = copyAssetsToCache(context, ld2FileNameInAsserts);
            String dbName = l2pathToDBFileName(ld2File.getAbsolutePath());
            if (ld2File == null || !ld2File.exists())
                sub.onError(new IOException("Asset file does not exist : " + ld2FileNameInAsserts));
            File dbFile = new File(context.getFilesDir(), dbName);
            ReadLd2Event readLd2Event = new ReadLd2Event(dbFile);
            PublishSubject<Integer> progressPublish = readLd2Event.getProgressPublish();
            MigrationEvent migrationEvent = new MigrationEvent(0, dbFile, false);
            progressPublish.asObservable().subscribeOn(Schedulers.newThread()).subscribe(progress -> {
                migrationEvent.progress = progress;
                sub.onNext(migrationEvent);
            });
            Observable.merge(progressPublish, null).doOnError(e -> sub.onError(e));
            new LingoesLd2Reader().readLd2(ld2File.getAbsolutePath(), readLd2Event);
            migrationEvent.isComplete = true;
            sub.onNext(migrationEvent);
            progressPublish.onCompleted();
            sub.onCompleted();
        } catch (IOException e) {
            sub.onError(e);
        }
    }



    private File copyAssetsToCache(Context context, String assetsFile) {
        File file = new File(context.getCacheDir(), assetsFile);
        try {
            InputStream is =  context.getAssets().open(assetsFile);
            BufferedInputStream bis = new BufferedInputStream(is);
            FileOutputStream fos = new FileOutputStream(file);
            BufferedOutputStream bos = new BufferedOutputStream(fos);
            byte[] buffer = new byte[2048];
            int len = 0;
            while((len = bis.read(buffer)) > 0) {
                bos.write(buffer, 0, len);
            }
            buffer = null;
            bos.close();
            fos.close();
            bis.close();
            is.close();;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return file;
    }


    private String l2pathToDBFileName(String ld2Path) {
        File file = new File(ld2Path);
        String name =  file.getName();
        name =  name.replaceAll("(ld2|Ld2|LD2|lD2)", "db");
        return name;
    }

    private class ReadLd2Event implements LingoesLd2Reader.ReadLd2Event {

        PublishSubject<Integer> imProgressSubject;
        String imDatabaseName;
        int imTotalWorlds = 0;
        int imReadedWorlds = 0;
        int imLastProgress = -1;
        Realm imRealm;

        ReadLd2Event(File DBFile) {
            imProgressSubject = PublishSubject.create();
            imProgressSubject.observeOn(Schedulers.newThread());
            imRealm = Realm.getInstance(DBFile.getParentFile(), DBFile.getName(), null);
            imDatabaseName = DBFile.getName();
        }

        PublishSubject<Integer> getProgressPublish() {
            return imProgressSubject;
        }

        @Override
        public void word(String word, String xml) {
            ++imReadedWorlds;
            imRealm.beginTransaction();
            DicDBColumn dicDBColumn = imRealm.createObject(DicDBColumn.class);
            dicDBColumn.setDicName(imDatabaseName);
            dicDBColumn.setWord(word);
            dicDBColumn.setDescription(xml);
            imRealm.commitTransaction();

            int progress = (int)(((float)imReadedWorlds / (float)imTotalWorlds) * 100.0f);
            if(progress != imLastProgress) {
                Log.d("test", "in progress : " + progress);
                imLastProgress = progress;
                imProgressSubject.onNext(imLastProgress);
            }
            if(imReadedWorlds >= imTotalWorlds) {
                imProgressSubject.onCompleted();
                imRealm.close();
            }
        }

        @Override
        public void error(Exception e) {
            imProgressSubject.onError(e);
        }

        @Override
        public void startRead(int totalWords) {
            this.imTotalWorlds = totalWords;
        }
    }

    /**
     * 마이그레이션 이벤트.
     * ld2 파일로부터 데이터를 추출하고 DB 로 옮기는 과정의 프로그래스를 받아올 수 있다.
     */
    public static class MigrationEvent {
        int progress;
        File dbFile;
        boolean isComplete = false;

        private MigrationEvent() {}
        private MigrationEvent(int progress, File dbFile, boolean isComplete) {
            this.progress = progress;
            this.isComplete = isComplete;

        }

        public int getProgress() {
            return progress;
        }
        public boolean isComplete() {
            return isComplete;
        }
        public File getDBFile() {
            return dbFile;
        }


    }



}
