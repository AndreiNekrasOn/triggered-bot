package org.andnekon.img_responder.bot.model;

import java.sql.Date;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name="chats")
public class Chat {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    Long id;
    Date authFrom;
    Date authTo;
}
