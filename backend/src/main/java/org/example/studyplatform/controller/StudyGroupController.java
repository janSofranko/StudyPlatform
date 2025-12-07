package org.example.studyplatform.controller;

import jakarta.validation.Valid;
import org.example.studyplatform.dto.CreateGroupRequest;
import org.example.studyplatform.dto.StudyGroupResponse;
import org.example.studyplatform.entity.StudyGroup;
import org.example.studyplatform.entity.User;
import org.example.studyplatform.service.StudyGroupService;
import org.example.studyplatform.service.UserService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/groups")
@CrossOrigin
public class StudyGroupController {

    private final StudyGroupService studyGroupService;
    private final UserService userService;

    public StudyGroupController(StudyGroupService studyGroupService, UserService userService) {
        this.studyGroupService = studyGroupService;
        this.userService = userService;
    }

    @PostMapping
    public StudyGroupResponse createGroup(@Valid @RequestBody CreateGroupRequest request) {
        User creator = userService.getUser(request.creatorId);
        StudyGroup group = studyGroupService.createGroup(
                request.name,
                request.description,
                creator
        );

        return new StudyGroupResponse(
                group.getId(),
                group.getName(),
                group.getDescription(),
                group.getCreatedBy().getId()
        );
    }

    @GetMapping
    public List<StudyGroupResponse> getAll() {
        return studyGroupService.getAllGroups()
                .stream()
                .map(g -> new StudyGroupResponse(
                        g.getId(),
                        g.getName(),
                        g.getDescription(),
                        g.getCreatedBy().getId()
                ))
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public StudyGroupResponse getOne(@PathVariable("id") Long id) {
        StudyGroup g = studyGroupService.getGroupById(id);
        return new StudyGroupResponse(
                g.getId(),
                g.getName(),
                g.getDescription(),
                g.getCreatedBy().getId()
        );
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable("id") Long id) {
        studyGroupService.deleteGroup(id);
    }

    // NEW: groups for a specific user (membership-based)
    @GetMapping("/user/{userId}")
    public List<StudyGroupResponse> getGroupsForUser(@PathVariable("userId") Long userId) {
        return studyGroupService.getGroupsForUser(userId)
                .stream()
                .map(g -> new StudyGroupResponse(
                        g.getId(),
                        g.getName(),
                        g.getDescription(),
                        g.getCreatedBy().getId()
                ))
                .toList();
    }
}
