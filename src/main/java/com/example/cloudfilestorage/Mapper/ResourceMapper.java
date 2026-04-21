package com.example.cloudfilestorage.Mapper;

import com.example.cloudfilestorage.DTO.DirectoryDTO;
import com.example.cloudfilestorage.DTO.FileDTO;
import com.example.cloudfilestorage.DTO.ResourceDTO;
import com.example.cloudfilestorage.Entity.Resource;
import com.example.cloudfilestorage.Entity.ResourceType;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class ResourceMapper {


    public ResourceDTO mapFileDto(Resource resource) {
        return new FileDTO(
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
                list.add(mapDirectoryDTO(path, ""));
            }
        }

        return list;
    }

    public List<ResourceDTO> mapDirectoriesDto(List<Resource> resources) {
        List<ResourceDTO> list = new ArrayList<>();
        for (Resource resource : resources) {
            ResourceDTO dto = resource.getType().equals(ResourceType.DIRECTORY)
                    ? mapDirectoryDTO(resource, "/") : mapFileDto(resource);
            list.add(dto);
        }
        return list;
    }

    public DirectoryDTO mapDirectoryDTO(Resource resource, String end) {
        return new DirectoryDTO(
                resource.getPath().substring(resource.getPath().indexOf("/") + 1),
                resource.getName() + end,
                ResourceType.DIRECTORY
        );
    }
}
