package kr.re.dev.MoogleDic.Commons;

/**
 * Created by ice3x2 on 15. 4. 21..
 */
public class ProgressEvent {

    private int mProgress;
    private boolean mIsComplete;
    private boolean mIsRecycled;
    private int mMax = 100;
    private static ProgressEvent sEnd;
    private ProgressEvent mPrev;
    private Throwable mException;
    private Object mTag;
    private boolean mIsError = false;


    public static ProgressEvent obtain() {
        if(sEnd == null) {
            return  new ProgressEvent();
        } else {
            ProgressEvent event = sEnd;
            sEnd = sEnd.mPrev;
            return event;
        }
    }

    public static ProgressEvent obtain(Throwable e) {
        ProgressEvent event =  obtain();
        event.setMax(0).setProgress(0).setComplete(false);
        event.mIsError = true;
        event.mException = e;
        return event;
    }

    public static ProgressEvent obtain(int max, int progress, boolean isComplete) {
        ProgressEvent event =  obtain();
        event.setMax(max).setProgress(progress).setComplete(isComplete);
        return event;
    }

    public static void clearResource() {
        while(sEnd != null) {
            sEnd = sEnd.mPrev;
        }
    }

    public void recycle() {
        mIsRecycled = true;
        mPrev = sEnd;
        sEnd = this;
    }

    public boolean isRecycled() {
        return mIsRecycled;
    }

    public ProgressEvent setProgress(int progress) {
        mProgress = progress;
        return this;
    }
    public boolean isError() {
         return mIsError;
    }
    public Throwable getThrowable() {
        return mException;
    }
    public ProgressEvent setMax(int max) {
        mMax = max;
        return this;
    }
    public ProgressEvent setComplete(boolean complete) {
        mIsComplete = complete;
        return this;
    }
    public int getMax() {
        return mMax;
    }
    public int getProgress() {
        return mProgress;
    }
    public boolean isComplete() {
        return mIsComplete;
    }
    public ProgressEvent setTag(Object tag) {
        this.mTag = tag;
        return this;
    }
    public Object getTag() {
        return mTag;
    }




}
