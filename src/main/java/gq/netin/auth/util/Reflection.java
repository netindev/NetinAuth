package gq.netin.auth.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;

/**
 *
 * @author netindev
 *
 */
public final class Reflection {

	public static Object newInstance(Class<?> clazz, Object... param) {
		try {
			final Class<?>[] clazzArr = new Class[2];
			for (int i = 0; i < 2; i++) {
				clazzArr[i] = param[i].getClass();
			}
			return Reflection.getConstructor(clazz, clazzArr).newInstance(param);
		} catch (final IllegalArgumentException | InstantiationException | IllegalAccessException
				| InvocationTargetException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static Object getField(String fieldName, Object value, int iteration) {
		try {
			return Reflection.declaredField(fieldName, value.getClass(), iteration).get(value);
		} catch (final IllegalArgumentException | IllegalAccessException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static void setField(String fieldName, Object value, Object clazz, int iteration) {
		try {
			Reflection.declaredField(fieldName, clazz.getClass(), iteration).set(clazz, value);
		} catch (final IllegalArgumentException | IllegalAccessException e) {
			e.printStackTrace();
		}
	}

	private static Constructor<?> getConstructor(Class<?> clazz, Class<?>... clazzArr) {
		try {
			final Constructor<?> declaredConstructor = clazz.getDeclaredConstructor(clazzArr);
			declaredConstructor.setAccessible(true);
			return declaredConstructor;
		} catch (final SecurityException | NoSuchMethodException e) {
			e.printStackTrace();
		}
		return null;
	}

	private static Field declaredField(String fieldName, Class<?> clazz, int iteration) {
		try {
			final Field declaredField = Reflection.superclassIteration(clazz, iteration).getDeclaredField(fieldName);
			Reflection.setPrivateField(declaredField);
			return declaredField;
		} catch (final NoSuchFieldException e) {
			e.printStackTrace();
		}
		return null;
	}

	private static Class<?> superclassIteration(Class<?> clazz, int toRepeat) {
		for (int i = 0; i < toRepeat; i++) {
			clazz = clazz.getSuperclass();
		}
		return clazz;
	}

	private static void setPrivateField(Field field) {
		field.setAccessible(true);
		if (Modifier.isFinal(field.getModifiers())) {
			try {
				final Field modifierField = Field.class.getDeclaredField("modifiers");
				modifierField.setAccessible(true);
				modifierField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
			} catch (final NoSuchFieldException | IllegalArgumentException | IllegalAccessException e) {
				e.printStackTrace();
			}
		}
	}

}
