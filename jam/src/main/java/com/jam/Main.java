package com.jam;


public class Main {

	public static void main(String[] args) {
		String str ="동작구 전체 "
				+ "노량진동 "
				+ "노량진1동 "
				+ "노량진2동 "
				+ "대방동 "
				+ "동작동 "
				+ "본동 "
				+ "사당동 "
				+ "사당1동 "
				+ "사당2동 "
				+ "사당3동 "
				+ "사당4동 "
				+ "사당5동 "
				+ "상도동 "
				+ "상도1동 "
				+ "상도2동 "
				+ "상도3동 "
				+ "상도4동 "
				+ "신대방동 "
				+ "신대방1동 "
				+ "신대방2동 "
				+ "흑석동";
		
		String[] strArr = str.split(" ");
		for(String s : strArr) {
			System.out.print("\"" + s + "\" , ");
		}

	}

}
