package com.example.cloudfilestorage.Mapper;

import com.example.cloudfilestorage.DTO.FileDTO;
import com.example.cloudfilestorage.DTO.ResourceDTO;
import com.example.cloudfilestorage.DTO.UserDTO;
import com.example.cloudfilestorage.Entity.Resource;
import com.example.cloudfilestorage.Entity.User;
import org.springframework.stereotype.Component;

@Component
public class ResourceMapper {

    public ResourceDTO mapResourceDto(Resource resource){
        return new ResourceDTO(
                resource.getPath(),
                resource.getName(),
                resource.getSize(),
                resource.getType(),
                resource.getOwner(),
                resource.getCreatedAt()
        );
    }

    public FileDTO mapFileDto(ResourceDTO resource) {
        return new FileDTO(
                resource.path(),
                resource.name(),
                resource.size(),
                resource.type()
        );
    }
}
