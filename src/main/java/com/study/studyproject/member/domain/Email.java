package com.study.studyproject.member.domain;

import com.study.studyproject.global.exception.ex.BadRequestException;
import com.study.studyproject.global.exception.ex.ErrorCode;
import com.study.studyproject.global.exception.ex.NotFoundException;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.regex.Pattern;

import static com.study.studyproject.global.exception.ex.ErrorCode.*;



@Embeddable
public record Email( String address) {
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$");

    public Email {
        if (address == null || address.isBlank()) {
            throw new NotFoundException(NOT_FOUND_EMAIL);
        }

        if (!EMAIL_PATTERN.matcher(address).matches()) {
            throw new BadRequestException(INVALID_EMAIL);
        }
    }

    @Override
    public String toString() {
        return address;
    }

}
