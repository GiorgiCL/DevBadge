package com.devbadge.devbadge.service;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Service;

import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class UserSyncService {

    private final ScoringService scoringService;
    private final TaskExecutor taskExecutor;

    private final ConcurrentHashMap<String, CompletableFuture<Void>> inFlight = new ConcurrentHashMap<>();

    public UserSyncService(
            ScoringService scoringService,
            @Qualifier("applicationTaskExecutor") TaskExecutor taskExecutor
    ) {
        this.scoringService = scoringService;
        this.taskExecutor = taskExecutor;
    }

    public void warmupUserIfNeeded(String username) {
        String key = username.toLowerCase(Locale.ROOT);

        inFlight.computeIfAbsent(key, k -> {
            CompletableFuture<Void> future = new CompletableFuture<>();

            taskExecutor.execute(() -> {
                try {
                    // Reuse your existing “fetch from GitHub + persist + compute + cache” flow
                    scoringService.calculateScores(username);
                    future.complete(null);
                } catch (Exception ex) {
                    future.completeExceptionally(ex);
                } finally {
                    inFlight.remove(k);
                }
            });

            return future;
        });
    }
}
