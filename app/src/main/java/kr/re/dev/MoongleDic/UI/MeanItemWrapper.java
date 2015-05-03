package kr.re.dev.MoongleDic.UI;

/**
 * Created by ice3x2 on 15. 5. 3..
 */

import android.content.Context;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.TextView;

import kr.re.dev.MoongleDic.Commons.ViewWrapper;
import kr.re.dev.MoongleDic.R;

/**
* 뜻이 각 품사별로 여러개가 있을 수 있다.
* 품사에 맞춰서 뷰를 여러번 생성하는 것은 속도 저하가 있을 수 있으므로 캐쉬할 수 있도록 한다.
*/
public  class MeanItemWrapper extends ViewWrapper {

    private static MeanItemWrapper sRecycledEnd = null;
    private static int sCount = 0;
    private MeanItemWrapper mRecycledPrev;
    private boolean mIsRecycled = false;
    private TextView mTextViewMeaning;
    private TextView mTextViewClass;


    public static MeanItemWrapper obtain(Context context, ViewGroup parentView) {
        MeanItemWrapper meanItemWrapper = null;
        if(sRecycledEnd == null) {
            meanItemWrapper =  new MeanItemWrapper(context);
            Log.d("testio", "MeanViewWrapper is created, count : " + ++sCount);
        }  else {
            meanItemWrapper =  sRecycledEnd;
            meanItemWrapper.mIsRecycled = false;
            sRecycledEnd = sRecycledEnd.mRecycledPrev;
            Log.d("testio", "MeanViewWrapper is obtained, count : " + ++sCount);
        }
        meanItemWrapper.removeFromParent();
        parentView.addView(meanItemWrapper.getView());
        return meanItemWrapper;
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

    public MeanItemWrapper setMeanOfWord(String mean) {
        mTextViewMeaning.setText(mean.replaceAll("^[\\s]{0,}", ""));
        return this;
    }
    public MeanItemWrapper setClassOfWord(String wordClass) {
        mTextViewClass.setText(wordClass);
        return this;
    }


    public MeanItemWrapper(Context context) {
        super(context, R.layout.view_wordmean);
        initViews();
    }

    private void initViews() {
        mTextViewClass = (TextView)findViewById(R.id.textViewWordClass);
        mTextViewMeaning = (TextView)findViewById(R.id.textViewWordMean);

    }


    @Override
    public void update() {}
}
