package com.jam.common.vo;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import java.util.List;

@Getter
@RequiredArgsConstructor
public class ReplyResponse<T> {
    private final List<T> replies; // 댓글 목록 (제너릭 사용)
    private final String userId;   // 현재 로그인한 사용자 ID
    private final String userName;
}
