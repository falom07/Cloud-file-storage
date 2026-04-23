package com.example.cloudfilestorage.Repository;

import com.example.cloudfilestorage.Entity.Resource;
import com.example.cloudfilestorage.Entity.ResourceType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ResourceRepository extends JpaRepository<Resource, Long> {
    @Query("SELECT r FROM Resource r WHERE r.path = :path AND r.owner.id = :ownerId AND r.name = :name")
    Optional<Resource> findResourceByOwnerIdAndPathAndName(@Param("ownerId") Integer ownerId,
                                                           @Param("path") String path,
                                                           @Param("name") String name);

    @Query("SELECT r FROM Resource r WHERE r.path = :path AND r.owner.id = :ownerId")
    List<Resource> findResourcesByOwnerIdAndPath(@Param("ownerId") Integer ownerId,
                                                 @Param("path") String path);

    @Modifying
    @Query("DELETE FROM Resource r WHERE r.owner.id = :ownerId AND r.path LIKE CONCAT(:path, '%')")
    void deleteResourcesByOwnerIdAndPath(@Param("ownerId") Integer ownerId,
                                         @Param("path") String path);

    @Modifying
    @Query("""
            UPDATE Resource r
            SET r.name = :newName, 
            r.path = :newPath
            WHERE r.type = :type AND r.name = :oldName AND r.path = :oldPath AND r.owner.id = :userId 
            """)
    void updateNameAndPathOfResource(@Param("oldName") String oldName,
                                     @Param("newName") String newName,
                                     @Param("type") ResourceType type,
                                     @Param("userId") Integer userId,
                                     @Param("oldPath") String pathFrom,
                                     @Param("newPath") String pathTo);

    @Modifying
    @Query("""
                UPDATE Resource r
                SET r.path = CONCAT(:newPath, SUBSTRING(r.path,LENGTH(:oldPath) + 1))
                WHERE r.path LIKE CONCAT(:oldPath, '%')
            """)
    void updatePaths(@Param("oldPath") String oldPath,
                     @Param("newPath") String newPath);

    @Modifying
    @Query("""
            UPDATE Resource r
            SET r.path = :newPath 
            , r.name = :newName
            WHERE r.type = :type AND r.name = :oldName AND r.path = :oldPath
            """)
    void updateFilePathAndName(
            @Param("oldPath") String pathFrom,
            @Param("newPath") String pathTo,
            @Param("type") ResourceType resourceType,
            @Param("oldName") String fileNameFrom,
            @Param("newName") String fileNameTo);

    List<Resource> findByNameContainingIgnoreCase(String name);

    Optional<Resource> getResourceByPathAndName(String path, String name);

    Optional<Resource> getResourceByPath(String path);

    void deleteDirectoryByOwnerIdAndPathAndName(Integer userId, String fullPath, String directoryName);

    @Query("""
                    SELECT r 
                    FROM Resource r 
                    WHERE r.path = :path AND r.owner.id = :userId AND r.name = :name AND r.type = :type
            """)
    Resource getResourceByPathAndNameAndUserIdAndType(
            @Param("path") String pathTo,
            @Param("name") String nameOfFileTo,
            @Param("userId") Integer userId,
            @Param("type") ResourceType resourceType);
}

