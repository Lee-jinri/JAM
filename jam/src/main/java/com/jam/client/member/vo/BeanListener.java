package com.jam.client.member.vo;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import com.jam.client.chat.service.ChatServiceImpl;

@Component
public class BeanListener implements InitializingBean{


    @Autowired
    private ApplicationContext context;

    @Override
    public void afterPropertiesSet() throws Exception {
        // chatServiceImpl 빈을 가져옴
        ChatServiceImpl chatService = (ChatServiceImpl) context.getBean("chatServiceImpl");

        // chatServiceImpl 빈이 등록된 ApplicationContext 확인
        ApplicationContext chatServiceContext = context;

        System.out.println("chatServiceImpl 빈이 등록된 ApplicationContext ID: " + chatServiceContext.getId());

        if (chatServiceContext.getParent() != null) {
            System.out.println("chatServiceImpl의 부모 ApplicationContext ID: " + chatServiceContext.getParent().getId());
        } else {
            System.out.println("chatServiceImpl의 부모 ApplicationContext는 없습니다.");
        }
    }
}
