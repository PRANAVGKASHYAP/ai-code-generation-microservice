package com.micro.workspace_service.repository;

import com.micro.common_lib.enums.ProjectRole;
import com.micro.workspace_service.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProjectRepository extends JpaRepository<Project , Long> {

    @Query(
            """
            select p as project , pm.role as role
            from Project p join ProjectMember pm
            on p.id = pm.project.id
            where p.deletedAt is NULL
            AND pm.id.userId = :userId
            ORDER BY p.createdAt DESC
            """
    )
    List<ProjectWithRole> findAllAccessibleByUser( @Param("userId") Long userId) ;

    @Query(
            """
            select p from Project p
            where p.id = :projectId
            and exists(
            select 1 from ProjectMember pm
            where pm.id.userId = :userId
            and pm.project.id = p.id
            )
            and p.deletedAt is NULL
            """
    )
    Optional<Project> findAccessibleProjectById( @Param("projectId") Long projectId ,  @Param("userId") Long userId);

    //adding a new method to return project info with user role

    @Query(
            """
            select p as project, pm.role as role from Project p
            join ProjectMember pm on p.id = pm.project.id
            where p.id = :projectId
            and pm.id.userId = :userId
            and p.deletedAt is NULL
            """
    )
    Optional<ProjectWithRole> findAccessibleProjectByIdWithRole( @Param("projectId") Long projectId ,  @Param("userId") Long userId);

    interface ProjectWithRole{
        Project getProject();
        ProjectRole getRole();
    }

}
