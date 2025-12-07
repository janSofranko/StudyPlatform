package org.example.studyplatform.controller;

import org.example.studyplatform.entity.Membership;
import org.example.studyplatform.service.MembershipService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/memberships")
public class MembershipController {

    private final MembershipService membershipService;

    public MembershipController(MembershipService membershipService) {
        this.membershipService = membershipService;
    }

    @GetMapping("/group/{groupId}")
    public List<Membership> getGroupMembers(@PathVariable("groupId") Long groupId) {
        return membershipService.getGroupMembers(groupId);
    }

    @PostMapping("/join")
    public Membership joinGroup(
            @RequestParam(name = "userId") Long userId,
            @RequestParam(name = "groupId") Long groupId,
            @RequestParam(name = "role", defaultValue = "member") String role
    ) {
        return membershipService.joinGroup(userId, groupId, role);
    }

    @DeleteMapping("/{membershipId}")
    public void removeMember(@PathVariable("membershipId") Long membershipId) {
        membershipService.removeMember(membershipId);
    }
}
