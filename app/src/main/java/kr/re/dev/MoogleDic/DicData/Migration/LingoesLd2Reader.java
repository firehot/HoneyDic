package kr.re.dev.MoogleDic.DicData.Migration;

/**
 * Created by ice3x2 on 15. 4. 15..
 */
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.CharBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CoderResult;
import java.nio.charset.CodingErrorAction;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

/**
 * 출처 :: https://github.com/tiancaihb/DangoDict/blob/f2d536930c87ce0203b5295d21ad328e9c44808a/LingoesLd2Reader.java#L21
 * 를 수정하여 사용함.
 *
 * 테스트 완료;;
 *
 *  Copyright (c) 2010 Xiaoyun Zhu
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in
 *  all copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 *  THE SOFTWARE.
 *
 *
 * Lingoes LD2/LDF File Reader
 *
 * <pre>
 * Lingoes Format overview:
 *
 * General Information:
 * - Dictionary data are stored in deflate streams.
 * - Index group information is stored in an index array in the LD2 file itself.
 * - Numbers are using little endian byte order.
 * - Definitions and xml data have UTF-8 or UTF-16LE encodings.
 *
 * LD2 file schema:
 * - File Header
 * - File Description
 * - Additional Information (optional)
 * - Index Group (corresponds to definitions in dictionary)
 * - Deflated Dictionary Streams
 * -- Index Data
 * --- Offsets of definitions
 * --- Offsets of translations
 * --- Flags
 * --- References to other translations
 * -- Definitions
 * -- Translations (xml)
 *
 * TODO: find encoding / language fields to replace auto-detect of encodings
 *
 * </pre>
 *
 * @author keke
 *
 */
public class LingoesLd2Reader {

    private WeakReference<ReadLd2Event> mReadLd2EventRef = null;

    private  final SensitiveStringDecoder[] AVAIL_ENCODINGS = {
            new SensitiveStringDecoder(Charset.forName("UTF-8")),
            new SensitiveStringDecoder(Charset.forName("UTF-16LE")),
            new SensitiveStringDecoder(Charset.forName("UTF-16BE")),
            new SensitiveStringDecoder(Charset.forName("EUC-JP")) };


    public  void readLd2(String ld2File, ReadLd2Event readLd2Event) throws IOException {
        mReadLd2EventRef = new WeakReference<LingoesLd2Reader.ReadLd2Event>(readLd2Event);

        // download from
        // https://skydrive.live.com/?cid=a10100d37adc7ad3&sc=documents&id=A10100D37ADC7AD3%211172#cid=A10100D37ADC7AD3&sc=documents
        // String ld2File = Helper.DIR_IN_DICTS+"\\lingoes\\Prodic English-Vietnamese Business.ld2";
        FileChannel fChannel = new RandomAccessFile(ld2File, "r").getChannel();
        ByteBuffer dataRawBytes = ByteBuffer.allocate((int) fChannel.size());
        fChannel.read(dataRawBytes);
        fChannel.close();
        dataRawBytes.order(ByteOrder.LITTLE_ENDIAN);
        dataRawBytes.rewind();

        //System.out.println("파일：" + ld2File);
        //System.out.println("유형：" + new String(dataRawBytes.array(), 0, 4, "ASCII"));
        //System.out.println("버전：" + dataRawBytes.getShort(0x18) + "." + dataRawBytes.getShort(0x1A));
        //System.out.println("ID: 0x" + Long.toHexString(dataRawBytes.getLong(0x1C)));

        int offsetData = dataRawBytes.getInt(0x5C) + 0x60;
        if (dataRawBytes.limit() > offsetData) {
            // System.out.println("프로필 주소：0x" + Integer.toHexString(offsetData));
            int type = dataRawBytes.getInt(offsetData);
            // System.out.println("소개 유형：0x" + Integer.toHexString(type));
            int offsetWithInfo = dataRawBytes.getInt(offsetData + 4) + offsetData + 12;
            if (type == 3) {
                // without additional information
                readDictionary(ld2File, dataRawBytes, offsetData);
            } else if (dataRawBytes.limit() > offsetWithInfo - 0x1C) {
                readDictionary(ld2File, dataRawBytes, offsetWithInfo);
            } else {
                if(mReadLd2EventRef.get() != null) {
                    mReadLd2EventRef.get().error(new ReadLd2Exception("Not ld2 format."));
                }
                //System.err.println("파일 은 사전 데이터를 포함하지 않습니다. 온라인 사전 ?");
            }
        } else {
            if(mReadLd2EventRef.get() != null) {
                mReadLd2EventRef.get().error(new ReadLd2Exception("Not ld2 format."));
            }
            //System.err.println("파일 은 사전 데이터를 포함하지 않습니다. 온라인 사전 ?");
        }
    }

