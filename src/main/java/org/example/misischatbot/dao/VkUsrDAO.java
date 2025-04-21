package org.example.misischatbot.dao;

import org.example.misischatbot.model.VkUsr;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VkUsrDAO extends JpaRepository<VkUsr, String> {

}
