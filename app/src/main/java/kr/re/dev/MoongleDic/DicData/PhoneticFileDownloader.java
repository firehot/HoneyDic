package kr.re.dev.MoongleDic.DicData;

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

import kr.re.dev.MoongleDic.Commons.ProgressEvent;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;
import retrofit.http.GET;
import retrofit.http.Path;
import rx.Observable;
import rx.Subscriber;

/**
 *  클립보드 영어사전 HoenyDic::PhoneticFileDownloader class.
 *  텍스트와 지역 정보를 입력받고, 지역에 해당하지 않는 알파벳을 포함하는 단어를 제거하여 반환해준다.
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