    private  final long decompress(final String inflatedFile, final ByteBuffer data, final int offset,
                                   final int length, final boolean append) throws IOException {
        Inflater inflator = new Inflater();
        InflaterInputStream in = new InflaterInputStream(new ByteArrayInputStream(data.array(), offset, length),
                inflator, 1024 * 8);
        FileOutputStream out = new FileOutputStream(inflatedFile, append);
        writeInputStream(in, out);
        long bytesRead = inflator.getBytesRead();
        in.close();
        out.close();
        inflator.end();
        return bytesRead;
    }

    private  final SensitiveStringDecoder[] detectEncodings(final ByteBuffer inflatedBytes, final int offsetWords, final int offsetXml, final int defTotal, final int dataLen, final int[] idxData, final String[] defData) throws UnsupportedEncodingException {
        final int test = Math.min(defTotal, 10);
        //Pattern p = Pattern.compile("^.*[\\x00-\\x1f].*$");
        for (int j = 0; j < AVAIL_ENCODINGS.length; j++) {
            for (int k = 0; k < AVAIL_ENCODINGS.length; k++) {
                try {
                    readDefinitionData(inflatedBytes, offsetWords, offsetXml, dataLen, AVAIL_ENCODINGS[j],
                            AVAIL_ENCODINGS[k], idxData, defData, test);
                    //System.out.println("구문 코딩：" + AVAIL_ENCODINGS[j].name);
                    //System.out.println("XML인코딩：" + AVAIL_ENCODINGS[k].name);
                    return new SensitiveStringDecoder[] { AVAIL_ENCODINGS[j], AVAIL_ENCODINGS[k] };
                } catch (Throwable e) {
                    // ignore
                }
            }
        }
        //System.err.println("자동 식별 코드 가 실패 ! 선택 UTF- 16LE 계속 .");
        return new SensitiveStringDecoder[] { AVAIL_ENCODINGS[1], AVAIL_ENCODINGS[1] };
    }

