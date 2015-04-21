package kr.re.dev.MoogleDic;

public class Call {
	public Class[] mArgsClass = null;
	public Object[] mArguments = null;
	public Object 	mReturn  = null;
	public String  mMethodName = "";
	public Object mTarget = new Object();
	public Call() {
	}
	public Call(Object target, String methodName) {
		mMethodName = methodName;
		mTarget = target;
	}
	public Call(Object target, String methodName, Object...args) {
		mMethodName = methodName;
		mTarget = target;
		if(args != null) {
			mArguments = args;
			mArgsClass = new Class[args.length];
			for(int i = 0, n = args.length; i < n; ++i) {
				if(args[i] == null) mArgsClass[i] = null; 
				else mArgsClass[i] = args[i].getClass();
			}
		}		
	}
	
	public Call(Object target, String methodName, Class[] types, Object[] arguments) {
		mMethodName = methodName;
		mArgsClass = types;
		mArguments = arguments;
		mTarget = target;
	}
	public void setReturnObj(Object obj) {
		mReturn = obj;
	}
	
	public Object getTarget() {
		return mTarget;
	}
	public String getMethodName() {
		return mMethodName;
	}
	public Class[] getTypes() {
		return mArgsClass;
	}
	public Object[] getArguments() {
		return mArguments;
	}
	public Object getReturnData() {
		return mReturn;
	}
}