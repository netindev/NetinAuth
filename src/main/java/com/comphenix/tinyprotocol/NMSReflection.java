package com.comphenix.tinyprotocol;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;

/**
 * An utility class that simplifies reflection in Bukkit plugins.
 *
 * @author Kristian
 */
public final class NMSReflection {

	public interface MethodInvoker {
		public Object invoke(Object target, Object... arguments);
	}

	public interface FieldAccessor<T> {
		public T get(Object target);

		public void set(Object target, Object vazlue);

		public boolean hasField(Object target);
	}

	private static final String OBC_PREFIX = Bukkit.getServer().getClass().getPackage().getName();
	private static final String NMS_PREFIX = NMSReflection.OBC_PREFIX.replace("org.bukkit.craftbukkit",
			"net.minecraft.server");
	private static final String VERSION = NMSReflection.OBC_PREFIX.replace("org.bukkit.craftbukkit", "").replace(".",
			"");

	private static final Pattern MATCH_VARIABLE = Pattern.compile("\\{([^\\}]+)\\}");

	public static <T> FieldAccessor<T> getField(String className, String name, Class<T> fieldType) {
		return NMSReflection.getField(NMSReflection.getClass(className), name, fieldType, 0);
	}

	public static <T> FieldAccessor<T> getField(Class<?> target, Class<T> fieldType, int index) {
		return NMSReflection.getField(target, null, fieldType, index);
	}

	public static <T> FieldAccessor<T> getField(String className, Class<T> fieldType, int index) {
		return NMSReflection.getField(NMSReflection.getClass(className), fieldType, index);
	}

	private static <T> FieldAccessor<T> getField(Class<?> target, String name, Class<T> fieldType, int index) {
		for (final Field field : target.getDeclaredFields()) {
			if ((name == null || field.getName().equals(name)) && fieldType.isAssignableFrom(field.getType())
					&& index-- <= 0) {
				field.setAccessible(true);

				return new FieldAccessor<T>() {
					@SuppressWarnings("unchecked")
					@Override
					public T get(Object target) {
						try {
							return (T) field.get(target);
						} catch (final IllegalAccessException e) {
							throw new RuntimeException("Reflection field error.", e);
						}
					}

					@Override
					public void set(Object target, Object value) {
						try {
							field.set(target, value);
						} catch (final IllegalAccessException e) {
							throw new RuntimeException("Reflection field set error.", e);
						}
					}

					@Override
					public boolean hasField(Object target) {
						return field.getDeclaringClass().isAssignableFrom(target.getClass());
					}
				};
			}
		}

		if (target.getSuperclass() != null) {
			return NMSReflection.getField(target.getSuperclass(), name, fieldType, index);
		}
		throw new IllegalArgumentException("Reflection field error." + fieldType);
	}

	public static MethodInvoker getMethod(String className, String methodName, Class<?>... params) {
		return NMSReflection.getTypedMethod(NMSReflection.getClass(className), methodName, null, params);
	}

	public static MethodInvoker getMethod(Class<?> clazz, String methodName, Class<?>... params) {
		return NMSReflection.getTypedMethod(clazz, methodName, null, params);
	}

	public static MethodInvoker getTypedMethod(Class<?> clazz, String methodName, Class<?> returnType,
			Class<?>... params) {
		for (final Method method : clazz.getDeclaredMethods()) {
			if ((methodName == null || method.getName().equals(methodName)) && returnType == null
					|| method.getReturnType().equals(returnType) && Arrays.equals(method.getParameterTypes(), params)) {

				method.setAccessible(true);
				return (target, arguments) -> {
					try {
						return method.invoke(target, arguments);
					} catch (final Exception e) {
						throw new RuntimeException("Reflection method invoke error." + method, e);
					}
				};
			}
		}
		if (clazz.getSuperclass() != null) {
			return NMSReflection.getMethod(clazz.getSuperclass(), methodName, params);
		}
		throw new IllegalStateException(
				String.format("Reflection method error. %s (%s).", methodName, Arrays.asList(params)));
	}

	/* getConstructor unused */

	public static Class<?> getUntypedClass(String lookupName) {
		final Class<?> clazz = NMSReflection.getClass(lookupName);
		return clazz;
	}

	public static Class<?> getClass(String lookupName) {
		return NMSReflection.getCanonicalClass(NMSReflection.expandVariables(lookupName));
	}

	public static Class<?> getMinecraftClass(String name) {
		return NMSReflection.getCanonicalClass(NMSReflection.NMS_PREFIX + "." + name);
	}

	private static Class<?> getCanonicalClass(String canonicalName) {
		try {
			return Class.forName(canonicalName);
		} catch (final ClassNotFoundException e) {
			throw new IllegalArgumentException("Reflection class error." + canonicalName, e);
		}
	}

	private static String expandVariables(String name) {
		final StringBuffer output = new StringBuffer();
		final Matcher matcher = NMSReflection.MATCH_VARIABLE.matcher(name);

		while (matcher.find()) {
			final String variable = matcher.group(1);
			String replacement = "";

			if ("nms".equalsIgnoreCase(variable)) {
				replacement = NMSReflection.NMS_PREFIX;
			} else if ("obc".equalsIgnoreCase(variable)) {
				replacement = NMSReflection.OBC_PREFIX;
			} else if ("version".equalsIgnoreCase(variable)) {
				replacement = NMSReflection.VERSION;
			} else {
				throw new IllegalArgumentException("Null variable: " + variable);
			}

			if (replacement.length() > 0 && matcher.end() < name.length() && name.charAt(matcher.end()) != '.') {
				replacement += ".";
			}
			matcher.appendReplacement(output, Matcher.quoteReplacement(replacement));
		}
		matcher.appendTail(output);
		return output.toString();
	}

}
