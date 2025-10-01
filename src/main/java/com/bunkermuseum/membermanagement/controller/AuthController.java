package com.bunkermuseum.membermanagement.controller;

import com.bunkermuseum.membermanagement.service.contract.RoleServiceContract;
import com.bunkermuseum.membermanagement.service.contract.UserServiceContract;

public class AuthController {
    private final UserServiceContract userService;
    private final RoleServiceContract roleService;

    public AuthController(
            UserServiceContract userService,
            RoleServiceContract roleService
    ) {
        this.userService = userService;
        this.roleService = roleService;
    }
}
