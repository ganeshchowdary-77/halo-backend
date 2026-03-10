package com.thehalo.halobackend.mapper.profile;

import com.thehalo.halobackend.dto.profile.response.ProfileDetailResponse;
import com.thehalo.halobackend.dto.profile.response.ProfileSummaryResponse;
import com.thehalo.halobackend.model.profile.UserProfile;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ProfileMapper {

    @Mapping(source = "platform.name", target = "platformName")
    ProfileSummaryResponse toSummaryDto(UserProfile profile);

    @Mapping(source = "platform.name", target = "platformName")
    @Mapping(source = "platform.description", target = "platformDescription")
    ProfileDetailResponse toDetailDto(UserProfile profile);
}
