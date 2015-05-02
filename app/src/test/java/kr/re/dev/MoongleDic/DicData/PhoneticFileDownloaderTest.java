package kr.re.dev.MoongleDic.DicData;

import android.os.Environment;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.tester.org.apache.http.FakeHttpLayer;

import java.io.File;
import java.io.FileInputStream;

import kr.re.dev.MoongleDic.Commons.ProgressEvent;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static org.mockito.Mockito.*;
import static kr.re.dev.MoongleDic.Commons.Invoker.*;

/**
 *
 * Created by ice3x2 on 15. 4. 27..
 */
@RunWith(RobolectricTestRunner.class)
@Config(emulateSdk = 18, manifest = "./app/src/main/AndroidManifest.xml")
public class PhoneticFileDownloaderTest {

    @Before
    public void setUp() throws Exception {
        Robolectric.getFakeHttpLayer().interceptHttpRequests(false);

    }


    @Test
    public void testDownload() throws Exception {


        File zipFileInPc = new File("./app/src/test/en.zip");
        FileInputStream is = new FileInputStream(zipFileInPc);
        byte[] buffer = new byte[is.available()];
        FakeHttpLayer fakeHttpLayer =  Robolectric.getFakeHttpLayer();

        Robolectric.getFakeHttpLayer().addHttpResponseContent(buffer);
        PhoneticFileDownloader downloader =  new PhoneticFileDownloader();
        rx.Subscriber<ProgressEvent> sub = mock(rx.Subscriber.class);
        ProgressEvent event = new ProgressEvent();
        event.setTag(sub);
        File zipFileInDownload = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "/en.zip");
        zipFileInDownload.delete();
        System.out.println(zipFileInDownload.getAbsolutePath());
        invoke(downloader, "downloadPhoneticFile", "http://google.com/stsefs/sadf/sdaf/en.zip", event);
        verify(sub, times(100)).onNext(anyObject());
        assertTrue(zipFileInDownload.isFile());
        assertEquals(zipFileInDownload.length(), zipFileInPc.length());
    }

    /**
     * UnZip 메소드에 대한 테스트.
     * 압축을 풀고 파일 개수를 확인.
     * 각 파일들이 존재하는지 확인.
     * OnNext 가 100번 호출되는지 확인.(100%)
     * @throws Exception
     */
    /*@Test
    public void testUnZip() throws Exception {
        PhoneticFileDownloader downloader =  new PhoneticFileDownloader();
        rx.Subscriber<ProgressEvent> sub = mock(rx.Subscriber.class);
        ProgressEvent event = new ProgressEvent();
        event.setTag(sub);
        File zipFileInPc = new File("./app/src/test/en.zip");
        File ObjDir =  new File(Robolectric.getShadowApplication().getApplicationContext().getFilesDir().getAbsolutePath() + File.separator +  Locale.ENGLISH);
        assertTrue(ObjDir.getAbsolutePath().matches("[\\w|\\W]*(en)$"));
        File zipFileInDownload = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "/en.zip");
        zipFileInDownload.delete();
        Files.copy(zipFileInPc, zipFileInDownload);
        System.out.println(zipFileInDownload.getAbsolutePath());
        int zipSize = new ZipFile(zipFileInDownload).size();
        invoke(downloader, "unZipFile", zipFileInDownload, ObjDir, event);
        verify(sub, times(100)).onNext(anyObject());
        assertTrue(new File(ObjDir.getAbsolutePath() + "/z/Zarathustra.mp3").isFile());
        assertTrue(new File(ObjDir.getAbsolutePath() + "/z/zarathustra.mp3").isFile());
        assertTrue(new File(ObjDir.getAbsolutePath() + "/f/fireplace.mp3").isFile());
        assertEquals(zipSize, fileCount(ObjDir, 0));
        removeAllFile(ObjDir);
        assertEquals(0, fileCount(ObjDir, 0));
    }*/

    public int fileCount(File file, int startCount) {
        File[] files =  file.listFiles();
        if(files == null) return 0;
        for(File inFile : files) {
            if(inFile.isDirectory()) {
                startCount++;
                startCount = fileCount(inFile, startCount);
            }
            else if(inFile.isFile()) {
                startCount++;
            }
        }
        return startCount;
    }

    public void removeAllFile(File file) {
        File[] files =  file.listFiles();
        for(File inFile : files) {
            if(inFile.isDirectory())
                removeAllFile(inFile);
            else if(inFile.isFile())
                inFile.delete();
        }
        file.delete();
    }




    @Test
    public void testExecute() throws Exception {
        /*String addr = "address";
        Observable<PhoneticFileDownloadeEvent> observable = PhoneticFileDownloader.execute(addr, Locale.ENGLISH);
        observable.subscribeOn(Schedulers.io()).subscribe(e -> {
           if(e.state() == PhoneticFileDownloadeEvent.INSTALL && e.progress() > 10) {
                e.cancel();
           } else {
               Log.i("testio", e.progress());
           }
        });

        Observable<PhoneticFileDownloadeEvent> observable = PhoneticFileDownloader.execute(addr, Locale.ENGLISH);
        observable.subscribeOn(Schedulers.io()).subscribe(e -> {
            if (e.state() != PhoneticFileDownloadeEvent.INSTALL_COMPLETE &&
                    e.state() != PhoneticFileDownloadeEvent.INSTALL) {
                fail();
            } else {
                Log.i("testio", e.progress());
            }
        });


        File dir = PhoneticFileDownloader.dir(Locale.ENGLISH);
        File[] files = dir.listFiles((file, name) -> {
            if (name.equals("z")) return true;
            return false;
        });

        assertEquals(1, files.length);
        assertEquals("z", files[0].getName());*/

    }
}