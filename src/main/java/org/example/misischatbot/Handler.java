package org.example.misischatbot;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.example.misischatbot.model.Mailing;
import org.example.misischatbot.model.UserStats;
import org.example.misischatbot.service.MailingService;
import org.example.misischatbot.service.VkUsrService;
import org.springframework.stereotype.Component;
import ru.mail.im.botapi.BotApiClient;
import ru.mail.im.botapi.BotApiClientController;
import ru.mail.im.botapi.api.entity.SendTextRequest;
import ru.mail.im.botapi.fetcher.event.CallbackQueryEvent;
import ru.mail.im.botapi.fetcher.event.Event;
import ru.mail.im.botapi.fetcher.event.NewMessageEvent;
import ru.mail.im.botapi.response.MessageResponse;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@RequiredArgsConstructor
public class Handler {
    private final BotApiClient client;
    private final BotApiClientController controller;

    private final String adminUserId = System.getenv("ADMIN_USER_ID");
    private final MailingService mailingService;
    private final VkUsrService vkUsrService;

    @PostConstruct
    public void init() {
        client.addOnEventFetchListener(events -> {
            for (Event event : events) {
                switch (event.getType()) {
                    case "newMessage":
                        handleMessage((NewMessageEvent) event);
                        break;
                    case "callbackQuery":
                        handleMessageCallback((CallbackQueryEvent) event);

                        break;
                }
            }
        });
    }

    @PostConstruct
    public void init1() {
        client.addOnEventFetchListener(events -> {
            for (Event<?> event : events) {
                System.out.println("--- Новое событие ---");
                System.out.println("Тип: " + event.getType());
                System.out.println("Данные: " + event.getEventId());
                System.out.println("---------------------");
            }
        });
    }

    private void handleMessage(NewMessageEvent event) {
        String text = event.getText();
        String senderId = event.getFrom().getUserId();
        String chatId = event.getChat().getChatId();

        vkUsrService.addNewUsr(senderId);

        if (!adminUserId.equals(senderId)) {
            return;
        }

        try {
            if (text.startsWith("/create")) {
                handleCreateCommand(chatId, text);
            } else if (text.startsWith("/get")) {
                handleGetCommand(chatId, text);
            } else if (text.startsWith("/delete")) {
                handleDeleteCommand(chatId, text);
            } else if (text.startsWith("/stat")) {
                handleStatCommand(chatId, text);
            } else {
                sendHelp(chatId);
            }
        } catch (Exception e) {
            sendText(chatId, "Ошибка: " + e.getMessage());
        }
    }

