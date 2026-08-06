package com.study.studyproject.email.service;

public interface EmailVerificationService {

    void sendCode(String email);

    void verifyCode(String email, String code);

    boolean isVerified(String email);

    void clearVerification(String email);
}
