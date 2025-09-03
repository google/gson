package com.google.gson.stream.control;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

/**
 * Utility class for performing "switch-like" operations on values.
 * <p>
 * Provides methods to execute actions or return results based on a matching value,
 * similar to a traditional switch-case statement. Supports default behavior
 * when no case matches, both synchronously and asynchronously.
 * </p>
 *
 * <p><b>Example usage (synchronous):</b></p>
 * <pre>{@code
 * Map<String, Runnable> actions = Map.of(
 *     "start", () -> System.out.println("Starting..."),
 *     "stop", () -> System.out.println("Stopping...")
 * );
 * CaseMatcher.on("start", actions, () -> System.out.println("Unknown command"));
 *
 * Map<String, Supplier<String>> responses = Map.of(
 *     "hello", () -> "Hi there!",
 *     "bye", () -> "Goodbye!"
 * );
 * String response = CaseMatcher.returning("hello", responses, () -> "Unknown input");
 * System.out.println(response); // Prints: Hi there!
 * }</pre>
 *
 * <p><b>Example usage (asynchronous):</b></p>
 * <pre>{@code
 * Map<String, Supplier<String>> asyncResponses = Map.of(
 *     "ping", () -> "pong",
 *     "hello", () -> "hi!"
 * );
 * CompletableFuture<String> future = CaseMatcher.returnAsync("ping", asyncResponses, () -> "unknown");
 * future.thenAccept(System.out::println); // Prints: pong
 * }</pre>
 */
public final class MatcherCase {

    private MatcherCase() {
    }

    /**
     * Executes a matching action based on the given value.
     *
     * @param value       the input value to match
     * @param cases       a map of values to corresponding actions
     * @param defaultCase the action to execute if no match is found (must not be null)
     * @param <T>         the type of the input value
     */
    public static <T> void on(T value, Map<T, Runnable> cases, Runnable defaultCase) {
        Objects.requireNonNull(defaultCase, "defaultCase must not be null");
        if (isInvalid(value)) return;
        cases.getOrDefault(value, defaultCase).run();
    }

    /**
     * Returns a value based on the given input, similar to a switch-case with a return.
     *
     * @param value       the input value to match
     * @param cases       a map of values to suppliers providing results
     * @param defaultCase the supplier to use if no match is found (must not be null)
     * @param <T>         the type of the input value
     * @param <R>         the type of the return value
     * @return the result from the matching supplier, or the default supplier if no match
     */
    public static <T, R> R returning(T value, Map<T, Supplier<R>> cases, Supplier<R> defaultCase) {
        Objects.requireNonNull(defaultCase, "defaultCase must not be null");
        if (isInvalid(value)) return defaultCase.get();
        return cases.getOrDefault(value, defaultCase).get();
    }

    /**
     * Asynchronously returns a value based on the given input, similar to a switch-case with a return.
     * The supplier will be executed asynchronously in a separate thread.
     *
     * @param value       the input value to match
     * @param cases       a map of values to suppliers providing results
     * @param defaultCase the supplier to use if no match is found
     * @param <T>         the type of the input and return value
     * @return a {@link CompletableFuture} containing the result from the matching supplier
     */
    public static <T> CompletableFuture<T> returnAsync(T value, Map<T, Supplier<T>> cases, Supplier<T> defaultCase) {
        Objects.requireNonNull(defaultCase, "defaultCase must not be null");
        if (isInvalid(value)) return CompletableFuture.completedFuture(defaultCase.get());
        return CompletableFuture.supplyAsync(() -> returning(value, cases, defaultCase));
    }

    /**
     * Checks if the value is considered invalid for matching.
     * Currently treats null values as invalid.
     *
     * @param value the value to check
     * @param <T>   the type of the value
     * @return true if the value is null, false otherwise
     */
    private static <T> boolean isInvalid(T value) {
        return value == null;
    }
}
