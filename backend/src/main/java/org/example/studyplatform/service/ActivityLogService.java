package org.example.studyplatform.service;

import lombok.RequiredArgsConstructor;
import org.example.studyplatform.entity.ActivityLog;
import org.example.studyplatform.entity.StudyGroup;
import org.example.studyplatform.entity.User;
import org.example.studyplatform.repository.ActivityLogRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ActivityLogService {

    private final ActivityLogRepository activityLogRepository;

    public void log(User user, StudyGroup group, String action, String message) {
        ActivityLog log = new ActivityLog();
        log.setUser(user);
        log.setGroup(group);
        log.setAction(action);
        log.setMessage(message);
        log.setTimestamp(LocalDateTime.now());

        activityLogRepository.save(log);
    }

    public List<ActivityLog> getGroupActivity(StudyGroup group) {
        return activityLogRepository.findByGroupOrderByTimestampDesc(group);
    }
}
