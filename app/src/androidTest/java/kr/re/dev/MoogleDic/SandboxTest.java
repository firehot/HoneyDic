package kr.re.dev.MoogleDic;

import android.app.Application;
import android.content.Context;
import android.test.ApplicationTestCase;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import io.realm.Realm;
import io.realm.RealmList;
import kr.re.dev.MoogleDic.DicData.Database.KeyWordColumns;
import kr.re.dev.MoogleDic.DicData.Database.WordColumns;

/**
 * Created by ice3x2 on 15. 4. 22..
 */
public class SandboxTest extends ApplicationTestCase<Application> {

    public SandboxTest() {
        super(Application.class);

    }




    public void testAvailable() throws IOException {

        String DBName = "Vicon English-Korean Dictionary.ld2";
        copyFromAssets(getContext(), DBName, getContext().getFilesDir());
        Realm realm = Realm.getInstance(getContext(),DBName);

        KeyWordColumns keyWordColumns = realm.where(KeyWordColumns.class).equalTo("keyword","boxing", false).findFirst();
        assertTrue(keyWordColumns.isBase());
        assertEquals(keyWordColumns.getDicName(), DBName);
        assertEquals(keyWordColumns.getRefs(), 1);
        RealmList<WordColumns> wordColumnses = keyWordColumns.getWords();
        assertEquals(wordColumnses.size(), 2);
        assertEquals(wordColumnses.get(0).getWord(), "boxing");
        assertEquals(wordColumnses.get(0).getDescription(), "<C><F><H><M>box·ing || 'bɒksɪŋ</M></H><I><N><U>n.</U> 권투, 두사람이 치고 받는 운동</N></I></F></C>");
        assertEquals(wordColumnses.get(1).getWord(), "box");
        assertEquals(wordColumnses.get(1).getDescription(), "<C><F><H><M>bɒks</M></H><I><N><U>n.</U> 통; 상자; 칸막이한 좌석; 손바닥으로 침, 주먹으로 침;텔레비젼</N></I><I><N><U>v.</U> 주먹질 하며 싸우다; 박스에 넣다</N></I></F></C>");
    }

    private File copyFromAssets(Context context, String fromName, File toDirs) throws IOException {
        InputStream is = null;
        is = context.getAssets().open(fromName);
        File tmpFile = new File(toDirs, fromName);
        if(tmpFile.isFile()) {
            return tmpFile;
        }
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


}
