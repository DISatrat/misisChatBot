package org.example.misischatbot.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import org.example.misischatbot.dao.MailingDAO;
import org.example.misischatbot.model.Mailing;
import org.example.misischatbot.model.UserStats;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class MailingServiceImpl implements MailingService {
    private final MailingDAO mailingDAO;

    @Override
    public List<Mailing> getAll() {
        return mailingDAO.findAll();
    }

    @Override
    public Optional<Mailing> getById(Long id) {
        return mailingDAO.findById(id);
    }

    @Override
    public Mailing create(Mailing mailing) {
        return mailingDAO.save(mailing);
    }

    @Override
    public void delete(Long id) {
        mailingDAO.deleteById(id);
    }

    @Override
    public void readMessage(String id) {
        Mailing mailing = mailingDAO.findByUsrIdInUserStats(id)
                .orElseThrow(() -> new EntityNotFoundException("Mailing with userId " + id + " not found."));

        UserStats stats = mailing.getUserStats().get(id);
        stats.setRead(true);
        stats.setReadTime(LocalDateTime.now());

        mailing.getUserStats().put(id, stats);
        mailingDAO.save(mailing);
    }


    private String formatStats(Mailing mailing) {
        int totalUsers = mailing.getUserStats().size();
        long sent = mailing.getUserStats().values().stream().filter(UserStats::isSent).count();
        long read = mailing.getUserStats().values().stream().filter(UserStats::isRead).count();

        return String.format(
                "Рассылка #%d (%s)\nТекст: %s\nВсего пользователей: %d\n" +
                        "Отправлено: %d\nДоставлено: %d\nПрочитано: %d",
                mailing.getId(),
                mailing.getType() == Mailing.MailingType.ONCE ? "Одноразовая" : "Повторяющаяся",
                mailing.getText(),
                totalUsers,
                sent,
                read
        );
    }
}