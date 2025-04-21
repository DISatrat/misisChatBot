package org.example.misischatbot;

import lombok.RequiredArgsConstructor;
import org.example.misischatbot.model.Mailing;
import org.example.misischatbot.model.UserStats;
import org.example.misischatbot.model.VkUsr;
import org.example.misischatbot.service.MailingService;
import org.example.misischatbot.service.VkUsrServiceImpl;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.support.CronExpression;
import ru.mail.im.botapi.BotApiClientController;
import ru.mail.im.botapi.api.entity.InlineKeyboardButton;
import ru.mail.im.botapi.api.entity.SendTextRequest;
import ru.mail.im.botapi.response.MessageResponse;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Configuration
@EnableScheduling
@RequiredArgsConstructor
public class MailingScheduler {

    private final String adminUserId = System.getenv("ADMIN_USER_ID");

    private final MailingService mailingService;
    private final BotApiClientController controller;
    private final VkUsrServiceImpl vkUsrService;

    @Scheduled(fixedRate = 600)
    public void checkMailings() {

        LocalDateTime now = LocalDateTime.now();

        mailingService.getAll().stream()
                .filter(m -> !m.isSent())
                .filter(m -> m.getType() == Mailing.MailingType.ONCE
                        ? m.getSendTime() != null && m.getSendTime().isBefore(now)
                        : shouldSendNow(m, now))
                .peek(m -> System.out.println("Найдена рассылка для отправки: " + m.getId()))
                .forEach(this::sendMailing);
    }

    private boolean shouldSendNow(Mailing mailing, LocalDateTime now) {
        try {
            CronExpression cron = CronExpression.parse(mailing.getCronExpression());
            return cron.next(now.minusSeconds(1)) != null &&
                    Objects.requireNonNull(cron.next(now.minusSeconds(1))).isBefore(now.plusSeconds(1));
        } catch (Exception e) {
            return false;
        }
    }

    private void sendMailing(Mailing mailing) {
        System.out.println("Начало отправки рассылки ID: " + mailing.getId());

        List<String> userIds = mailing.getUserStats().isEmpty()
                ? getAllUserIds()
                : new ArrayList<>(mailing.getUserStats().keySet());

        System.out.println("Получатели: " + userIds);

        for (String userId : userIds) {
            try {
                System.out.println("Попытка отправить пользователю: " + userId);

                InlineKeyboardButton readButton = InlineKeyboardButton.callbackButton(
                        "👀 Прочитано",
                        "read_" + mailing.getId() + "_" + userId,
                        null
                );
                List<List<InlineKeyboardButton>> keyboard = List.of(List.of(readButton));

                MessageResponse message = controller.sendTextMessage(
                        new SendTextRequest()
                                .setChatId(userId)
                                .setText(mailing.getText())
                                .setKeyboard(keyboard)
                );
                long msgId = message.getMsgId();

                System.out.println("Сообщение с кнопкой отправлено: " + userId);

                mailing.getUserStats().compute(userId, (k, v) -> {
                    if (v == null) {
                        return new UserStats(true, false, LocalDateTime.now(), null, msgId);
                    }
                    v.setSent(true);
                    v.setSentTime(LocalDateTime.now());
                    return v;
                });

            } catch (Exception e) {
                System.err.println("Ошибка отправки пользователю " + userId + ": " + e.getMessage());
            }
        }

        mailing.setSent(true);
        mailingService.create(mailing);
        System.out.println("Рассылка завершена. ID: " + mailing.getId());
    }

    private List<String> getAllUserIds() {
        List<String> ids = vkUsrService.getUsrList().stream()
                .map(VkUsr::getUsrId)
                .collect(Collectors.toList());

        if (ids.isEmpty()) {
            return List.of(adminUserId);
        }

        return ids;
    }
}