package com.google.gson.internal.bind;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.internal.ObjectConstructor;
import com.google.gson.internal.bind.TypeAdapterRuntimeTypeWrapper;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

/**
 * Create adapter for destinated {@link java.lang.Iterable Iterable} interface implement.
 * <p>To make the factory work properly, you must register it in {@link com.google.gson.GsonBuilder GsonBuilder}
 * and assign an adding method for certain Iterable implement. A simple example is show below:</p>
 * <pre>
 * 	Type aIterableType = new TypeToken&lt;Iterable&lt;String&gt;&gt;(){}.getType();
 * 	Gson gson = new GsonBuilder()
 * 			.registerTypeAdapterFactory(new IterableTypeAdapterFactory(ArrayList.class, "add"))
 *			.create();
 *	Iterable&lt;String&gt; sit = new ArrayList&lt;String&gt;(Arrays.asList("abc", "def"));
 *	String json = gson.toJson(sit);
 *	Iterable&lt;String&gt; nsit = gson.fromJson(json, aIterableType);
 *</pre>
 * The correctness of writing behavior replys on the writeMethodName parameterand and the anonymous 
 * {@link com.google.gson.reflect.TypeToken TypeToken} class 
 * @author ianate
 * 
 */
/*
 * non-javadoc
 * On issue#672:
 * It is impossible to provide a TypeAdapterFactory like CollectionTypeAdapterFactory for Iterable.class.
 * The main reason is that the Iterable class provides read method only(for-each loop and thus corresponding to JSON array), 
 * while Collection class, as an implementation of Iterable, provides both read and write methodss.
 * So when you try to deserialize a JSON into a Iterable class, the Iterable implementation must provides 
 * an accessible write method(more precisly, an accessible single-adding method for reflect methods in java) 
 */
public class IterableTypeAdapterFactory implements TypeAdapterFactory{
	
	@SuppressWarnings("rawtypes")
	private final Class<? extends Iterable> iterableClass;
	private final String writeMethodName;
	
	public IterableTypeAdapterFactory(@SuppressWarnings("rawtypes") Class<? extends Iterable> iterableClass, String writeMethodName) {
		this.iterableClass = iterableClass;
		this.writeMethodName = writeMethodName;
	}
	
	@SuppressWarnings("rawtypes")
	@Override
	public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> typeToken) {
		Class<? super T> rawType = typeToken.getRawType();
		if(!Iterable.class.isAssignableFrom(rawType)){
			return null;
		}
		
		Type elementType = null;
		if(Iterable.class.equals(rawType)){
			Type type = typeToken.getType();
			if(type instanceof Class){//Class cannot hold parameter as ParameterizedType do
				throw new RuntimeException("Destinated Type parameter must be an anonymous "
						+ "com.google.gson.reflect.TypeToken class to avoid runtime erasure ");
			}
			elementType = 
					((ParameterizedType) type).getActualTypeArguments()[0];
		}else{
			elementType = ((ParameterizedType) rawType.getGenericSuperclass()).getActualTypeArguments()[0];
		}
		TypeAdapter<?> elementTypeAdapter = gson.getAdapter(TypeToken.get(elementType));
		@SuppressWarnings("unchecked")//type must agree
		TypeAdapter<T> adapter = 
				new IterableTypeAdapter(gson, elementType, elementTypeAdapter, iterableClass, writeMethodName);
		return adapter;
	}
	
	private static final class IterableTypeAdapter<E> extends TypeAdapter<Iterable<E>>{

		private final TypeAdapter<E> elementTypeAdapter;
		private final ObjectConstructor<? extends Iterable<E>> iterableConstructor;
		private final Method method;
		
		private IterableTypeAdapter(Gson gsonContext, Type elementType,
				TypeAdapter<E> typeAdapter,final Class<? extends Iterable<E>> iterableClass,String writeMethodName) {
			this.elementTypeAdapter = 
					new TypeAdapterRuntimeTypeWrapper<E>(gsonContext, typeAdapter, elementType);

			try {
				this.method = iterableClass.getDeclaredMethod(writeMethodName, Object.class);
			} catch (NoSuchMethodException e1) {
				throw new RuntimeException();
			} catch (SecurityException e1) {
				throw new RuntimeException();
			}

			this.iterableConstructor = new ObjectConstructor<Iterable<E>>() {
				@Override
				public Iterable<E> construct() {
					Constructor<? extends Iterable<E>> constructor;
					try {
						constructor = iterableClass.getDeclaredConstructor();
						if(!constructor.isAccessible()){
							constructor.setAccessible(true);
						}
						return constructor.newInstance();
					} catch (InstantiationException e) {
						throw new RuntimeException();
					} catch (IllegalAccessException e) {
						throw new RuntimeException();
					} catch (IllegalArgumentException e) {
						throw new RuntimeException();
					} catch (InvocationTargetException e) {
						throw new RuntimeException();
					} catch (NoSuchMethodException e) {
						throw new RuntimeException(iterableClass.getSimpleName() 
								+ "has no default constructor");
					} catch (SecurityException e) {
						throw new RuntimeException();
					}
				}
			};
		}
		
		@Override public void write(JsonWriter out, Iterable<E> iterables) throws IOException {
			if(iterables == null){
				out.nullValue();
				return;
			}
			out.beginArray();
			for(E element : iterables){
				elementTypeAdapter.write(out, element);
			}
			out.endArray();
		}

		@Override public Iterable<E> read(JsonReader in) throws IOException {
			if(in.peek() == JsonToken.NULL){
		        in.nextNull();
		        return null;
		     }
			
			Iterable<E> iterables = iterableConstructor.construct();
			if(!method.isAccessible()){
				method.setAccessible(true);
			}
			in.beginArray();
			while(in.hasNext()){
				E element = elementTypeAdapter.read(in);
				try {
					method.invoke(iterables, element);
				} catch (IllegalAccessException e) {
					throw new RuntimeException();
				} catch (IllegalArgumentException e) {
					throw new RuntimeException();
				} catch (InvocationTargetException e) {
					throw new RuntimeException();
				}
			}
			in.endArray();
			return iterables;
		}
		
	}

}
