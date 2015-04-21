package kr.re.dev.MoogleDic;

import java.lang.reflect.Method;
import java.util.ArrayList;

import android.os.Handler;
import android.os.Message;


public class SimpleHandler extends Handler{
	private static SimpleHandler msHandler = new SimpleHandler();
	final public static int MSG_CALL_METHOD = -9999;
	
	public static void initHandler() {
		msHandler = new SimpleHandler();
	}
	public static  void releaseHanlder() {
		msHandler = null;
	}
	public void dispatchMessage(android.os.Message msg) {
		if(msg.what == MSG_CALL_METHOD) {
			
			Call call =  (Call)msg.obj;
			try {
				
				
				ArrayList<Method> listAllMethod = new ArrayList<Method>();
				Method[] methods =  call.getTarget().getClass().getMethods();
				//Method[] declaredMethods =  mca.getTarget().getClass().getDeclaredMethods();
				
				for(Method method: methods) listAllMethod.add(method);
				//for(Method method: declaredMethods) listAllMethod.add(method);
				
				for(Method method: listAllMethod) {
					if(method.getName().equals(call.getMethodName())) {
				
						Class[] types =  method.getParameterTypes();
						
						int typesIndex = 0;
						int typesN = (call.getTypes() != null)?call.getTypes().length:0;
						boolean searchOK = true;
						while(typesIndex < typesN) {
							
							if(call.getTypes()[typesIndex] == null) {}
							else if(!defaultToObject(call.getTypes()[typesIndex])
									.equals(defaultToObject(types[typesIndex]))) {
								searchOK = false;
								break;
							}
							
							++typesIndex;
						}
						
						if(searchOK)  call.setReturnObj(method.invoke(call.getTarget(), call.getArguments()));
						
					}
				}
				
			} catch (Exception e) {
				e.printStackTrace();
				
			}
		}
		super.dispatchMessage(msg);
	};
	
	private static Class defaultToObject(Class classType) {
		
		if(classType.equals(int.class)) classType = Integer.class;
		else if(classType.equals(float.class)) classType = Float.class;
		else if(classType.equals(boolean.class))classType = Boolean.class;
		else if(classType.equals(byte.class))classType = Byte.class;
		else if(classType.equals(short.class))classType = Short.class;
		else if(classType.equals(long.class))classType = Long.class;
		else if(classType.equals(double.class))classType = Double.class;
		else if(classType.equals(char.class))classType = Character.class;
		
		return classType;
		
	}

	public static Object run(Call call) {
		try {
			ArrayList<Method> listAllMethod = new ArrayList<Method>();
			Method[] methods =  call.getTarget().getClass().getMethods();
			//Method[] declaredMethods =  mca.getTarget().getClass().getDeclaredMethods();

			for(Method method: methods) listAllMethod.add(method);
			//for(Method method: declaredMethods) listAllMethod.add(method);

			for(Method method: listAllMethod) {
				if(method.getName().equals(call.getMethodName())) {

					Class[] types =  method.getParameterTypes();

					int typesIndex = 0;
					int typesN = (call.getTypes() != null)?call.getTypes().length:0;
					boolean searchOK = true;
					while(typesIndex < typesN) {
						if(call.getTypes()[typesIndex] == null) {}
						else if(!defaultToObject(call.getTypes()[typesIndex])
								.equals(defaultToObject(types[typesIndex]))) {
							searchOK = false;
							break;
						}

						++typesIndex;
					}

					if(searchOK) {
						call.setReturnObj(method.invoke(call.getTarget(), call.getArguments()));
						return call.getReturnData();
					}

				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static void call(Call arg) {
		Message msg = msHandler.obtainMessage();
		msg.what = MSG_CALL_METHOD;
		msg.obj  = arg;
		msHandler.sendMessage(msg);
	}
	
}
