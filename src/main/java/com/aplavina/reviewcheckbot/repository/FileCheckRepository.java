package com.aplavina.reviewcheckbot.repository;

import com.aplavina.reviewcheckbot.model.FileCheck;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface FileCheckRepository extends JpaRepository<FileCheck, String> {
    @Modifying
    @Transactional
    @Query(nativeQuery = true, value = "UPDATE file_check SET count = :count WHERE file_key = :id")
    void setCount(@Param("id") String id, @Param("count") long count);
}