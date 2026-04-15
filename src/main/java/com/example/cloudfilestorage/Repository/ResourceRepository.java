package com.example.cloudfilestorage.Repository;

import com.example.cloudfilestorage.Entity.Resource;
import jakarta.persistence.criteria.CriteriaBuilder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ResourceRepository extends JpaRepository<Resource, Long> {
    @Query("SELECT r FROM Resource r WHERE r.path = :path AND r.owner_id = :ownerId AND r.name = :name")
    Optional<Resource> findResourceByOwnerIdAndPath(@Param("ownerId") Integer ownerId,
                                                    @Param("path") String path,
                                                    @Param("name") String name);

    @Query("DELETE FROM Resource r WHERE r.owner_id = :ownerId AND r.path LIKE CONCAT(:path, '%')")
    void deleteDirectoryByOwnerIdAndPath(@Param("ownerId") Integer ownerId,
                                         @Param("path") String path);

}

