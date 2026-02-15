package com.jam.file.dto;

public enum FileCategory {
	APPLICATION("applications"),
    POST_IMAGE("images/posts"),
    PROFILE_IMAGE("images/profile");

    private final String prefix;
    FileCategory(String prefix) { this.prefix = prefix; }
    public String prefix() { return prefix; }
}
