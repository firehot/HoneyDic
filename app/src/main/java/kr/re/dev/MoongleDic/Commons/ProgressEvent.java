package kr.re.dev.MoongleDic.Commons;

import java.util.concurrent.Semaphore;


/**
 *  클립보드 영어사전 HoenyDic::ProgressEvent class. Copyright (C) 2015 ice3x2@gmail.com [https://github.com/ice3x2/HoneyDic]
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
