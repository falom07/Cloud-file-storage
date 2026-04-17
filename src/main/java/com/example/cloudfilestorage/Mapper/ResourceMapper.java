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
//
//    public ResourceDTO mapResourceDto(Resource resource){
//        return new ResourceDTO(
//                resource.getPath(),
//                resource.getName(),
//                resource.getSize(),
//                resource.getType(),
//                resource.getOwner(),
//                resource.getCreatedAt()
//        );
//    }

    public FileDTO mapResourceDto(Resource resource) {
        return new FileDTO(
                resource.getPath(),
                resource.getName(),
                resource.getSize(),
                resource.getType()
        );
    }

    public List<FileDTO> mapResourcesDto(List<Resource> resource) {
        List<FileDTO> list = new ArrayList<>();
        for (Resource path : resource) {
            list.add(mapResourceDto(path));
        }

        return list;
    }

    public DirectoryDTO mapDirectoryDTO(Resource resource) {
        return new DirectoryDTO(
                resource.getPath(),
                resource.getName(),
                ResourceType.DIRECTORY
        );
    }
}
