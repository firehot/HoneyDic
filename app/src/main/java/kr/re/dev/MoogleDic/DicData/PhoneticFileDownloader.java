package kr.re.dev.MoogleDic.DicData;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import kr.re.dev.MoogleDic.Commons.ProgressEvent;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;
import retrofit.http.GET;
import retrofit.http.Path;
import rx.Observable;
import rx.Subscriber;

/**
 * - 음성 파일을 다운받고 이 것을 압축까지 풀어버리는 역할을 담당하게 된다.
 * - 압축된 음성 파일은 External 영역의 Download 폴더에 저장하게 되고, 내부 디렉토리에 압축을 해제한다.
 * - 다운로드 진행 단계 -> 압축 해제 단계의 상태를 파일로 저장하여 도중에 작업이 끊겨도 이어나갈 수 있도록 한다.
 * - 취소 기능을 만들어야 한다.
 * Created by ice3x2 on 15. 4. 27..
 */
public class PhoneticFileDownloader {

    final  static  int ACTION_DOWLOAD = 1;
    final  static  int ACTION_DOWLOAD_COMPLETE = 2;
    final  static  int ACTION_DOWLOAD_FAIL = 3;
    final  static  int ACTION_INSTALL = 4;
    final  static  int ACTION_INSTALL_COMPLETE = 5;
    final  static  int ACTION_INSTALL_FAIL = 6;
    private File mDowloadFile;
    private File mFile;

    public interface AccessPhoneticFile {
        @GET("/{addr}")
        Response access(@Path("addr") String id);
    }




    public static void execute(String addr, Locale locale) {

    }


    public Observable<ProgressEvent> execute() {
        return Observable.create((Observable.OnSubscribe<ProgressEvent>)sub -> {

        });
    }

    private void installPhoneticFile(ProgressEvent event) {
        event.setProgress(0).setAction(ACTION_INSTALL);
    }

    public void doNotCallBecauseForTest() throws IOException {
        unZipFile(null, null, null);
    }


    private void unZipFile(File file,File targetDir, ProgressEvent event) throws IOException {
        ZipEntry zipEntry = null;
        ZipFile zipFile = new ZipFile(file);
        Enumeration<ZipEntry> entrys = (Enumeration<ZipEntry>) zipFile.entries();
        int total = 0;
        int progress = 0;
        while(entrys.hasMoreElements()) {
            zipEntry = entrys.nextElement();
            total += zipFile.getInputStream(zipEntry).available();
        }
        entrys = (Enumeration<ZipEntry>) zipFile.entries();
        while(entrys.hasMoreElements()) {
            zipEntry = entrys.nextElement();
            String fileName = zipEntry.getName();
            File targetFile = new File(targetDir, fileName);
            if(zipEntry.isDirectory()) {
                targetFile.mkdirs();
            } else {
                InputStream is = zipFile.getInputStream(zipEntry);
                progress = progressWrite(is, targetFile, progress, total, event);
            }
        }
    }

    private void downloadPhoneticFile(String addr, ProgressEvent event) {
        Subscriber<ProgressEvent> sub = (Subscriber<ProgressEvent>)event.getTag();
        event.setProgress(0).setAction(ACTION_DOWLOAD);
        sub.onNext(ProgressEvent.obtain(event).setTag(null));
        try {
            Response response = getResponse(addr);
            int total = response.getBody().in().available();
            progressWrite(response.getBody().in(),mDowloadFile, 0, response.getBody().in().available(), event);
        } catch (RetrofitError | IOException e) {
            sub.onNext(ProgressEvent.obtain(e).setAction(ACTION_DOWLOAD_FAIL).setProgress(event.getProgress()).setMax(event.getMax()));
            sub.onError(e);
        }
    }

    private Response getResponse(String addr)  {
        String domain = "";
        String path = "";
        try {
            URL url = new URL(addr);
            path = url.getPath();
            domain = addr.replace(path, "");
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
        RestAdapter restAdapter = new RestAdapter.Builder().setEndpoint(domain).build();
        return restAdapter.create(AccessPhoneticFile.class).access(path);
    }

    private int progressWrite(InputStream inputStream,File targetFile, int start, long total, ProgressEvent event) throws IOException {
        Subscriber<ProgressEvent> sub = (Subscriber<ProgressEvent>)event.getTag();
        FileOutputStream outputStream = new FileOutputStream(targetFile);
        int current = start, readLen = 0;
        byte[] buffer = new byte[10240];
        while((readLen = inputStream.read(buffer)) > 0) {
            outputStream.write(buffer, 0, readLen);
            current += readLen;
            int progress = (int)((float)current / (float)total * (float)event.getMax());
            if(progress != event.getProgress()) {
                event.setProgress(progress);
                sub.onNext(ProgressEvent.obtain(event).setTag(null));
            }
        }
        outputStream.close();
        return current;
    }






}
