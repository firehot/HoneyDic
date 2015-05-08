package kr.re.dev.MoongleDic.UI;


import android.content.Context;
import android.graphics.Rect;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.common.base.Strings;
import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Lists;

import java.util.Collections;
import java.util.List;

import kr.re.dev.MoongleDic.Commons.ViewWrapper;
import kr.re.dev.MoongleDic.DicData.DescriptionCard;
import kr.re.dev.MoongleDic.DicData.WordCard;
import kr.re.dev.MoongleDic.R;


/**
 *  클립보드 영어사전 HoenyDic::DicItemViewWrapper class.
 *  DicItemViewWrapper UI 요소.
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
public class DicItemViewWrapper extends ViewWrapper {

    private LinearLayout mLayoutHorizontalContentBox;
    private TextView mTextViewWord;
    private TextView mTextViewPhonetic;
    private List<MeanItemWrapper> mMeanItemWrapperList = Lists.newArrayList();
    private static String mTouchedWordString = "";
    private String mWord = "";

    public  static String flushThouchedWord() {
        String result = mTouchedWordString;
        mTouchedWordString = "";
        return result;
    }

    public void cleanMeanItems() {
        for(MeanItemWrapper meanItemWrapper :  mMeanItemWrapperList) {
            meanItemWrapper.removeFromParent();
        }
        mMeanItemWrapperList.clear();
    }

    public DicItemViewWrapper(Context context) {
        super(context, R.layout.item_wordcard);
        initViews();
    }


    public void setWordCard(WordCard wordCard) {
        updateFromWordCard(wordCard);

        update();
    }

    private void updateFromWordCard(WordCard wordCard) {
        mWord = wordCard.word();
        mTextViewWord.setText(mWord);
        String phonetic =  wordCard.phonetic();
        if(!phonetic.isEmpty()) {
            mTextViewPhonetic.setVisibility(View.VISIBLE);
            mTextViewPhonetic.setText("[" + wordCard.phonetic() + "]");
        }  else {
            mTextViewPhonetic.setVisibility(View.GONE);
            mTextViewPhonetic.setText("");
        }

        List<DescriptionCard> descriptionCardList = wordCard.getDescriptionCards();
        updateMeanItems(descriptionCardList);
    }

    private void updateMeanItems(List<DescriptionCard> descriptionCards) {
        MeanItemWrapper meanItemWrapper = null;
        Collections.sort(mMeanItemWrapperList);
        int meanItemsSize =  mMeanItemWrapperList.size();
        int i = 0, n = descriptionCards.size();
        while(i < n) {
            if (meanItemsSize <= i) {
                meanItemWrapper = new MeanItemWrapper(getContext());
                mMeanItemWrapperList.add(meanItemWrapper);
                addView(meanItemWrapper);
                ++meanItemsSize;
            } else {
                meanItemWrapper = mMeanItemWrapperList.get(i);
            }
            DescriptionCard descriptionCard = descriptionCards.get(i);
            meanItemWrapper.setClassOfWord(descriptionCard.wordClass()).setMeanOfWord(descriptionCard.meaning());
            meanItemWrapper.getView().setVisibility(View.VISIBLE);
            ++i;
        }
        while(i < meanItemsSize) {
            meanItemWrapper.getView().setVisibility(View.GONE);
            ++i;
        }
    }



    @Override
    public void update() {

    }


    private void initViews() {
        mLayoutHorizontalContentBox = (LinearLayout)findViewById(R.id.linearLayoutHorizontalContentBox);
        mTextViewWord = (TextView)findViewById(R.id.textViewWord);
        mTextViewPhonetic = (TextView)findViewById(R.id.textViewPhonetic);
        mTextViewWord.setOnTouchListener((v,motionEvent) -> {
            if(motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                mTouchedWordString = mWord;
            }
            return false;
        });
    }



}
