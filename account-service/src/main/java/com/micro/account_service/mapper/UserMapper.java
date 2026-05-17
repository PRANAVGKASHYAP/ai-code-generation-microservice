package com.micro.account_service.mapper;


import com.micro.account_service.dto.auth.SignUpRequest;
import com.micro.account_service.dto.auth.UserProfileResponse;
import com.micro.account_service.entity.User;
import com.micro.common_lib.DTO.UserDTO;
import com.micro.common_lib.security.JwtUserContext;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {

    User signUpReqToUser(SignUpRequest request);
    UserProfileResponse userToUserProfileResponse(User user);
    UserDTO UserToUserDTO(User user);
    @Mapping(source = "userId" , target = "id")
    UserProfileResponse jwtProncipleToUserProfileResponse(JwtUserContext jwtUserContext);
}
