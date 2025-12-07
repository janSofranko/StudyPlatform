package org.example.studyplatform.controller;

import org.example.studyplatform.entity.Task;
import org.example.studyplatform.entity.StudyGroup;
import org.example.studyplatform.entity.TaskAssignment;
import org.example.studyplatform.entity.User;
import org.example.studyplatform.service.TaskService;
import org.example.studyplatform.service.StudyGroupService;
import org.example.studyplatform.service.UserService;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tasks")
@CrossOrigin
public class TaskController {

    private final TaskService taskService;
    private final StudyGroupService groupService;
    private final UserService userService;

    public TaskController(TaskService taskService,
                          StudyGroupService groupService,
                          UserService userService) {
        this.taskService = taskService;
        this.groupService = groupService;
        this.userService = userService;
    }

    @PostMapping
    public Task create(@RequestBody CreateTaskRequest request) {
        StudyGroup group = groupService.getGroupById(request.groupId);
        User creator = userService.getUser(request.createdBy);

        return taskService.createTask(
                request.title,
                request.description,
                request.status,
                group,
                creator
        );
    }

    @GetMapping("/group/by-name/{groupName}")
    public List<Task> getTasksForGroupByName(@PathVariable("groupName") String groupName) {
        StudyGroup group = groupService.getGroupByName(groupName);
        return taskService.getTasksForGroup(group);
    }

    @GetMapping("/group/{groupId}")
    public List<Task> getTasksForGroup(@PathVariable("groupId") Long groupId) {
        StudyGroup group = groupService.getGroupById(groupId);
        return taskService.getTasksForGroup(group);
    }

    @GetMapping("/{id}")
    public Task getOne(@PathVariable("id") Long id) {
        return taskService.getTask(id);
    }

    @PostMapping("/{id}/update")
    public Task update(@PathVariable("id") Long id, @RequestBody UpdateTaskRequest request) {
        return taskService.updateTask(id, request.title, request.description);
    }

    @PostMapping("/{id}/status")
    public Task changeStatus(@PathVariable("id") Long id, @RequestBody ChangeStatusRequest request) {
        return taskService.changeStatus(id, request.status);
    }

    @PostMapping("/{id}/assign")
    public void assign(@PathVariable("id") Long id, @RequestBody AssignUserRequest request) {
        taskService.assignUser(id, request.userId, request.role);
    }

    @PostMapping("/{id}/unassign/{userId}")
    public void unassign(@PathVariable("id") Long id, @PathVariable("userId") Long userId) {
        taskService.unassignUser(id, userId);
    }
    @GetMapping("/group/{groupId}/stats")
    public Map<String, Long> getTaskStats(@PathVariable("groupId") Long groupId) {
        StudyGroup group = groupService.getGroupById(groupId);
        List<Task> tasks = taskService.getTasksForGroup(group);

        long open = tasks.stream().filter(t -> "OPEN".equals(t.getStatus())).count();
        long inProgress = tasks.stream().filter(t -> "IN_PROGRESS".equals(t.getStatus())).count();
        long done = tasks.stream().filter(t -> "DONE".equals(t.getStatus())).count();

        Map<String, Long> stats = new HashMap<>();
        stats.put("total", (long) tasks.size());
        stats.put("open", open);
        stats.put("inProgress", inProgress);
        stats.put("done", done);
        return stats;
    }


    @GetMapping("/{id}/assignees")
    public List<TaskAssignment> assignees(@PathVariable("id") Long id) {
        return taskService.listAssignees(id);
    }

    public static class CreateTaskRequest {
        public String title;
        public String description;
        public String status;
        public Long groupId;
        public Long createdBy;
    }

    public static class UpdateTaskRequest {
        public String title;
        public String description;
    }

    public static class ChangeStatusRequest {
        public String status; // OPEN / IN_PROGRESS / DONE
    }

    public static class AssignUserRequest {
        public Long userId;
        public String role; // optional
    }
}
