package com.jam.global.service;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Service;

import com.jam.s3.service.S3Service;

@Service
public class PostImageViewService {
	private final S3Service s3Service;

	public PostImageViewService(S3Service s3Service) {
		this.s3Service = s3Service;
	}
	
	public String injectViewUrls(String contentHtml) {
		if (contentHtml == null || contentHtml.isBlank()) return contentHtml;

		Document doc = Jsoup.parseBodyFragment(contentHtml);
		for (Element img : doc.select("img[data-key]")) {
			String key = img.attr("data-key");
			String viewUrl = s3Service.generatePresignedViewUrl(key);
			img.attr("src", viewUrl);
		}

		return doc.body().html();
	}
}
