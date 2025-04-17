package org.example.misischatbot;

import org.springframework.stereotype.Component;
import ru.mail.im.botapi.BotApiClient;
import ru.mail.im.botapi.BotApiClientController;
import ru.mail.im.botapi.fetcher.Chat;
import ru.mail.im.botapi.fetcher.event.Event;

import java.io.IOException;

@Component
public class EchoBot {

//    @Value("${myteam.bot.token}")
    private String token = "001.0362936170.1000994003:1011924349";
    BotApiClient client = new BotApiClient(token);
    BotApiClientController controller = BotApiClientController.startBot(client);

    public void echo() throws IOException {
        controller.
        Chat chat = new Chat();
        var chatID = chat.getChatId();

    }
}
