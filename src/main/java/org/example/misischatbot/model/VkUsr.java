package org.example.misischatbot.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.*;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class VkUsr {

    @Id
    private String usrId;

    private boolean blocked = false;
}
