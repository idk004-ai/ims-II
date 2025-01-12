package com.khoilnm.ims.mapper;

import com.khoilnm.ims.common.ConstantUtils;
import com.khoilnm.ims.dto.UserDisplayDTO;
import com.khoilnm.ims.model.User;
import com.khoilnm.ims.service.MasterService;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    private final MasterService masterService;

    public UserMapper(MasterService masterService) {
        this.masterService = masterService;
    }

    public UserDisplayDTO toUserDTO(User user) {
        return UserDisplayDTO.builder()
                .id(user.getId())
                .email(user.getEmail())
                .username(user.get_username())
                .fullname(user.getFullName())
                .phoneNo(user.getPhone())
                .role(masterService.findByCategoryAndCategoryId(ConstantUtils.USER_ROLE, user.getRoleId()).getCategoryValue())
                .status(masterService.findByCategoryAndCategoryId(ConstantUtils.USER_STATUS, user.getStatus()).getCategoryValue())
                .department(masterService.findByCategoryAndCategoryId(ConstantUtils.DEPARTMENT, user.getDepartmentId()).getCategoryValue())
                .gender(masterService.findByCategoryAndCategoryId(ConstantUtils.USER_GENDER, user.getGender()).getCategoryValue())
                .address(user.getAddress())
                .dob(user.getDob())
                .note(user.getNote())
                .build();
    }
}
