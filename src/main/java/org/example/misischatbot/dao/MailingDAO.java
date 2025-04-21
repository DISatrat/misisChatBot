package org.example.misischatbot.dao;

import org.example.misischatbot.model.Mailing;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MailingDAO extends JpaRepository<Mailing, Long> {

    @Query("SELECT m FROM Mailing m JOIN m.userStats stats WHERE KEY(stats) = :usrId")
    Optional<Mailing> findByUsrIdInUserStats(@Param("usrId") String usrId);
}
