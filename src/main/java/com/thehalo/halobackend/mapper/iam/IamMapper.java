package com.thehalo.halobackend.mapper.iam;

import com.thehalo.halobackend.dto.iam.response.StaffSummaryResponse;
import com.thehalo.halobackend.model.user.AppUser;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface IamMapper {

    @Mapping(source = "id", target = "userId")
    @Mapping(source = "role.name", target = "role")
    @Mapping(target = "active", expression = "java(user.getDeletedAt() == null)")
    StaffSummaryResponse toStaffSummary(AppUser user);

    List<StaffSummaryResponse> toStaffSummaryList(List<AppUser> users);
}