package com.assessment.speernotes.utils;

import com.assessment.speernotes.model.User;
import com.assessment.speernotes.model.UserAuthDto;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class UserUtil {
    @Autowired
    private ModelMapper modelMapper;

    public User convertUserAuthToUser(UserAuthDto userAuthDto) {
        return modelMapper.map(userAuthDto, User.class);
    }

    public UserAuthDto convertUserToUserAuth(User user) {
        return modelMapper.map(user, UserAuthDto.class);
    }
}
