package com.study.studyproject.board.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.study.studyproject.global.exception.ex.ErrorCode;
import com.study.studyproject.global.exception.ex.NotFoundException;

public enum ConnectionType {


    OFFLINE("오프라인"), ONLINE("온라인");

    @JsonValue
    private String description;

    ConnectionType(String description) {
        this.description = description;
    }

    public String getName() {
        return description;
    }


    @JsonCreator
    public static ConnectionType fromValue(String value) {
        if ("ONLINE".equalsIgnoreCase(value)) {
            return ONLINE;
        }
        if ("OFFLINE".equalsIgnoreCase(value)) {
            return OFFLINE;
        }

        throw new NotFoundException(ErrorCode.NOT_FOUND_VALUE);

    }


}