    private  final void extract(final String inflatedFile,
                                final int[] idxArray, final int offsetDefs, final int offsetXml) throws IOException, FileNotFoundException,
            UnsupportedEncodingException {
        //System.out.println("쓰기'" + extractedOutputFile + "'。。。");

        //FileWriter indexWriter = new FileWriter(indexFile);
        //FileWriter defsWriter = new FileWriter(extractedWordsFile);
        //FileWriter xmlWriter = new FileWriter(extractedXmlFile);
        //FileWriter outputWriter = new FileWriter(extractedOutputFile);
        // read inflated data
        FileChannel fChannel = new RandomAccessFile(inflatedFile, "r").getChannel();
        ByteBuffer dataRawBytes = ByteBuffer.allocate((int) fChannel.size());
        fChannel.read(dataRawBytes);
        fChannel.close();
        dataRawBytes.order(ByteOrder.LITTLE_ENDIAN);
        dataRawBytes.rewind();

        final int dataLen = 10;
        final int defTotal = offsetDefs / dataLen - 1;


        //String[] words = new String[defTotal];
        int[] idxData = new int[6];
        String[] defData = new String[2];

        final SensitiveStringDecoder[] encodings = detectEncodings(dataRawBytes, offsetDefs, offsetXml, defTotal,
                dataLen, idxData, defData);

        dataRawBytes.position(8);
        if(mReadLd2EventRef.get() != null) {
            mReadLd2EventRef.get().startRead(defTotal);
        }


        for (int i = 0; i < defTotal; i++) {
            readDefinitionData(dataRawBytes, offsetDefs, offsetXml, dataLen, encodings[0], encodings[1], idxData, defData, i);
            //words[i] = defData[0];

            //System.out.println(defData[0] + " = " + defData[1]);
            dataRawBytes.position(8);
            if(mReadLd2EventRef.get() != null) {
                mReadLd2EventRef.get().word(defData[0], defData[1]);
            }

        }

        /*
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        try {
        	InputStream istream = new ByteArrayInputStream(defData[1].getBytes("utf-8"));
        	Document doc =factory.newDocumentBuilder().parse(istream);
        	Element order = doc.getDocumentElement();
        	NodeList items  = order.getElementsByTagName("I");
        	Node item =  items.item(0);
        	System.out.println(item.getTextContent());
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}*/



        new File(inflatedFile).delete();




        //System.out.println("성공적인 판독" + counter + "/" + defTotal + " 데이터 셋");
    }

    private  final void getIdxData(final ByteBuffer dataRawBytes, final int position, final int[] wordIdxData) {
        dataRawBytes.position(position);
        wordIdxData[0] = dataRawBytes.getInt();
        wordIdxData[1] = dataRawBytes.getInt();
        wordIdxData[2] = dataRawBytes.get() & 0xff;
        wordIdxData[3] = dataRawBytes.get() & 0xff;
        wordIdxData[4] = dataRawBytes.getInt();
        wordIdxData[5] = dataRawBytes.getInt();
    }

    private  final void inflate(final ByteBuffer dataRawBytes, final List<Integer> deflateStreams, final String inflatedFile) {
        //System.out.println("감압'" + deflateStreams.size() + "'데이터 스트림'" + inflatedFile + "'。。。");
        int startOffset = dataRawBytes.position();
        int offset = -1;
        int lastOffset = startOffset;
        boolean append = false;
        try {
            for (Integer offsetRelative : deflateStreams) {
                offset = startOffset + offsetRelative.intValue();
                decompress(inflatedFile, dataRawBytes, lastOffset, offset - lastOffset, append);
                append = true;
                lastOffset = offset;
            }
        } catch (Throwable e) {
            System.err.println("압축 해제 실패 : 0x" + Integer.toHexString(offset) + ": " + e.toString());
        }
    }

    private  final void readDefinitionData(final ByteBuffer inflatedBytes, final int offsetWords,
                                           final int offsetXml, final int dataLen, final SensitiveStringDecoder wordStringDecoder,
                                           final SensitiveStringDecoder xmlStringDecoder, final int[] idxData, final String[] defData, final int i)
            throws UnsupportedEncodingException {
        getIdxData(inflatedBytes, dataLen * i, idxData);
        int lastWordPos = idxData[0];
        int lastXmlPos = idxData[1];
        final int flags = idxData[2];
        int refs = idxData[3];
        int currentWordOffset = idxData[4];
        int currenXmlOffset = idxData[5];

        //String xml = strip(new String(xmlStringDecoder.decode(inflatedBytes.array(), offsetXml + lastXmlPos, currenXmlOffset - lastXmlPos)));
        String xml = new String(xmlStringDecoder.decode(inflatedBytes.array(), offsetXml + lastXmlPos, currenXmlOffset - lastXmlPos));
        defData[1] = xml;
        while (refs-- > 0) {
            int ref = inflatedBytes.getInt(offsetWords + lastWordPos);
            getIdxData(inflatedBytes, dataLen * ref, idxData);
            lastXmlPos = idxData[1];
            currenXmlOffset = idxData[5];
            if (xml.isEmpty()) {
                //xml = strip(new String(xmlStringDecoder.decode(inflatedBytes.array(), offsetXml + lastXmlPos, currenXmlOffset - lastXmlPos)));
                xml = new String(xmlStringDecoder.decode(inflatedBytes.array(), offsetXml + lastXmlPos, currenXmlOffset - lastXmlPos));
                defData[1] = xml;
            } else {
                xml = new String(xmlStringDecoder.decode(inflatedBytes.array(), offsetXml + lastXmlPos, currenXmlOffset - lastXmlPos)) + ", " + xml;
            }
            lastWordPos += 4;
        }
        String word = new String(wordStringDecoder.decode(inflatedBytes.array(), offsetWords + lastWordPos,currentWordOffset - lastWordPos));
        defData[0] = word;
    }

