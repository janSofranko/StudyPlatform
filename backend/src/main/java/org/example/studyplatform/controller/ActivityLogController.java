package org.example.studyplatform.controller;

import lombok.RequiredArgsConstructor;
import org.example.studyplatform.entity.ActivityLog;
import org.example.studyplatform.entity.StudyGroup;
import org.example.studyplatform.service.ActivityLogService;
import org.example.studyplatform.service.StudyGroupService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/activity")
@RequiredArgsConstructor
public class ActivityLogController {

    private final ActivityLogService activityLogService;
    private final StudyGroupService groupService;

    @GetMapping("/group/{groupId}")
    public List<ActivityLog> getActivityForGroup(@PathVariable("groupId") Long groupId) {
        StudyGroup group = groupService.getGroupById(groupId);
        return activityLogService.getGroupActivity(group);
    }
}
