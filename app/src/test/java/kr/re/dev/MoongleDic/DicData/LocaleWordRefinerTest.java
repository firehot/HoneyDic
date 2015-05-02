package kr.re.dev.MoongleDic.DicData;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.Locale;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;

/**
 * Created by ice3x2 on 15. 5. 3..
 */


@RunWith(RobolectricTestRunner.class)
@Config(emulateSdk = 18, manifest = "./app/src/main/AndroidManifest.xml")
public class LocaleWordRefinerTest {

    @Test
    public void testEn() {
        String word = "하하하    히히히   \t\r   test\n にほんご 日本語辞書 toast       trio";
        String result =  LocaleWordRefiner.refine(word, Locale.ENGLISH);
        assertThat("test toast trio", is(result));
    }

    @Test
    public void testEn2() {
        String word = "하하하    히히히   \t\r   \n にほんご 日本語辞書        ";
        String result =  LocaleWordRefiner.refine(word, Locale.ENGLISH);
        assertThat("", is(result));
    }

    @Test
    public void testKo() {
        String word = "하하하 히히히 test にほんご 日本語辞書 toast ㄱㄴㄷㄹㅈ";
        String result =  LocaleWordRefiner.refine(word, Locale.KOREAN);
        assertThat("하하하 히히히 ㄱㄴㄷㄹㅈ", is(result));
    }


    @Test
    public void testJjock() {
        String word = "하하하 히히히 test にほんご 日本語辞書 toast ㄱㄴㄷㄹㅈ";
        String result =  LocaleWordRefiner.refine(word, Locale.JAPANESE);
        assertThat("にほんご 日本語辞書", is(result));

    }


}