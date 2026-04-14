package com.example.cloudfilestorage.Repository;

import com.example.cloudfilestorage.Entity.Resource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ResourceRepository extends JpaRepository<Resource, Long> {
    @Query("SELECT r FROM resources WHERE path = :path AND owner_id = :ownerId")
    Optional<Resource> findResourceByOwnerIdAndPath(@Param("ownerId") Integer ownerId,
                                                    @Param("path") String path);



}

