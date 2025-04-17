package org.example.misischatbot;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.mail.im.botapi.BotApiClientController;
import ru.mail.im.botapi.api.entity.SendTextRequest;
import ru.mail.im.botapi.fetcher.event.CallbackQueryEvent;

import java.io.IOException;

@RestController
@RequestMapping()
public class WebhookController {

    private final BotApiClientController controller;

    public WebhookController(BotApiClientController controller) {
        this.controller = controller;
    }

    @PostMapping("/test")
    public void onEvent(@RequestBody CallbackQueryEvent callbackQueryEvent) throws IOException {
        var chatId = callbackQueryEvent.getMessageChat().getChatId();
        var text = callbackQueryEvent.getMessageText();
        SendTextRequest newMessageEvent = new SendTextRequest();
        newMessageEvent.setText(text);
        newMessageEvent.setChatId(chatId);
        echo(newMessageEvent);
    }

    private void echo(SendTextRequest messageEvent) throws IOException {
        controller.sendTextMessage(messageEvent);
    }
}
