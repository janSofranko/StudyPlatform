package org.example.studyplatform.repository;

import org.example.studyplatform.entity.Resource;
import org.example.studyplatform.entity.StudyGroup;
import org.example.studyplatform.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ResourceRepository extends JpaRepository<Resource, Long> {
    List<Resource> findByGroup(StudyGroup group);
    List<Resource> findByTask(Task task); // nové
}
