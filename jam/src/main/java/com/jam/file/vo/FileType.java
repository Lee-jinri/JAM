package com.jam.file.vo;

public enum FileType {
	PDF("pdf",  "application/pdf", FileCategory.APPLICATION),
    DOC("doc",  "application/msword", FileCategory.APPLICATION),
    DOCX("docx","application/vnd.openxmlformats-officedocument.wordprocessingml.document", FileCategory.APPLICATION),
    HWP("hwp",  "application/x-hwp", FileCategory.APPLICATION),
    
    PNG("png",  "image/png", FileCategory.POST_IMAGE),
    JPG("jpg",  "image/jpeg", FileCategory.POST_IMAGE),
    JPEG("jpeg","image/jpeg", FileCategory.POST_IMAGE),
    WEBP("webp","image/webp", FileCategory.POST_IMAGE),
    GIF("gif",  "image/gif", FileCategory.POST_IMAGE);

    public final String ext;
    public final String mime;
    public final FileCategory category;

    FileType(String ext, String mime, FileCategory category) {
        this.ext = ext;
        this.mime = mime;
        this.category = category;
    }
    
    public static FileType fromExt(String ext) {
        for (FileType t : values()) {
            if (t.ext.equalsIgnoreCase(ext)) return t;
        }
        throw new IllegalArgumentException("허용되지 않은 확장자입니다: " + ext);
    }
}
