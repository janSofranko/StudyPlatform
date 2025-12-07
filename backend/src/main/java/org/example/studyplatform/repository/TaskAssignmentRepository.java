package org.example.studyplatform.repository;

import org.example.studyplatform.entity.Task;
import org.example.studyplatform.entity.TaskAssignment;
import org.example.studyplatform.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TaskAssignmentRepository extends JpaRepository<TaskAssignment, Long> {
    List<TaskAssignment> findByTask(Task task);
    boolean existsByTaskAndUser(Task task, User user);
    Optional<TaskAssignment> findByTaskAndUser(Task task, User user);
}
