    package org.example.studyplatform.service;

    import org.example.studyplatform.entity.Membership;
    import org.example.studyplatform.entity.User;
    import org.example.studyplatform.entity.StudyGroup;
    import org.example.studyplatform.notification.service.NotificationService;
    import org.example.studyplatform.repository.MembershipRepository;
    import org.example.studyplatform.repository.UserRepository;
    import org.example.studyplatform.repository.StudyGroupRepository;
    import org.springframework.stereotype.Service;

    import java.util.List;

    @Service
    public class MembershipService {

        private final MembershipRepository membershipRepository;
        private final UserRepository userRepository;
        private final StudyGroupRepository groupRepository;
        private final NotificationService notificationService;

        public MembershipService(
                MembershipRepository membershipRepository,
                UserRepository userRepository,
                StudyGroupRepository groupRepository,
                NotificationService notificationService
        ) {
            this.membershipRepository = membershipRepository;
            this.userRepository = userRepository;
            this.groupRepository = groupRepository;
            this.notificationService = notificationService;
        }

        public List<Membership> getGroupMembers(Long groupId) {
            return membershipRepository.findByGroup_Id(groupId);
        }

        public Membership joinGroup(Long userId, Long groupId, String role) {
            if (membershipRepository.existsByUser_IdAndGroup_Id(userId, groupId)) {
                throw new RuntimeException("User is already a member of this group");
            }

            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            StudyGroup group = groupRepository.findById(groupId)
                    .orElseThrow(() -> new RuntimeException("Group not found"));

            Membership membership = new Membership();
            membership.setUser(user);
            membership.setGroup(group);
            membership.setRole(role);

            Membership saved = membershipRepository.save(membership);


            notificationService.createAndSend(user.getId(),
                    "You were added to the group " + group.getName());

            return saved;
        }

        public void removeMember(Long membershipId) {
            Membership membership = membershipRepository.findById(membershipId)
                    .orElseThrow(() -> new RuntimeException("Membership not found"));

            Long userId = membership.getUser().getId();
            String groupName = membership.getGroup().getName();

            membershipRepository.deleteById(membershipId);


            notificationService.createAndSend(userId,
                    "You were removed from the group " + groupName);
        }
    }
