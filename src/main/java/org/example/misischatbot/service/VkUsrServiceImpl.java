package org.example.misischatbot.service;

import lombok.AllArgsConstructor;
import org.example.misischatbot.dao.VkUsrDAO;
import org.example.misischatbot.model.VkUsr;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class VkUsrServiceImpl implements VkUsrService{

    private final VkUsrDAO vkUsrDAO;


    @Override
    public void addNewUsr(String id) {
        boolean exists = vkUsrDAO.existsById(id);
        if (!exists) {
            VkUsr newUser = new VkUsr();
            newUser.setUsrId(id);
            vkUsrDAO.save(newUser);
        }
    }


    @Override
    public List<VkUsr> getUsrList() {
        return vkUsrDAO.findAll();
    }
}