    private  final void readDictionary(final String ld2File, final ByteBuffer dataRawBytes,
                                       final int offsetWithIndex) throws IOException, FileNotFoundException, UnsupportedEncodingException {
        //System.out.println("사전 유형：0x" + Integer.toHexString(dataRawBytes.getInt(offsetWithIndex)));
        int limit = dataRawBytes.getInt(offsetWithIndex + 4) + offsetWithIndex + 8;
        int offsetIndex = offsetWithIndex + 0x1C;
        int offsetCompressedDataHeader = dataRawBytes.getInt(offsetWithIndex + 8) + offsetIndex;
        int inflatedWordsIndexLength = dataRawBytes.getInt(offsetWithIndex + 12);
        int inflatedWordsLength = dataRawBytes.getInt(offsetWithIndex + 16);
        int inflatedXmlLength = dataRawBytes.getInt(offsetWithIndex + 20);
        int definitions = (offsetCompressedDataHeader - offsetIndex) / 4;
        List<Integer> deflateStreams = new ArrayList<Integer>();
        dataRawBytes.position(offsetCompressedDataHeader + 8);
        int offset = dataRawBytes.getInt();
        while (offset + dataRawBytes.position() < limit) {
            offset = dataRawBytes.getInt();
            deflateStreams.add(Integer.valueOf(offset));
        }
        int offsetCompressedData = dataRawBytes.position();
        //System.out.println("문구 의 색인 번호：" + definitions);
        //System.out.println("인덱스 주소 / 크기：0x" + Integer.toHexString(offsetIndex) + " / " + (offsetCompressedDataHeader - offsetIndex) + " B");
        //System.out.println("압축 된 데이터 의 주소 / 크기：0x" + Integer.toHexString(offsetCompressedData) + " / " + (limit - offsetCompressedData) + " B");
        //System.out.println("구문 인덱스 주소 / 크기 ( 압축 해제 후) ：0x0 / " + inflatedWordsIndexLength + " B");
        //System.out.println("구 주소 / 크기 ( 압축 해제 후)：0x" + Integer.toHexString(inflatedWordsIndexLength) + " / " + inflatedWordsLength + " B");
        //System.out.println("XML 주소 / 크기 ( 압축 해제 후)：0x" + Integer.toHexString(inflatedWordsIndexLength + inflatedWordsLength) + " / " + inflatedXmlLength + " B");
        //System.out.println("파일 크기 ( 압축 )：" + (inflatedWordsIndexLength + inflatedWordsLength + inflatedXmlLength) / 1024 + " KB");
        String inflatedFile = ld2File + ".inflated";
        inflate(dataRawBytes, deflateStreams, inflatedFile);

        if (new File(inflatedFile).isFile()) {

            dataRawBytes.position(offsetIndex);
            int[] idxArray = new int[definitions];
            for (int i = 0; i < definitions; i++) {
                idxArray[i] = dataRawBytes.getInt();
            }
            extract(inflatedFile,  idxArray, inflatedWordsIndexLength, inflatedWordsIndexLength + inflatedWordsLength);
        }
    }

