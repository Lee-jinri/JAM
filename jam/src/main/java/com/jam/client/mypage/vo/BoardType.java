package com.jam.client.mypage.vo;

public enum BoardType {
	JOB("job"),
    COMMUNITY("community"),
    FLEA_MARKET("fleaMarket"),
    ROOM_RENTAL("roomRental");

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
        throw new IllegalArgumentException("Invalid board type: " + text);
    }
}
