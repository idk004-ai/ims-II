package com.khoilnm.ims.controller;

import com.khoilnm.ims.common.ConstantUtils;
import com.khoilnm.ims.dto.BaseDTO;
import com.khoilnm.ims.dto.UserDisplayDTO;
import com.khoilnm.ims.service.UserReadService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/user")
public class UserController {
    private final UserReadService userReadService;

    public UserController(UserReadService userReadService) {
        this.userReadService = userReadService;
    }

    @GetMapping("/profile")
    public String getProfile() {
        return "Profile";
    }

    @Secured(ConstantUtils.ADMIN)
    @GetMapping("/get-all-user")
    public ResponseEntity<?> getAllUser(BaseDTO base) {
        List<UserDisplayDTO> users = userReadService.getAllUsers(base.getPage(), base.getPageSize());
        return ResponseEntity.ok().body(users);
    }
}
