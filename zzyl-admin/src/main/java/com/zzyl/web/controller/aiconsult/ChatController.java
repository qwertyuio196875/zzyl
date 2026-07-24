package com.zzyl.web.controller.aiconsult;

import com.zzyl.common.annotation.Anonymous;
import com.zzyl.common.core.domain.AjaxResult;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/aiconsult/virtualagent")
public class ChatController
{
    @Autowired
    private ChatClient chatClient;

    @Anonymous
    @GetMapping("/chat")
    public AjaxResult chat(String prompt)
    {
        String response = chatClient.prompt()
                .user(prompt)
                .call()
                .content();
        return AjaxResult.success("操作成功", response);
    }
}
