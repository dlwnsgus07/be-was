package webApplicationServer.service;

import dto.UserSignUpDto;

public interface UserService {
    void signUp(UserSignUpDto userSignUpDto);
}