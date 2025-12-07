    package org.example.studyplatform.entity;

    import jakarta.persistence.*;
    import lombok.Getter;
    import lombok.Setter;

    import java.time.LocalDateTime;

    @Entity
    @Getter
    @Setter
    @Table(name = "memberships")
    public class Membership {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long membershipId;

        @ManyToOne
        @JoinColumn(name = "user_id", nullable = false)
        private User user;

        @ManyToOne
        @JoinColumn(name = "group_id", nullable = false)
        private StudyGroup group;

        @Column(nullable = false)
        private String role;

        private LocalDateTime joinedAt = LocalDateTime.now();
    }
