package org.example.misischatbot.service;

import org.example.misischatbot.model.Mailing;

import java.util.List;
import java.util.Optional;

public interface MailingService {

    List<Mailing> getAll();
    Optional<Mailing> getById(Long id);
    Mailing create(Mailing mailing);
    void delete(Long id);
    void readMessage(String id);

}
