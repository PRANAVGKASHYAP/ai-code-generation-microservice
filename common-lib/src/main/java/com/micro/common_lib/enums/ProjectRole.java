package com.micro.common_lib.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Set;

import static com.micro.common_lib.enums.ProjectPermission.*;


@RequiredArgsConstructor
@Getter
public enum ProjectRole {
      EDITOR(Set.of(VIEW , EDIT , DELETE))
    , VIEWER(Set.of(VIEW))
    , OWNER(Set.of(VIEW , EDIT , DELETE , MANAGE_MEMBERS));


    private final Set<ProjectPermission>permissions;
}
