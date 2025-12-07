package org.example.studyplatform.service;

import lombok.RequiredArgsConstructor;
import org.example.studyplatform.entity.Resource;
import org.example.studyplatform.entity.StudyGroup;
import org.example.studyplatform.entity.Task;
import org.example.studyplatform.entity.User;
import org.example.studyplatform.repository.ResourceRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ResourceService {

    private final ResourceRepository resourceRepository;

    public Resource addResource(String title, String url, StudyGroup group, User uploader, Task task) {
        Resource r = new Resource();
        r.setTitle(title);
        r.setUrl(url);
        r.setGroup(group);
        r.setUploadedBy(uploader);
        r.setTask(task);
        return resourceRepository.save(r);
    }

    public List<Resource> getResourcesForGroup(StudyGroup group) {
        return resourceRepository.findByGroup(group);
    }

    public List<Resource> getResourcesForTask(Task task) {
        return resourceRepository.findByTask(task);
    }

    public void deleteResource(Long id) {
        resourceRepository.deleteById(id);
    }
}
