package kr.re.dev.MoongleDic.DicData;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.List;

import kr.re.dev.MoongleDic.DicData.Database.WordColumns;

import static org.junit.Assert.*;

/**
 * Created by ice3x2 on 15. 4. 26..
 */
@RunWith(RobolectricTestRunner.class)
@Config(emulateSdk = 18, manifest = "./app/src/main/AndroidManifest.xml")
public class WordCardTest {



    @Test
    public void testRawToDescriptionCard() {

        String raw = "<C><F><H><M>teɪk</M></H><I><N><U>n.</U> 잡음, 포획; 잡은 것; 포회량; 수입, 이익 (구어); 중단없이 찍은 영화장면; 한면적에 만들어진 녹음; 성공적인 예방접종</N></I><I><N><U>v.</U> 취하다; 잡다, 포획하다; 움켜쥐다; ...의 영향을 받다; 나르다; 매혹시키다; 소거하다, 빼다; 행하다; 차지하다; 음식을 섭취하다; 타다; 받아 들이다; 느끼다; 받아 적다; 견디다</N></I></F></C>";
        WordColumns wordCol = new WordColumns();
        wordCol.setWord("take");
        wordCol.setDescription(raw);
        WordCard wordCard = WordCard.newInstance(wordCol);
        assertEquals(wordCard.phonetic(), "teɪk");
        assertEquals(wordCard.word(), "take");
        List<DescriptionCard> descriptionCardList = wordCard.getDescriptionCards();
        DescriptionCard card =  descriptionCardList.get(0);
        assertEquals("n.",card.wordClass());
        assertEquals(" 잡음, 포획; 잡은 것; 포회량; 수입, 이익 (구어); 중단없이 찍은 영화장면; 한면적에 만들어진 녹음; 성공적인 예방접종",card.meaning());
        card =  descriptionCardList.get(1);
        assertEquals("v.",card.wordClass());
        assertEquals(" 취하다; 잡다, 포획하다; 움켜쥐다; ...의 영향을 받다; 나르다; 매혹시키다; 소거하다, 빼다; 행하다; 차지하다; 음식을 섭취하다; 타다; 받아 들이다; 느끼다; 받아 적다; 견디다",card.meaning());
        assertEquals(" 취하다; 잡다, 포획하다; 움켜쥐다; ...의 영향을 받다; 나르다; 매혹시키다; 소거하다, 빼다; 행하다; 차지하다; 음식을 섭취하다; 타다; 받아 들이다; 느끼다; 받아 적다; 견디다",card.meaning());
        assertEquals(raw, wordCard.raw());

        /* <C>
            <F>
               <H>
                  <M>teɪk</M>
               </H>
            <I>
                <N>
                    <U>n.</U>
                     잡음, 포획; 잡은 것; 포회량; 수입, 이익 (구어); 중단없이 찍은 영화장면; 한면적에 만들어진 녹음; 성공적인 예방접종
                </N>
            </I>

            <I>
                <N>
                    <U>v.</U>
                    취하다; 잡다, 포획하다; 움켜쥐다; ...의 영향을 받다; 나르다; 매혹시키다; 소거하다, 빼다; 행하다; 차지하다; 음식을 섭취하다; 타다; 받아 들이다; 느끼다; 받아 적다; 견디다
                </N>
            </I>
           </F>
         </C>";*/
    }
}