package com.google.gson;

import com.google.gson.reflect.TypeToken;

public class JRefTypeAdapterFactory implements TypeAdapterFactory {

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
		TypeAdapter<Object> delegateTypeAdapter = (TypeAdapter<Object>) gson.getDelegateAdapter(this, type);
		// We require a delegate type adapter. If none is present, then return null
		if (delegateTypeAdapter == null) {
			return null;
		}
		return (TypeAdapter) new JRefTypeAdapter((TypeToken<Object>) type, delegateTypeAdapter);
	}

}
