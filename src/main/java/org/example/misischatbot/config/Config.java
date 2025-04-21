package org.example.misischatbot.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.mail.im.botapi.BotApiClient;
import ru.mail.im.botapi.BotApiClientController;

@Configuration
public class Config {

    @Bean
    public BotApiClient botApiClient() {
        String token = "001.0362936170.1000994003:1011924349";
        return new BotApiClient(token);
    }

    @Bean
    public BotApiClientController botApiClientController(BotApiClient client) {
        return BotApiClientController.startBot(client);
    }
}

