package com.jam.common.dto;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class PageDto {
	private int startPage;
	private int endPage;
	private boolean prev, next;
	
	private int total;
	private CommonDto cvo;
	
	public PageDto(CommonDto cvo, int total) {
		this.cvo = cvo;
		this.total = total;
		this.calculatePaging(cvo.getPageNum(), cvo.getAmount());
	}
	
    public PageDto(int pageNum, int amount, int total) {
        this.total = total;
        this.calculatePaging(pageNum, amount);
    }

	private void calculatePaging(int pageNum, int amount) {
		/*페이징의 끝번호(endPage) 구하기
		 * this.endPage = (int) (Math.ceil 정수로 반올림 (페이지번호 / 10.0)) * 10;*/
		this.endPage = (int) (Math.ceil(cvo.getPageNum() / 10.0)) * 10; 
		
		/*페이지의 시작번호(startPage) 구하기*/
		this.startPage = this.endPage - 9; 
		
		/*끝 페이지 구하기*/
		int realEnd = (int) (Math.ceil((total * 1.0) / cvo.getAmount()));
		
		/*realEnd가 endPage와 같거나 작은 경우, endPage 값 변경*/
		if(realEnd <= this.endPage) {
			this.endPage = realEnd;
		}
		
		/* 이전페이지 구하기 : startPage(시작 페이지)값이 1보다 크면 true*/
		this.prev = this.startPage > 1;
		
		/*다음(next)구하기 : endPage(전체 페이지)값이 끝 페이지보다 작으면 true*/
		this.next = this.endPage < realEnd;
	}
}
