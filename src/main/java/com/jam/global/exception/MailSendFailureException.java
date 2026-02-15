package com.jam.global.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
public class MailSendFailureException extends RuntimeException {
	public MailSendFailureException(String message) {
		super(message);
	}
}
