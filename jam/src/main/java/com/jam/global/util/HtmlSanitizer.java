package com.jam.global.util;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.safety.Safelist;

public final class HtmlSanitizer {
	private HtmlSanitizer() {}
	
	/**
	 * 입력 문자열에 HTML 태그가 포함되어 있는지 검사합니다.
	 *
	 * @param input 검사할 문자열
	 * @return true - HTML 태그가 포함되어 있음 / false - 순수 텍스트
	 */
	public static boolean hasHtmlTag(String input) {
		String cleaned = Jsoup.clean(input, Safelist.none());
		return !cleaned.equals(input);
	}
	
	public static String sanitizeHtml(String html) {
		if (html == null) {
			return null;
		}

		Safelist safelist = Safelist.none()
			.addTags("br", "p", "b", "strong", "i", "em", "ul", "ol", "li", "span")
			.addAttributes("span", "style");

		return Jsoup.clean(html, "", safelist, new Document.OutputSettings().prettyPrint(false));
	}
}
