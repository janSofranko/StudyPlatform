    package org.example.studyplatform.repository;

    import org.example.studyplatform.entity.ActivityLog;
    import org.example.studyplatform.entity.StudyGroup;
    import org.springframework.data.jpa.repository.JpaRepository;

    import java.util.List;

    public interface ActivityLogRepository extends JpaRepository<ActivityLog, Long> {

        List<ActivityLog> findByGroupOrderByTimestampDesc(StudyGroup group);
    }
