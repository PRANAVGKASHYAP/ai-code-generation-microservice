package com.micro.common_lib.DTO;

public record UserDTO(String username , // the user name is the mail id that is used to store in db and to generate the access token
        String id,
        String name
) {

}
