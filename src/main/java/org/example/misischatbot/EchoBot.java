package org.example.misischatbot;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.mail.im.botapi.BotApiClient;
import ru.mail.im.botapi.BotApiClientController;
import ru.mail.im.botapi.api.entity.SendTextRequest;
import ru.mail.im.botapi.fetcher.event.Event;
import ru.mail.im.botapi.fetcher.event.NewMessageEvent;

import java.io.IOException;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class EchoBot {

    private final BotApiClient client;
    private final BotApiClientController controller;

    @PostConstruct
    public void init() {
        client.addOnEventFetchListener(events -> {
            for (Event event : events) {
                if (Objects.equals(event.getType(), "newMessage")) {
                    handleMessage((NewMessageEvent) event);
                }
            }
        });
    }

    private void handleMessage(NewMessageEvent messageEvent) {
        String text = messageEvent.getText();
        String text2 =" Ёхо";

        String w = text2+text;
        String chatId = messageEvent.getChat().getChatId();
        System.out.println(text);
        try {
            controller.sendTextMessage(
                    new SendTextRequest()
                            .setChatId(chatId)
                            .setText(w)
            );
        } catch (IOException e) {
            throw new RuntimeException("ќшибка отправки сообщени€", e);
        }
    }
    
    
}