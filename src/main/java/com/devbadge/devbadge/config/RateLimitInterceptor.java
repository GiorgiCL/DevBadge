package com.devbadge.devbadge.config;

import com.devbadge.devbadge.exception.RateLimitExceededException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.*;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
public class RateLimitInterceptor implements ClientHttpRequestInterceptor {

    private static final int MAX_RETRIES = 3;
    private static final int SAFE_THRESHOLD = 10; // throttle when remaining < 10

    @Override
    public ClientHttpResponse intercept(
            HttpRequest request,
            byte[] body,
            ClientHttpRequestExecution execution
    ) throws IOException {

        int attempts = 0;

        while (attempts < MAX_RETRIES) {
            attempts++;

            ClientHttpResponse response = execution.execute(request, body);


            HttpHeaders headers = response.getHeaders();

            String remainingStr = headers.getFirst("X-RateLimit-Remaining");
            String resetStr = headers.getFirst("X-RateLimit-Reset");

            int remaining = remainingStr != null ? Integer.parseInt(remainingStr) : -1;
            long resetEpoch = resetStr != null ? Long.parseLong(resetStr) : -1;

            if (remaining == 0 && resetEpoch > 0) {
                long waitMs = (resetEpoch * 1000L) - System.currentTimeMillis();
                if (waitMs > 0) {
                    log.warn("GitHub rate limit reached. Waiting {} ms until reset...", waitMs);
                    try {
                        Thread.sleep(waitMs);
                    } catch (InterruptedException ignored) {}
                }

                continue;
            }

            if (remaining > 0 && remaining < SAFE_THRESHOLD) {
                log.warn("Throttling GitHub requests... remaining={}", remaining);
                try {
                    Thread.sleep(1000); // 1 second backoff
                } catch (InterruptedException ignored) {}
            }

            return response;
        }

        throw new RateLimitExceededException("GitHub API rate limit exceeded after retries.");
    }
}
