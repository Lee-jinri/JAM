package com.jam.mypage.dto;

import com.jam.global.exception.BadRequestException;

// favorite.board_type에 실제로 저장되는 값과 반드시 일치해야 한다 (각 도메인 매퍼의 board_type 리터럴 참고).
// studio는 아직 즐겨찾기 조회 쿼리가 구현되어 있지 않아 제외했다.
public enum BoardType {
    JOB("JOB"),
    COMMUNITY("COM"),
    FLEA_MARKET("FLEA");

    private final String value;

    BoardType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static BoardType fromString(String text) {
        for (BoardType type : BoardType.values()) {
            if (type.value.equalsIgnoreCase(text)) {
                return type;
            }
        }
        throw new BadRequestException("Invalid board type: " + text);
    }
}
