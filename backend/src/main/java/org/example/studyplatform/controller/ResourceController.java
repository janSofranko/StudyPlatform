package org.example.studyplatform.controller;

import lombok.RequiredArgsConstructor;
import org.example.studyplatform.dto.CreateResourceRequest;
import org.example.studyplatform.entity.Resource;
import org.example.studyplatform.entity.StudyGroup;
import org.example.studyplatform.entity.Task;
import org.example.studyplatform.entity.User;
import org.example.studyplatform.service.ResourceService;
import org.example.studyplatform.service.StudyGroupService;
import org.example.studyplatform.service.TaskService;
import org.example.studyplatform.service.UserService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/resources")
@RequiredArgsConstructor
public class ResourceController {

    private final ResourceService resourceService;
    private final StudyGroupService groupService;
    private final UserService userService;
    private final TaskService taskService;

    @PostMapping
    public Resource createResource(@RequestBody CreateResourceRequest request) {
        StudyGroup group = groupService.getGroupById(request.getGroupId());
        User uploader = userService.getUser(request.getUploadedById());
        Task task = request.getTaskId() != null ? taskService.getTask(request.getTaskId()) : null;

        return resourceService.addResource(
                request.getTitle(),
                request.getUrl(),
                group,
                uploader,
                task
        );
    }
    @DeleteMapping("/{id}")
    public void deleteResource(@PathVariable("id") Long id) {
        resourceService.deleteResource(id);
    }

    @GetMapping("/group/{groupId}")
    public List<Resource> getResourcesByGroup(@PathVariable Long groupId) {
        StudyGroup group = groupService.getGroupById(groupId);
        return resourceService.getResourcesForGroup(group);
    }


    @GetMapping("/task/{taskId}")
    public List<Resource> getResourcesByTask(@PathVariable("taskId") Long taskId) {
        Task task = taskService.getTask(taskId);
        return resourceService.getResourcesForTask(task);
    }

}
