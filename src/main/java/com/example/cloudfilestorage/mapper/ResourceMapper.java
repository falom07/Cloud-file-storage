package com.example.cloudfilestorage.mapper;

import com.example.cloudfilestorage.dto.ResourceDTO;
import com.example.cloudfilestorage.entity.Resource;
import com.example.cloudfilestorage.entity.ResourceType;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ResourceMapper {


    public ResourceDTO mapFileDto(Resource resource) {
        return new ResourceDTO(
                resource.getPath().substring(resource.getPath().indexOf("/") + 1),
                resource.getName(),
                resource.getSize(),
                resource.getType()
        );
    }

    public List<ResourceDTO> mapResourcesDto(List<Resource> resources) {
        return resources.stream()
                .map(r -> r.getType().equals(ResourceType.DIRECTORY) ?
                        mapDirectoryDTO(r) :
                        mapFileDto(r))
                .toList();
    }

    public ResourceDTO mapDirectoryDTO(Resource resource) {
        return new ResourceDTO(
                resource.getPath().substring(resource.getPath().indexOf("/") + 1),
                resource.getName() + "/",
                null,
                ResourceType.DIRECTORY
        );
    }
}
