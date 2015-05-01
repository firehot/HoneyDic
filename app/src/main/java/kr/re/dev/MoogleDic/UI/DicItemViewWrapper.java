package kr.re.dev.MoogleDic.UI;


import android.content.Context;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.common.collect.Lists;
import java.util.List;

import kr.re.dev.MoogleDic.Commons.ViewWrapper;
import kr.re.dev.MoogleDic.DicData.DescriptionCard;
import kr.re.dev.MoogleDic.DicData.WordCard;
import kr.re.dev.MoogleDic.R;

/**
 * DicToast 내부의 단어 뜻을 보여주는 리스트의 아이템 뷰 포함.
 * Created by ice3x2 on 15. 4. 30..
 */
public class DicItemViewWrapper extends ViewWrapper {

    private WordCard mWordCard;

    private static DicItemViewWrapper sRecycledEnd = null;
    private static DicItemViewWrapper sOnEnd = null;
    private DicItemViewWrapper mRecycledPrev;
    private DicItemViewWrapper mOnPrev;
    private boolean mIsRecycled = false;
    private TextView mTextViewWord;
    private TextView mTextViewPhonetic;
    private List<MeanViewWrapper> mMeanViewWrapperList = Lists.newArrayList();

    public static void recycleAll() {
        DicItemViewWrapper dicItemViewWrapper = sOnEnd;
        while(dicItemViewWrapper != null) {
            sOnEnd = dicItemViewWrapper.mOnPrev;
            dicItemViewWrapper.mIsRecycled = true;
            dicItemViewWrapper.recycleMeanViewWrappers();
            DicItemViewWrapper oldRecycled =  sRecycledEnd;
            sRecycledEnd = dicItemViewWrapper;
            sRecycledEnd.mRecycledPrev = oldRecycled;
            dicItemViewWrapper = sOnEnd;
        }
    }



    public static DicItemViewWrapper obtain(Context context) {
        DicItemViewWrapper oldDicItemViewWrapper = sOnEnd;
        if(sRecycledEnd == null) {
            sOnEnd =  new DicItemViewWrapper(context);
        }  else {
            DicItemViewWrapper newDicItem =  sRecycledEnd;
            newDicItem.mIsRecycled = false;
            sRecycledEnd = sRecycledEnd.mRecycledPrev;
            sOnEnd = newDicItem;
        }
        sOnEnd.mOnPrev = oldDicItemViewWrapper;

        return sOnEnd;
    }


    public boolean isRecycled() {
        return mIsRecycled;
    }


    public DicItemViewWrapper(Context context) {
        super(context, R.layout.item_wordcard);

        initViews();
    }

    public void setWordCard(WordCard wordCard) {
        mWordCard = wordCard;
        update();
    }

    @Override
    public void update() {
        recycleMeanViewWrappers();
        mTextViewWord.setText(mWordCard.word());
        mTextViewPhonetic.setText("[" + mWordCard.phonetic() + "]");
        List<DescriptionCard> descriptionCardList = mWordCard.getDescriptionCards();
        for(DescriptionCard descriptionCard : descriptionCardList) {
            MeanViewWrapper meanViewWrapper = MeanViewWrapper.obtain(getContext(), (ViewGroup) getView())
                    .setClassOfWord(descriptionCard.wordClass())
                    .setMeanOfWord(descriptionCard.meaning());
            mMeanViewWrapperList.add(meanViewWrapper);
        }
    }

    private void recycleMeanViewWrappers() {
        for(MeanViewWrapper meanViewWrapper : mMeanViewWrapperList) {
            meanViewWrapper.recycle();
        }
    }

    private void initViews() {
        mTextViewWord = (TextView)findViewById(R.id.textViewWord);
        mTextViewPhonetic = (TextView)findViewById(R.id.textViewPhonetic);
    }


    /**
     * 뜻이 각 품사별로 여러개가 있을 수 있다.
     * 품사에 맞춰서 뷰를 여러번 생성하는 것은 속도 저하가 있을 수 있으므로 캐쉬할 수 있도록 한다.
     */
    public static class MeanViewWrapper extends ViewWrapper {

        private static MeanViewWrapper sRecycledEnd = null;
        private MeanViewWrapper mRecycledPrev;
        private boolean mIsRecycled = false;
        private TextView mTextViewMeaning;
        private TextView mTextViewClass;
        private ViewGroup mParent;

        public static MeanViewWrapper obtain(Context context, ViewGroup parentView) {
            MeanViewWrapper meanViewWrapper = null;
            if(sRecycledEnd == null) {
                meanViewWrapper =  new MeanViewWrapper(context);
            }  else {
                meanViewWrapper =  sRecycledEnd;
                meanViewWrapper.mIsRecycled = false;
                sRecycledEnd = sRecycledEnd.mRecycledPrev;
            }
            meanViewWrapper.setParent(parentView);
            parentView.addView(meanViewWrapper.getView());
            return meanViewWrapper;
        }

        public void recycle() {
            mRecycledPrev = sRecycledEnd;
            sRecycledEnd = this;
            if(mParent != null) {
                mParent.removeView(getView());
            }
            mParent = null;
            mIsRecycled = true;
        }


        public boolean isRecycled() {
            return mIsRecycled;
        }

        public MeanViewWrapper setMeanOfWord(String mean) {
            mTextViewMeaning.setText(mean);
            return this;
        }
        public MeanViewWrapper setClassOfWord(String wordClass) {
            mTextViewClass.setText(wordClass);
            return this;
        }


        public MeanViewWrapper(Context context) {
            super(context, R.layout.view_wordmean);
            initViews();
        }

        private void initViews() {
            mTextViewClass = (TextView)findViewById(R.id.textViewWordClass);
            mTextViewMeaning = (TextView)findViewById(R.id.textViewWordMean);
        }
        private void setParent(ViewGroup view) {
            mParent = view;
        }
        private ViewGroup getParent() {
            return mParent;
        }

        @Override
        public void update() {

        }
    }

}
