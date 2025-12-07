    package org.example.studyplatform.repository;

    import org.example.studyplatform.entity.Task;
    import org.example.studyplatform.entity.StudyGroup;
    import org.springframework.data.jpa.repository.JpaRepository;

    import java.util.List;

    public interface TaskRepository extends JpaRepository<Task, Long> {

        List<Task> findByGroup(StudyGroup group);
    }