    private  final String strip(final String xml) {
        int open = 0;
        int end = 0;
        if(xml.isEmpty()) {
            System.out.println("XML is Empty");
            return "XML is Empty";
        }


        if ((open = xml.indexOf("<![CDATA[")) != -1) {
            if ((end = xml.indexOf("]]>", open)) != -1) {
                return xml.substring(open + "<![CDATA[".length(), end).replace('\t', ' ').replace('\n', ' ')
                        .replace('\u001e', ' ').replace('\u001f', ' ');
            }
        } else if ((open = xml.indexOf("<?")) != -1) {
            if ((end = xml.indexOf("</?", open)) != -1) {
                open = xml.indexOf(">", open + 1);
                return xml.substring(open + 1, end).replace('\t', ' ').replace('\n', ' ').replace('\u001e', ' ')
                        .replace('\u001f', ' ');
            }
        } else {
            StringBuilder sb = new StringBuilder();
            end = 0;
            open = xml.indexOf('<');
            do {
                if (open - end > 1) {
                    sb.append(xml.substring(end + 1, open));
                }
                open = xml.indexOf('<', open + 1);
                end = xml.indexOf('>', end + 1);
            } while (open != -1 && end != -1);
            return sb.toString().replace('\t', ' ').replace('\n', ' ').replace('\u001e', ' ').replace('\u001f', ' ');
        }
        return "";
    }

    private  final void writeInputStream(final InputStream in, final OutputStream out) throws IOException {
        byte[] buffer = new byte[1024 * 8];
        int len;
        while ((len = in.read(buffer)) > 0) {
            out.write(buffer, 0, len);
        }
    }

    private  class SensitiveStringDecoder {
        public final String name;
        private final CharsetDecoder cd;

        private SensitiveStringDecoder(Charset cs) {
            this.cd = cs.newDecoder().onMalformedInput(CodingErrorAction.REPORT)
                    .onUnmappableCharacter(CodingErrorAction.REPORT);
            this.name = cs.name();
        }

        char[] decode(byte[] ba, int off, int len) {
            int en = (int) (len * (double) cd.maxCharsPerByte());
            char[] ca = new char[en];
            if (len == 0)
                return ca;
            cd.reset();
            ByteBuffer bb = ByteBuffer.wrap(ba, off, len);
            CharBuffer cb = CharBuffer.wrap(ca);
            try {
                CoderResult cr = cd.decode(bb, cb, true);
                if (!cr.isUnderflow()) {
                    cr.throwException();
                }
                cr = cd.flush(cb);
                if (!cr.isUnderflow()) {
                    cr.throwException();
                }
            } catch (CharacterCodingException x) {
                // Substitution is always enabled,
                // so this shouldn't happen
                throw new Error(x);
            }
            return safeTrim(ca, cb.position());
        }

        private char[] safeTrim(char[] ca, int len) {
            if (len == ca.length) {
                return ca;
            } else {
                return Arrays.copyOf(ca, len);
            }
        }
    }

    public static interface ReadLd2Event {
        void word(String word, String xml);
        void error(Exception e);
        void startRead(int totalWords);
    }

    public static class ReadLd2Exception extends Exception {
        private ReadLd2Exception(String msg) {
            super(msg);
        }
    }
}
/*

<C>
	<F>
		<H>
			<M>an·y || 'enɪ</M>
		</H>
		<I>
			<N>
				<U>adj.</U>
					어느, 어떤; 모든
			</N>
		</I>
		<I>
			<N>
				<U>adv.</U>
				 	조금도; 전혀
			</N>
		</I>
		<I>
			<N>
				<U>pron.</U>
				어떤
			</N>
		</I>
	</F>
</C>                                                                                                                                          
any = an·y || 'enɪadj. 어느, 어떤; 모든adv. 조금도; 전혀pron. 어떤

*/