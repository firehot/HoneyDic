package kr.re.dev.MoongleDic.UI;


import android.content.Context;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.common.collect.Lists;
import java.util.List;

import kr.re.dev.MoongleDic.Commons.ViewWrapper;
import kr.re.dev.MoongleDic.DicData.DescriptionCard;
import kr.re.dev.MoongleDic.DicData.WordCard;
import kr.re.dev.MoongleDic.R;
import rx.subjects.PublishSubject;

/**
 * DicToast 내부의 단어 뜻을 보여주는 리스트의 아이템 뷰 포함.
 * Created by ice3x2 on 15. 4. 30..
 */
public class DicItemViewWrapper extends ViewWrapper  {

    private WordCard mWordCard;
    private static DicItemViewWrapper sRecycledEnd = null;
    private static DicItemViewWrapper sOnEnd = null;
    private static int sCount = 0;
    private DicItemViewWrapper mRecycledPrev;
    private DicItemViewWrapper mOnPrev;
    private boolean mIsRecycled = false;
    private TextView mTextViewWord;
    private TextView mTextViewPhonetic;
    private List<MeanViewWrapper> mMeanViewWrapperList = Lists.newArrayList();
    private PublishSubject<String> mTouchWordSubject;


    public static void recycleAll() {
        DicItemViewWrapper dicItemViewWrapper = sOnEnd;
        while(dicItemViewWrapper != null) {
            sOnEnd = dicItemViewWrapper.mOnPrev;
            dicItemViewWrapper.mIsRecycled = true;
            dicItemViewWrapper.release();
            DicItemViewWrapper oldRecycled =  sRecycledEnd;
            sRecycledEnd = dicItemViewWrapper;
            sRecycledEnd.mRecycledPrev = oldRecycled;
            dicItemViewWrapper = sOnEnd;
            --sCount;
        }
        Log.d("testio", "DicItemViewWrapper is recycled all, count : " + sCount);
    }
    private void release() {
        recycleMeanViewWrappers();
        mWordCard = null;
        mTouchWordSubject = null;
    }


    public static DicItemViewWrapper findWith(WordCard wordCard) {
        DicItemViewWrapper dicItemViewWrapper = sOnEnd;
        while(dicItemViewWrapper != null) {
            if(dicItemViewWrapper.mWordCard != null && wordCard == dicItemViewWrapper.mWordCard) return dicItemViewWrapper;
            dicItemViewWrapper = dicItemViewWrapper.mOnPrev;
        }
        return null;
    }


    public static DicItemViewWrapper obtain(Context context) {
        DicItemViewWrapper oldDicItemViewWrapper = sOnEnd;
        if(sRecycledEnd == null) {
            sOnEnd =  new DicItemViewWrapper(context);
            Log.d("testio", "DicItemViewWrapper is created : " + ++sCount);
        }  else {
            DicItemViewWrapper newDicItem =  sRecycledEnd;
            newDicItem.mIsRecycled = false;
            sRecycledEnd = sRecycledEnd.mRecycledPrev;
            sOnEnd = newDicItem;
            Log.d("testio", "DicItemViewWrapper is obtained : " + ++sCount);
        }
        sOnEnd.mOnPrev = oldDicItemViewWrapper;
        return sOnEnd;
    }

    public void setWordSubject(PublishSubject<String> subject) {
        mTouchWordSubject = subject;
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
        String phonetic =  mWordCard.phonetic();
        if(!phonetic.isEmpty())
            mTextViewPhonetic.setText("[" + mWordCard.phonetic() + "]");
        else
            mTextViewPhonetic.setText("");
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
        mTextViewWord.setOnTouchListener((v,motionEvent) -> {
            if(mTouchWordSubject != null && motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                mTouchWordSubject.onNext(((TextView)v).getText().toString());
            }
            return false;
        });
    }


    /**
     * 뜻이 각 품사별로 여러개가 있을 수 있다.
     * 품사에 맞춰서 뷰를 여러번 생성하는 것은 속도 저하가 있을 수 있으므로 캐쉬할 수 있도록 한다.
     */
    public static class MeanViewWrapper extends ViewWrapper {

        private static MeanViewWrapper sRecycledEnd = null;
        private static int sCount = 0;
        private MeanViewWrapper mRecycledPrev;
        private boolean mIsRecycled = false;
        private TextView mTextViewMeaning;
        private TextView mTextViewClass;


        public static MeanViewWrapper obtain(Context context, ViewGroup parentView) {
            MeanViewWrapper meanViewWrapper = null;
            if(sRecycledEnd == null) {
                meanViewWrapper =  new MeanViewWrapper(context);
                Log.d("testio", "MeanViewWrapper is created, count : " + ++sCount);
            }  else {
                meanViewWrapper =  sRecycledEnd;
                meanViewWrapper.mIsRecycled = false;
                sRecycledEnd = sRecycledEnd.mRecycledPrev;
                Log.d("testio", "MeanViewWrapper is obtained, count : " + ++sCount);
            }
            meanViewWrapper.removeFromParent();
            parentView.addView(meanViewWrapper.getView());
            return meanViewWrapper;
        }

        public void recycle() {
            if(isRecycled()) return;
            mRecycledPrev = sRecycledEnd;
            sRecycledEnd = this;
            removeFromParent();
            mIsRecycled = true;
            Log.d("testio", "MeanViewWrapper is recycled, count : " + --sCount);
        }

        private void removeFromParent() {
            ViewGroup parent = ((ViewGroup) getView().getParent());
            if(parent != null) {
                parent.removeView(getView());
            }
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


        @Override
        public void update() {

        }
    }

}
