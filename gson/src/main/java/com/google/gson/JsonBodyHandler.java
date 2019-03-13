package com.google.gson;

import java.net.http.HttpResponse.*;
import java.nio.*;
import java.nio.charset.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.Flow.*;
import java.util.function.*;

/**
 * Utility class for creating Json parsing {@link BodyHandler}-s.
 * @author Degubi
 */
public final class JsonBodyHandler<T> implements BodySubscriber<T>{
	private static final JsonParser parser = new JsonParser();

	private final CompletableFuture<T> future = new CompletableFuture<>();
	private final ArrayList<ByteBuffer> buffers = new ArrayList<>();
	private final Charset charset;
	private final Function<String, T> finisherFunction;
	
	private JsonBodyHandler(ResponseInfo info, Function<String, T> finisherFunction) {
		this.charset = info.headers().firstValue("Content-type")
					       .map(type -> Charset.forName(type.substring(type.lastIndexOf('=') + 1)))
					       .orElse(StandardCharsets.UTF_8);
		this.finisherFunction = finisherFunction;
	}
	
	/**
	 * Create a {@link BodyHandler} that parses the response body as a {@link JsonObject}.
	 * @return The {@link BodyHandler} object.
	 */
	public static BodyHandler<JsonObject> ofJsonObject(){
		return info -> new JsonBodyHandler<>(info, data -> parser.parse(data).getAsJsonObject());
	}
	
	/**
	 * Create a {@link BodyHandler} that parses the response body as a {@link JsonElement}.
	 * @return The {@link BodyHandler} object.
	 */
	public static BodyHandler<JsonElement> ofJsonElement() {
		return info -> new JsonBodyHandler<>(info, parser::parse);
	}
	
	/**
	 * Create a {@link BodyHandler} that parses the response body as a {@link JsonArray}.
	 * @return The {@link BodyHandler} object.
	 */
	public static BodyHandler<JsonArray> ofJsonArray(){
		return info -> new JsonBodyHandler<>(info, data -> parser.parse(data).getAsJsonArray());
	}
	
	/**
	 * Create a {@link BodyHandler} that parses the response body as a {@link JsonPrimitive}.
	 * @return The {@link BodyHandler} object.
	 */
	public static BodyHandler<JsonPrimitive> ofJsonPrimitive(){
		return info -> new JsonBodyHandler<>(info, data -> parser.parse(data).getAsJsonPrimitive());
	}
	
	/**
	 * Create a {@link BodyHandler} that parses the response body as a custom type.
	 * @param gson The {@code Gson} instance used for parsing.
	 * @param type The type to parse.
	 * @return The {@link BodyHandler} object.
	 */
	public static<T> BodyHandler<T> ofType(Gson gson, Class<T> type){
		return info -> new JsonBodyHandler<>(info, data -> gson.fromJson(data, type));
	}
	
	@Override
	public void onSubscribe(Subscription subscription) {
		subscription.request(Long.MAX_VALUE);
	}

	@Override
	public void onNext(List<ByteBuffer> item) {
		buffers.addAll(item);
	}

	@Override
	public void onError(Throwable throwable) {
		buffers.clear();
		future.completeExceptionally(throwable);
	}

	@Override
	public void onComplete() {
		ArrayList<ByteBuffer> buffers = this.buffers;
		byte[] data = new byte[buffers.stream().mapToInt(ByteBuffer::remaining).sum()];
		
		int offset = 0;
        for(ByteBuffer buff : buffers) {
            int size = buff.remaining();
            buff.get(data, offset, size);
            offset += size;
        }
        
        future.complete(finisherFunction.apply(new String(data, charset)));
        buffers.clear();
	}

	@Override
	public CompletionStage<T> getBody() {
		return future;
	}
}