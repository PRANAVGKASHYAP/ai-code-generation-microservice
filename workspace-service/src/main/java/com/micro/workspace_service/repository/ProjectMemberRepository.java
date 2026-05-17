package com.micro.workspace_service.repository;

import com.micro.workspace_service.entity.ProjectMember;
import com.micro.workspace_service.entity.ProjectMemberId;
import com.micro.common_lib.enums.ProjectRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProjectMemberRepository extends JpaRepository<ProjectMember , ProjectMemberId> {

    List<ProjectMember> findByIdProjectId(Long projectId); // project member uses 2 keys for primary key so appeding the method by ProjectId

    @Query("SELECT pm.role FROM ProjectMember pm " +
            "WHERE pm.id.userId = :userId AND pm.id.projectId = :projectId")
    Optional<ProjectRole> findRoleByProjectIdAndUserId(Long projectId, Long userId);

    @Query("""
            select count(pm) from ProjectMember pm
            where pm.id.userId = :userId and pm.role = com.micro.common_lib.enums.ProjectRole.OWNER
           """)
    Integer countProjectsOwnedByUser(Long userId);
}
