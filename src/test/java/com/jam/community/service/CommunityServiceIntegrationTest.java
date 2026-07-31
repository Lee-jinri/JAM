package com.jam.community.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.jam.community.dto.CommunityDto;

@SpringBootTest
public class CommunityServiceIntegrationTest {
	
	@Autowired
    private CommunityService communityService;
	
	@Test
	@DisplayName("A: 검색어 없이 조회하면 전체 리스트를 반환한다")
	void getBoard_noKeyword() {
	    // Given
	    CommunityDto com = new CommunityDto();
	    com.setPageNum(1);
	    com.setKeyword(""); // 검색어 없음
	    
	    // When
	    List<CommunityDto> result = communityService.getBoard(com);
	    
	    // Then
	    assertThat(result).isNotNull();
	    assertThat(result).isNotEmpty(); // 데이터가 최소 1개는 있다고 가정
	}
	
	@Test
	@DisplayName("B: '기타'로 검색하면 제목이나 내용에 '기타'가 포함된 게시글만 반환한다")
	void getBoard_withKeyword() {
	    // Given
	    String searchKeyword = "기타";
	    CommunityDto com = new CommunityDto();
	    com.setPageNum(1);
	    com.setKeyword(searchKeyword);
	    
	    // When
	    List<CommunityDto> result = communityService.getBoard(com);
	    
	    // Then
	    // 결과 리스트의 모든 항목이 '기타'라는 단어를 포함하고 있는지 확인
	    assertThat(result).allMatch(post -> 
	        post.getTitle().contains(searchKeyword) || 
	        post.getContent().contains(searchKeyword)
	    );
	}
	
	@Test
	@DisplayName("C: 일치하는 검색 결과가 없으면 빈 리스트를 반환한다")
	void getBoard_emptyResult() {
	    // Given
	    CommunityDto com = new CommunityDto();
	    com.setPageNum(1);
	    com.setKeyword("절대없을것같은검색어12345");
	    
	    // When
	    List<CommunityDto> result = communityService.getBoard(com);
	    
	    // Then
	    assertThat(result).isEmpty(); // 리스트가 null은 아니지만 비어있어야 함 (size=0)
	}
	
	@Test
	@DisplayName("D: 사용자 아이디가 있다면 즐겨찾기 여부를 포함하여 반환한다")
	void getBoard_withUser() {
	    CommunityDto com = new CommunityDto();
	    com.setPageNum(1);
	    com.setUser_id("abcd1234");
	    
	    List<CommunityDto> result = communityService.getBoard(com);
	    
	    assertThat(result).isNotNull();
	    assertThat(result).anyMatch(post -> post.isFavorite() == true);
	}
	
	@Test
	@DisplayName("E: 사용자 아이디가 없다면 모든 게시물의 즐겨찾기 여부는 false이다")
	void getBoard_noUser() {
	    CommunityDto com = new CommunityDto();
	    com.setPageNum(1);
	    
	    List<CommunityDto> result = communityService.getBoard(com);
	    
	    assertThat(result).isNotEmpty();
	    assertThat(result).allMatch(post -> post.isFavorite() == false);
	}
}
