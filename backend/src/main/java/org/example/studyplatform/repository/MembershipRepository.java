package org.example.studyplatform.repository;

import org.example.studyplatform.entity.Membership;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MembershipRepository extends JpaRepository<Membership, Long> {

    List<Membership> findByGroup_Id(Long groupId);

    List<Membership> findByUser_Id(Long userId);

    boolean existsByUser_IdAndGroup_Id(Long userId, Long groupId);
}
