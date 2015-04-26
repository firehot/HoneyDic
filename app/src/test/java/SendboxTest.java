import android.content.Context;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import static junit.framework.Assert.assertEquals;

/**
 * Created by ice3x2 on 15. 4. 25..
 */

@RunWith(RobolectricTestRunner.class)
@Config(emulateSdk = 18, manifest = "./app/src/main/AndroidManifest.xml")
public class SendboxTest {




    @Test
    public void testXMLParsing() throws IOException, SAXException {
        String raw = "<C><F><H><M>teɪk</M></H><I><N><U>n.</U> 잡음, 포획; 잡은 것; 포회량; 수입, 이익 (구어); 중단없이 찍은 영화장면; 한면적에 만들어진 녹음; 성공적인 예방접종</N></I><I><N><U>v.</U> 취하다; 잡다, 포획하다; 움켜쥐다; ...의 영향을 받다; 나르다; 매혹시키다; 소거하다, 빼다; 행하다; 차지하다; 음식을 섭취하다; 타다; 받아 들이다; 느끼다; 받아 적다; 견디다</N></I></F></C>";
        DocumentBuilder documentBuilder = null;
        try {
            documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }

        InputSource is = new InputSource(new StringReader(raw));

        Document document = documentBuilder.parse(is);
        Element element =  document.getDocumentElement();
        NodeList phonetics =  element.getElementsByTagName("M");
        assertEquals(phonetics.getLength(), 1);
        String phonetic = phonetics.item(0).getTextContent();
        System.out.println(phonetic);

        NodeList descriptions =  element.getElementsByTagName("N");
        assertEquals(descriptions.getLength(), 2);

        System.out.println(descriptions.item(0).getChildNodes().item(0).getNodeName());
        System.out.println(descriptions.item(0).getChildNodes().item(1).getNodeName());
        System.out.println(descriptions.item(0).getChildNodes().item(1).getTextContent());


    }

    @Test
    public void testAvailable() throws IOException {

    }

}
