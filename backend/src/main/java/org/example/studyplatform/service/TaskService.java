package org.example.studyplatform.service;

import org.example.studyplatform.entity.Task;
import org.example.studyplatform.entity.StudyGroup;
import org.example.studyplatform.entity.User;
import org.example.studyplatform.entity.TaskAssignment;
import org.example.studyplatform.notification.service.NotificationService;
import org.example.studyplatform.repository.MembershipRepository;
import org.example.studyplatform.repository.TaskRepository;
import org.example.studyplatform.repository.TaskAssignmentRepository;
import org.example.studyplatform.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final MembershipRepository membershipRepository;
    private final NotificationService notificationService;
    private final TaskAssignmentRepository taskAssignmentRepository;
    private final UserRepository userRepository;

    public TaskService(TaskRepository taskRepository,
                       MembershipRepository membershipRepository,
                       NotificationService notificationService,
                       TaskAssignmentRepository taskAssignmentRepository,
                       UserRepository userRepository) {
        this.taskRepository = taskRepository;
        this.membershipRepository = membershipRepository;
        this.notificationService = notificationService;
        this.taskAssignmentRepository = taskAssignmentRepository;
        this.userRepository = userRepository;
    }

    public Task createTask(String title, String description, String status,
                           StudyGroup group, User createdBy) {

        Task task = new Task();
        task.setTitle(title);
        task.setDescription(description);
        task.setStatus(status != null ? status : "OPEN");
        task.setGroup(group);
        task.setCreatedBy(createdBy);

        Task saved = taskRepository.save(task);

        membershipRepository.findByGroup_Id(group.getId()).forEach(m ->
                notificationService.createAndSend(
                        m.getUser().getId(),
                        "New task in " + group.getName() + ": " + title
                )
        );

        return saved;
    }

    public List<Task> getTasksForGroup(StudyGroup group) {
        return taskRepository.findByGroup(group);
    }

    public Task getTask(Long id) {
        return taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Task not found"));
    }

    public Task updateTask(Long id, String title, String description) {
        Task task = getTask(id);
        if (title != null && !title.isBlank()) task.setTitle(title);
        if (description != null) task.setDescription(description);
        return taskRepository.save(task);
    }

    public Task changeStatus(Long id, String newStatus) {
        if (!List.of("OPEN", "IN_PROGRESS", "DONE").contains(newStatus)) {
            throw new RuntimeException("Invalid status");
        }
        Task task = getTask(id);
        task.setStatus(newStatus);
        Task saved = taskRepository.save(task);

        taskAssignmentRepository.findByTask(task).forEach(a ->
                notificationService.createAndSend(a.getUser().getId(),
                        "Task \"" + task.getTitle() + "\" status changed to " + newStatus)
        );

        return saved;
    }

    public void assignUser(Long taskId, Long userId, String role) {
        Task task = getTask(taskId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (taskAssignmentRepository.existsByTaskAndUser(task, user)) {
            throw new RuntimeException("User already assigned to this task");
        }

        TaskAssignment ta = new TaskAssignment();
        ta.setTask(task);
        ta.setUser(user);
        ta.setRole(role != null ? role : "assignee");
        taskAssignmentRepository.save(ta);


        if ("OPEN".equals(task.getStatus())) {
            task.setStatus("IN_PROGRESS");
            taskRepository.save(task);
        }

        notificationService.createAndSend(user.getId(),
                "You were assigned to task \"" + task.getTitle() + "\"");
    }


    public void unassignUser(Long taskId, Long userId) {
        Task task = getTask(taskId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        TaskAssignment ta = taskAssignmentRepository.findByTaskAndUser(task, user)
                .orElseThrow(() -> new RuntimeException("Assignment not found"));

        taskAssignmentRepository.delete(ta);

        notificationService.createAndSend(user.getId(),
                "You were unassigned from task \"" + task.getTitle() + "\"");
    }

    public List<TaskAssignment> listAssignees(Long taskId) {
        Task task = getTask(taskId);
        return taskAssignmentRepository.findByTask(task);
    }
}
