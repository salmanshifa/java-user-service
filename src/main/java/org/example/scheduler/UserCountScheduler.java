package org.example.scheduler;

import org.example.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class UserCountScheduler {
    private static final Logger log = LoggerFactory.getLogger(UserCountScheduler.class);

    private final UserService userService;
    private final String userCountCron;

    public UserCountScheduler(UserService userService,
                              @Value("${app.cron.user-count}") String userCountCron) {
        this.userService = userService;
        this.userCountCron = userCountCron;
    }

    @Scheduled(cron = "${app.cron.user-count}")
    public void logUserCount() {
        log.info("Scheduled job [{}] - counting users...", userCountCron);
        long count = userService.count();
        log.info("Scheduled job [{}] - current user count: {}", userCountCron, count);
        
    }
}
