package org.example.misischatbot;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.mail.im.botapi.BotApiClient;
import ru.mail.im.botapi.BotApiClientController;

import java.io.IOException;

@Configuration
public class Config {
    @Bean
    public BotApiClientController botApiClientController() {
        String token = "001.0362936170.1000994003:1011924349";

        BotApiClient client = new BotApiClient(token);
        return BotApiClientController.startBot(client);
    }
    public void echo() throws IOException {

    }
}

