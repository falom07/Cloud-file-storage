package com.example.cloudfilestorage.mapper;

import com.example.cloudfilestorage.dto.ResourceDTO;
import com.example.cloudfilestorage.entity.Resource;
import com.example.cloudfilestorage.entity.ResourceType;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
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

    public List<ResourceDTO> mapResourcesDto(List<Resource> resource) {
        List<ResourceDTO> list = new ArrayList<>();
        for (Resource path : resource) {
            if (path.getType().equals(ResourceType.FILE)) {
                list.add(mapFileDto(path));
            } else {
                list.add(mapDirectoryDTO(path));
            }
        }

        return list;
    }

    public List<ResourceDTO> mapDirectoriesDto(List<Resource> resources) {
        List<ResourceDTO> list = new ArrayList<>();
        for (Resource resource : resources) {
            ResourceDTO dto = resource.getType().equals(ResourceType.DIRECTORY)
                    ? mapDirectoryDTO(resource) : mapFileDto(resource);
            list.add(dto);
        }
        return list;
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
