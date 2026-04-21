package com.example.cloudfilestorage.Repository;

import com.example.cloudfilestorage.Entity.Resource;
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
    void deleteDirectoryByOwnerIdAndPath(@Param("ownerId") Integer ownerId,
                                         @Param("path") String path);

    @Modifying
    @Query("""
            UPDATE Resource r
            SET r.name = :oldName
            WHERE r.type = :type AND r.name = :newName AND r.path = :path
            """)
    void updateNameOfResource(@Param("oldName") String oldName,
                              @Param("newName") String newName,
                              @Param("type") String type,
                              @Param("path") String path);

    @Modifying
    @Query("""
                UPDATE Resource r
                SET r.path = REPLACE(r.path, :oldPath, :newPath)
                WHERE r.path LIKE CONCAT(:oldPath, '%')
            """)
    Resource updatePath(@Param("oldPath") String oldPath,
                   @Param("newPath") String newPath);

    List<Resource> findByNameContainingIgnoreCase(String name);

    Optional<Resource> getResourceByPathAndName(String path, String name);

    Optional<Resource> getResourceByPath(String path);
}

