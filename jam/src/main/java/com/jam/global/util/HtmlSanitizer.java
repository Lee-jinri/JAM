package com.jam.global.util;

import java.util.Arrays;
import java.util.stream.Collectors;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.parser.Parser;
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
	
	public static String sanitizeTitle(String title) {
	    if (title == null) {
	        return "";
	    }
	    
	    String unescaped = Parser.unescapeEntities(title, true);
	    
	    // 모든 HTML 태그를 허용하지 않음 
	    String cleaned = Jsoup.clean(unescaped, Safelist.none());
	    return Jsoup.parse(cleaned).text();
	}
	
	public static String sanitizeHtml(String html) {
		if (html == null) {
			return null;
		}
		String unescapedHtml = Parser.unescapeEntities(html, true);
		
		Safelist safelist = Safelist.none() 
				.addTags("br", "p", "b", "strong", "i", "em", "ul", "ol", "li", "span", "a", "img")
				.addAttributes("span", "style")
				.addAttributes("p", "style")
				.addAttributes("img", "style", "src", "alt", "width", "height")
				
				// 프로토콜 제한 (javascript 금지)
				.addProtocols("a", "href", "http", "https", "mailto")
		        .addProtocols("img", "src", "http", "https");
		
		// 모든 링크에 rel="nofollow"를 붙여 SEO 스팸 방지
	    safelist.addEnforcedAttribute("a", "rel", "nofollow");

	    String cleaned = Jsoup.clean(unescapedHtml, "", safelist, new Document.OutputSettings().prettyPrint(false));
		
	    Document doc = Jsoup.parseBodyFragment(cleaned);

		// p / span 태그에 style 필터 적용
		doc.select("p[style], span[style]").forEach(el -> {
			el.attr("style", filterStyle(el.attr("style")));
		});
		
		doc.select("img[style]").forEach(img -> {
			img.attr("style", filterImgStyle(img.attr("style")));
		});

		return doc.body().html();
	}
	
	private static String filterImgStyle(String style) {
		if (style == null) return "";

		return Arrays.stream(style.split(";"))
				.filter(s -> s.contains(":"))
				.map(String::trim)
				.filter(s -> {
					String lower = s.toLowerCase().split(":")[0].trim();
					return lower.equals("width")
						|| lower.equals("height")
						|| lower.equals("max-width");
				})
				.collect(Collectors.joining("; "));
	}

	private static String filterStyle(String style) {
		if (style == null) return "";

		return Arrays.stream(style.split(";"))
			.map(String::trim)
			.filter(s -> {
				String prop = s.toLowerCase().split(":")[0].trim();
				return prop.equals("color") 
					|| prop.equals("background-color")  
					|| prop.equals("font-size") 
					|| prop.equals("text-align");
			})
			.collect(Collectors.joining("; "));
	}
}
