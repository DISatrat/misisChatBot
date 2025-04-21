package org.example.misischatbot.service;

import org.example.misischatbot.model.VkUsr;

import java.util.List;

public interface VkUsrService {

    void addNewUsr(String id);

    List<VkUsr> getUsrList();
}