    private void handleCreateCommand(String chatId, String text) {
        Pattern pattern = Pattern.compile("^/(create)\\s+(once|repeat)\\s+([^\\s]+)\\s+(.*?)(?:\\s*\\{(.*)\\})?$");
        Matcher matcher = pattern.matcher(text);

        if (!matcher.find()) {
            sendText(chatId, "Неверный формат команды. Используйте:\n" +
                    "/create once <дата_время> <текст> {user1,user2,...}\n" +
                    "/create repeat <cron_выражение> <текст> {user1,user2,...}\n" +
                    "Примеры:\n" +
                    "/create once 2025-04-21T17:55:00 Новогоднее поздравление {user123,user456}\n" +
                    "/create repeat \"0 0 9 * * *\" Ежедневное уведомление");
            return;
        }

        Mailing mailing = new Mailing();
        mailing.setText(matcher.group(4));

        if ("once".equalsIgnoreCase(matcher.group(2))) {
            mailing.setType(Mailing.MailingType.ONCE);
            try {
                mailing.setSendTime(LocalDateTime.parse(matcher.group(3), DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            } catch (DateTimeParseException e) {
                sendText(chatId, "Неверный формат даты. Используйте yyyy-MM-ddTHH:mm:ss");
                return;
            }
        } else {
            mailing.setType(Mailing.MailingType.REPEATING);
            mailing.setCronExpression(matcher.group(3));
        }

        if (matcher.group(5) != null) {
            Arrays.stream(matcher.group(5).split(","))
                    .map(String::trim)
                    .filter(id -> !id.isEmpty())
                    .forEach(userId -> mailing.getUserStats().put(userId,
                            new UserStats(false, false, null, null, 0)));
        }

        mailingService.create(mailing);
        sendText(chatId, "Рассылка создана с ID: " + mailing.getId());
    }

    private void handleGetCommand(String chatId, String text) {
        String[] parts = text.split(" ");
        if (parts.length == 1) {
            List<Mailing> mailings = mailingService.getAll();
            if (mailings.isEmpty()) {
                sendText(chatId, "Нет созданных рассылок");
                return;
            }

            StringBuilder response = new StringBuilder("Все рассылки:\n");
            mailings.forEach(m -> response.append(formatMailingShort(m)).append("\n"));
            sendText(chatId, response.toString());
        } else {
            try {
                Long id = Long.parseLong(parts[1]);
                mailingService.getById(id).ifPresentOrElse(
                        mailing -> sendText(chatId, formatMailingFull(mailing)),
                        () -> sendText(chatId, "Рассылка с ID " + id + " не найдена")
                );
            } catch (NumberFormatException e) {
                sendText(chatId, "Неверный формат ID");
            }
        }
    }

    private void handleDeleteCommand(String chatId, String text) {
        String[] parts = text.split(" ");
        if (parts.length != 2) {
            sendText(chatId, "Используйте: /delete <ID_рассылки>");
            return;
        }

        try {
            Long id = Long.parseLong(parts[1]);
            mailingService.delete(id);
            sendText(chatId, "Рассылка с ID " + id + " удалена");
        } catch (NumberFormatException e) {
            sendText(chatId, "Неверный формат ID");
        }
    }


    private void handleStatCommand(String chatId, String text) {
        String[] parts = text.split(" ");
        if (parts.length == 1) {
            sendText(chatId, mailingService.getAllStatistics());
        } else {
            try {
                Long id = Long.parseLong(parts[1]);
                sendText(chatId, mailingService.getStatistics(id));
            } catch (NumberFormatException e) {
                sendText(chatId, "Неверный формат ID");
            }
        }
    }

    private String formatMailingStats(Mailing mailing) {
        int total = mailing.getUserStats().size();
        long sent = mailing.getUserStats().values().stream().filter(UserStats::isSent).count();
        long read = mailing.getUserStats().values().stream().filter(UserStats::isRead).count();

        return String.format(
                "📌 Рассылка #%d\n" +
                        "📅 %s\n" +
                        "✉️ Отправлено: %d/%d (%.1f%%)\n" +
                        "✓ Доставлено: %d (%.1f%%)\n" +
                        "👁️ Прочитано: %d (%.1f%%)",
                mailing.getId(),
                mailing.getType() == Mailing.MailingType.ONCE ?
                        "Одноразовая: " + mailing.getSendTime().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")) :
                        "Повторяющаяся: " + mailing.getCronExpression(),
                sent, total, (sent * 100.0 / total),
                read, (read * 100.0 / total)
        );
    }

    private String formatDetailedStats(Mailing mailing) {
        StringBuilder sb = new StringBuilder();
        sb.append(formatMailingStats(mailing));

        sb.append("\n\n👤 Детали по пользователям:\n");

        mailing.getUserStats().forEach((userId, stats) -> {
            sb.append(String.format(
                    "\n🔹 %s: %s%s%s",
                    userId,
                    stats.isSent() ? "✓ отправлено" : "× не отправлено",
                    stats.isRead() ? ", 👁️ прочитано" : ""
            ));

            if (stats.isRead() && stats.getReadTime() != null) {
                sb.append(" в ").append(stats.getReadTime().format(DateTimeFormatter.ofPattern("HH:mm dd.MM.yyyy")));
            }
        });

        return sb.toString();
    }

    private String formatMailingShort(Mailing mailing) {
        return String.format("#%d: %s (%s)",
                mailing.getId(),
                mailing.getText(),
                mailing.getType() == Mailing.MailingType.ONCE ?
                        "одноразовая" : "повторяющаяся");
    }

    private String formatMailingFull(Mailing mailing) {
        return String.format(
                "Рассылка #%d\nТип: %s\nТекст: %s\n" +
                        "Дата создания: %s\n" +
                        (mailing.getType() == Mailing.MailingType.ONCE ?
                                "Время отправки: %s" : "Cron выражение: %s"),
                mailing.getId(),
                mailing.getType() == Mailing.MailingType.ONCE ? "Одноразовая" : "Повторяющаяся",
                mailing.getText(),
                mailing.getCreateDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")),
                mailing.getType() == Mailing.MailingType.ONCE ?
                        mailing.getSendTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) :
                        mailing.getCronExpression()
        );
    }

    private void sendHelp(String chatId) {
        sendText(chatId,
                "Доступные команды:\n" +
                        "/create <once/repeat> [дата/cron] <текст> {usrId1,usrId2,...}- создать рассылку\n" +
                        "/get [id] - получить информацию о рассылке(ах)\n" +
                        "/delete <id> - удалить рассылку\n" +
                        "/stat [id] - получить статистику\n" +
                        "\nПримеры:\n" +
                        "/create once 2023-12-31 23:59 Новогоднее поздравление\n" +
                        "/create repeat \"0 0 9 * * *\" Ежедневное уведомление\n" +
                        "/get\n/delete 1\n/stat 1");
    }

    private long sendText(String chatId, String text) {
        try {
            MessageResponse response = controller.sendTextMessage(
                    new SendTextRequest()
                            .setChatId(chatId)
                            .setText(text)
            );

            return response.getMsgId();
        } catch (IOException e) {
            e.printStackTrace();
            return -1;
        }
    }

    private void handleMessageCallback(CallbackQueryEvent event) {
        mailingService.readMessage(event.getFrom().getUserId());
    }

}
