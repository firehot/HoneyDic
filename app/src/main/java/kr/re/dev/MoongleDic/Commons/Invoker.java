package kr.re.dev.MoongleDic.Commons;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * CallInfo class
 * private 메소드를 호출해주는 모듈중 일부.   Copyright (C)  2015 ice3x2@gmail.com
 * 사용법 : http://www.dev.re.kr/77
 */
public class Invoker {

	public static Object invokeStatic(Class<?> type,String methodName) throws Exception {
		return invoke(new CallInfo(type, methodName));
	}
	public static Object invokeStatic(Class<?> type,String methodName, Object arg) throws Exception {
		return invoke(new CallInfo(type, methodName,arg));
	}

	public static Object invokeStatic(Class<?> type,String methodName, Object ... args) throws Exception {
		return invoke(new CallInfo(type, methodName,args));
	}

	public static Object invoke(Object target,String methodName) throws Exception {
		return invoke(new CallInfo(target, methodName));
	}

	public static Object invoke(Object target,String methodName, Object arg) throws Exception {
		return invoke(new CallInfo(target, methodName, arg));
	}

	public static Object invoke(Object target,String methodName, Object ... args) throws Exception {
		return invoke(new CallInfo(target, methodName, args));
	}

	private static Object invoke(CallInfo callInfo) throws Exception {
		Class<?> type = callInfo.getTargetType();
		ArrayList<MethodScore> methodScores = new ArrayList<>();
		List<Method> allMethods = getAllMethods(type);

		for(Method method: allMethods) {
			MethodScore methodScore =  getMethodScore(method, callInfo);
			if(methodScore != null) methodScores.add(methodScore);
		}
		if(methodScores.size() == 0) {
			throw new NoSuchMethodException(callInfo.getMethodName() + " is not found.");
		}
		Method method = getMaxScoreMethod(methodScores);
		return  runInvoke(method, callInfo);
	}

	private static List<Method> getAllMethods(Class<?> type) {
		ArrayList<Method> methods = new ArrayList<>();
		do {
			Method[]  declaredMethods = type.getDeclaredMethods();
			Collections.addAll(methods, declaredMethods);
			type = type.getSuperclass();
		} while(type != null);
		return methods;
	}

	private static MethodScore getMethodScore(Method method, CallInfo callInfo) {
		if(method.getName().equals(callInfo.getMethodName())) {
			Class[] types =  method.getParameterTypes();
			int typesIndex = 0;
			int typesA = types.length;
			int typesN = callInfo.getArgumentTypes().length;
			if(typesN != typesA) return null;
			boolean searchOK = true;
			MethodScore methodScore = new MethodScore(method);
			while(typesIndex < typesN) {
				Class<?> methodArgsType = convertPrimitiveTypeToWrapperType(types[typesIndex]);
				Class<?>  inArgsType = convertPrimitiveTypeToWrapperType(callInfo.getArgumentTypes()[typesIndex]);
				if(methodArgsType.equals(inArgsType)) {
					methodScore.increaseScore();
				} else {
					while (inArgsType != null && !methodArgsType.equals(inArgsType)) {
						inArgsType = inArgsType.getSuperclass();
						methodScore.decreaseScore();
					}
				}
				if(inArgsType == null) {
					searchOK = false;
					break;
				}
				typesIndex++;
			}
			if(searchOK) {
				return methodScore;
			}
		}
		return null;
	}

	private static Method getMaxScoreMethod(List<MethodScore> methodScores) {
		MethodScore maxScore = methodScores.get(0);
		for(MethodScore methodScore : methodScores) {
			if(methodScore.getScore() > maxScore.getScore()) {
				maxScore = methodScore;
			}
		}
		return maxScore.getMethod();
	}

	private static Object runInvoke(Method method, CallInfo callInfo) throws InvocationTargetException, IllegalAccessException {
		boolean accessible = method.isAccessible();
		method.setAccessible(true);
		Object obj = method.invoke(callInfo.getTarget(), callInfo.getArguments());
		method.setAccessible(accessible);
		callInfo.release();
		return obj;
	}

	
	private static Class convertPrimitiveTypeToWrapperType(Class classType) {
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

	private static class MethodScore {
		private MethodScore(Method method) {
			this.method = method;
		}
		private int getScore() {
			return this.score;
		}
		private void increaseScore() {
			++this.score;
		}
		private void decreaseScore() {
			--this.score;
		}
		private Method getMethod() {
			return this.method;
		}

		Method method = null;
		int score = 0;
	}
}
