package be.technobel.corder.pl.interceptor;

import be.technobel.corder.pl.config.exceptions.TooManyRequestsException;
import io.github.bucket4j.Bucket;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.HandlerInterceptor;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A class representing a rate limit interceptor that implements the HandlerInterceptor interface.
 * This interceptor applies rate limiting to incoming requests based on the client's IP address.
 * If the client exceeds the rate limit, a TooManyRequestsException is thrown.
 */
public class RateLimitInterceptor implements HandlerInterceptor {
    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    /**
     * Creates a new instance of {@link Bucket}.
     *
     * @return The newly created {@link Bucket} instance.
     */
    private Bucket createNewBucket() {
        return Bucket.builder()
                .addLimit(limit -> limit
                        .capacity(100)
                        .refillGreedy(60, Duration.ofMinutes(1)))
                .build();
    }

    /**
     * Checks if the incoming request from a client is within the rate limit.
     *
     * @param request  the HttpServletRequest object representing the incoming request
     * @param response the HttpServletResponse object representing the response to be sent
     * @param handler  the handler object that will be used to process the request
     * @return true if the request is within the rate limit, false otherwise
     * @throws TooManyRequestsException if the client exceeds the rate limit
     */
    @Override
    public boolean preHandle(HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler) {
        String ip = request.getRemoteAddr();
        System.out.println(ip);
        Bucket bucket = buckets.computeIfAbsent(ip, k -> createNewBucket());

        if (bucket.tryConsume(1)) {
            return true;
        } else {
            throw new TooManyRequestsException("Too many requests, try later");
        }
    }
}