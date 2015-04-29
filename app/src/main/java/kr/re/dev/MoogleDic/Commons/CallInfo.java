package kr.re.dev.MoogleDic.Commons;

import java.lang.ref.WeakReference;


public class CallInfo {
	public Class<?>[] mArgsClass = new Class[0];
	public Object[] mArguments = new Object[0];
	public String  mMethodName = "";
	public WeakReference<Object> mTarget;
	public Class<?> mTargetType;
	private CallInfo() {
	}
	protected CallInfo(Class<?> type, String methodName) {
		mMethodName = methodName;
		mTargetType = type;
		mTarget =  new WeakReference<>(null);
	}
	protected CallInfo(Class<?> type, String methodName, Object... args) {
		mMethodName = methodName;
		mTargetType = type;
		mTarget =  new WeakReference<>(null);
		initArgs(args);
	}
	protected CallInfo(Class<?> type, String methodName, Object arg) {
		mMethodName = methodName;
		mTargetType = type;
		mTarget =  new WeakReference<>(null);
		initArg(arg);
	}
	protected CallInfo(Object target, String methodName) {
		mMethodName = methodName;
		mTarget =  new WeakReference<>(target);
		mTargetType = target.getClass();
	}
	protected CallInfo(Object target, String methodName, Object arg) {
		mMethodName = methodName;
		mTarget =  new WeakReference<>(target);
		mTargetType = target.getClass();
		initArg(arg);
	}
	protected CallInfo(Object target, String methodName, Object... args) {
		mMethodName = methodName;
		mTarget =  new WeakReference<>(target);
		mTargetType = target.getClass();
		initArgs(args);
	}
	private void initArg(Object arg) {
		mArguments = new Object[]{arg};
		if(arg != null) {
			mArgsClass = new Class[]{arg.getClass()};
		} else {
			mArguments = new Class[]{null};
		}
	}

	private void initArgs(Object ... args) {
		if(args != null) {
			mArguments = args;
			mArgsClass = new Class[args.length];
			for(int i = 0, n = args.length; i < n; ++i) {
				if(args[i] == null) mArgsClass[i] = null;
				else mArgsClass[i] = args[i].getClass();
			}
		}
	}

	protected void release() {
		mArgsClass = null;
		mArguments = null;
		mMethodName = null;
		mTarget.clear();
	}

	protected Class<?> getTargetType() {return mTargetType;}
	protected Object getTarget() {
		return mTarget.get();
	}
	protected String getMethodName() {
		return mMethodName;
	}
	protected Class[] getArgumentTypes() {
		return mArgsClass;
	}
	protected Object[] getArguments() {
		return mArguments;
	}

}