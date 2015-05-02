package kr.re.dev.MoongleDic.Commons;

import java.util.concurrent.Semaphore;

/**
 * Created by ice3x2 on 15. 4. 21..
 */
public class ProgressEvent {

    private int mAction = 0;
    private int mProgress;
    private int mMax = 100;
    private boolean mIsComplete;
    private boolean mIsRecycled;
    private boolean mIsError = false;
    private boolean mShutdown = false;
    private ProgressEvent mPrev;
    private Throwable mException;
    private Object mTag;
    private static ProgressEvent sEnd;
    private static Semaphore sSemaphore = new Semaphore(1);

    public static ProgressEvent obtain() {
        try {
            sSemaphore.acquire();
        } catch (InterruptedException e1) {
            e1.printStackTrace();
        }
        if (sEnd == null) {
            sSemaphore.release();
            return new ProgressEvent();
        } else {
            ProgressEvent event = sEnd;
            sEnd = sEnd.mPrev;
            sSemaphore.release();
            return event;
        }


    }

    public static ProgressEvent obtain(ProgressEvent event) {
        ProgressEvent nEvent = obtain();
        nEvent.copyFrom(event);
        return nEvent;
    }

    public void copyFrom(ProgressEvent event) {
        this.mProgress = event.mProgress;
        this.mIsComplete = event.mIsComplete;
        this.mIsRecycled = event.mIsRecycled;
        this.mMax = event.mMax;
        this.mPrev = event.mPrev;
        this.mException = event.mException;
        this.mTag = event.mTag;
        this.mIsError = event.mIsError;
        this.mAction = event.mAction;
        this.mShutdown = event.mShutdown;

    }

    public static ProgressEvent obtain(Throwable e) {
        ProgressEvent event = null;
        event = obtain();
        event.setMax(0).setProgress(0).setComplete(false);
        event.mIsError = true;
        event.mException = e;
        return event;
    }

    public static ProgressEvent obtain(int max, int progress, boolean isComplete) {
        ProgressEvent event = null;
        event = obtain();
        event.setMax(max).setProgress(progress).setComplete(isComplete);
        return event;

    }

    private static void reset(ProgressEvent event) {
        event.mProgress = 0;
        event.mIsComplete = false;
        event.mIsRecycled = false;
        event.mMax = 100;
        event.mException = null;
        event.mTag = null;
        event.mIsError = false;
        event.mAction = 0;
        event.mShutdown = false;
    }

    public static void clearResource() {
        while(sEnd != null) {
            sEnd = sEnd.mPrev;
        }
    }

    public void recycle() {
        reset(this);
        mIsRecycled = true;
        try {
            sSemaphore.acquire();
        } catch (InterruptedException e1) {
            e1.printStackTrace();
        }
            mPrev = sEnd;
            sEnd = this;
        sSemaphore.release();
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

    public int getAction() {
        return mAction;
    }
    public ProgressEvent setAction(int action) {
        mAction = action;
        return this;
    }
    public void shutdownIfPossible() {
        mShutdown = true;
    }
    public boolean isShutdownCalled() {
        return mShutdown;
    }



}
