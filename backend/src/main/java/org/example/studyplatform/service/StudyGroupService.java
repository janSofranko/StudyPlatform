package org.example.studyplatform.service;

import org.example.studyplatform.entity.StudyGroup;
import org.example.studyplatform.repository.StudyGroupRepository;
import org.example.studyplatform.repository.MembershipRepository;
import org.example.studyplatform.repository.TaskRepository;
import org.example.studyplatform.repository.ResourceRepository;
import org.example.studyplatform.repository.ActivityLogRepository;
import org.example.studyplatform.notification.service.NotificationService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class StudyGroupService {

    private final StudyGroupRepository studyGroupRepository;
    private final MembershipRepository membershipRepository;
    private final TaskRepository taskRepository;
    private final ResourceRepository resourceRepository;
    private final ActivityLogRepository activityLogRepository;
    private final MembershipService membershipService;
    private final NotificationService notificationService;

    public StudyGroupService(StudyGroupRepository studyGroupRepository,
                             MembershipRepository membershipRepository,
                             TaskRepository taskRepository,
                             ResourceRepository resourceRepository,
                             ActivityLogRepository activityLogRepository,
                             MembershipService membershipService,
                             NotificationService notificationService) {

        this.studyGroupRepository = studyGroupRepository;
        this.membershipRepository = membershipRepository;
        this.taskRepository = taskRepository;
        this.resourceRepository = resourceRepository;
        this.activityLogRepository = activityLogRepository;
        this.membershipService = membershipService;
        this.notificationService = notificationService;
    }

    public StudyGroup createGroup(String name, String description, org.example.studyplatform.entity.User creator) {
        StudyGroup group = new StudyGroup();
        group.setName(name);
        group.setDescription(description);
        group.setCreatedBy(creator);
        return studyGroupRepository.save(group);
    }

    public List<StudyGroup> getAllGroups() {
        return studyGroupRepository.findAll();
    }

    public StudyGroup getGroupById(Long id) {
        return studyGroupRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Group not found"));
    }

    public StudyGroup getGroupByName(String name) {
        return studyGroupRepository.findByName(name)
                .orElseThrow(() -> new RuntimeException("Group not found"));
    }

    public void deleteGroup(Long id) {
        StudyGroup group = studyGroupRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Group not found"));

        String groupName = group.getName();

        var members = membershipRepository.findByGroup_Id(id);

        members.forEach(m -> membershipService.removeMember(m.getMembershipId()));

        members.forEach(m -> notificationService.createAndSend(
                m.getUser().getId(),
                "Group " + groupName + " has been canceled."
        ));

        taskRepository.deleteAll(taskRepository.findByGroup(group));
        resourceRepository.deleteAll(resourceRepository.findByGroup(group));
        activityLogRepository.deleteAll(activityLogRepository.findByGroupOrderByTimestampDesc(group));
        studyGroupRepository.delete(group);
    }


    public List<StudyGroup> getGroupsForUser(Long userId) {
        return membershipRepository.findByUser_Id(userId)
                .stream()
                .map(m -> m.getGroup())
                .toList();
    }
}
