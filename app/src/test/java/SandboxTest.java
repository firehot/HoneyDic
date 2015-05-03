
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.io.IOException;
import retrofit.client.Response;
import retrofit.http.GET;
import retrofit.http.Headers;
import retrofit.http.Path;


/**
 * Created by ice3x2 on 15. 4. 25..
 *
 *
 */

@RunWith(RobolectricTestRunner.class)
@Config(emulateSdk = 18, manifest = "./app/src/main/AndroidManifest.xml")
public class SandboxTest {


    public interface DownloadTest {
        @GET("/{addr}")
        @Headers({"User-Agent: Mozilla/5.0 (Windows; U; Windows NT 6.1; ko; rv:1.9.2.8) Gecko/20100722 Firefox/3.6.8 IPMS/A640400A-14D460801A1-000000426571"})
        //@Headers({"Content-Type: image/jpeg"})
        Response getImageFile(@Path("addr") String id);
    }




    @Test
    public void testAvailable() throws IOException, InterruptedException {


            System.out.println("http://dsfsdafasd.asdfsadf.asdfasdf".contains("://"));




//        CountDownLatch latch = new CountDownLatch(1);
//        Robolectric.getFakeHttpLayer().interceptHttpRequests(false);
//        //Robolectric.addPendingHttpResponse(200, "OK");
//
//        RestAdapter restAdapter = new RestAdapter.Builder().setEndpoint("https://docs.google.com").build();
//        DownloadTest downloadTest = restAdapter.create(DownloadTest.class);
//        Response ob = null;
//        try {
//            ob = downloadTest.getImageFile("uc?authuser=0&id=0BxFGvh-2Lj3TM1pjSjFGTnQ2d2c&export=download");
//        } catch (RetrofitError e) {
//            return;
//        }
//
//        System.out.println("start!");
//
//            System.out.println(ob.getStatus());
//            try {
//                InputStream is = ob.getBody().in();
//                int len = 0;
//                byte[] buffer = new byte[10024];
//                File file = new File("/Users/ice3x2/realm.db");
//                FileOutputStream fos = new FileOutputStream(file);
//                int count = 0;
//                while ((len = is.read(buffer)) > 0) {
//                    fos.write(buffer, 0, len);
//                    count += len;
//                    System.out.println(count);
//                }
//                System.out.println("end");
//                latch.countDown();
//            } catch (Exception e) {
//                System.out.println(e.toString());
//                e.printStackTrace();
//                latch.countDown();
//                fail();
//            }
//
//        latch.await();

    }

}
