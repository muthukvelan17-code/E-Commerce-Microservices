package com.ecommerce.userservice.dto;

import com.ecommerce.userservice.model.Role;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserDto {
    private String id;
    private String email;
    private String name;
    private String role;
}
