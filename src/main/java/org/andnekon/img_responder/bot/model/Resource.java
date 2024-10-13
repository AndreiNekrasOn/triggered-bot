package org.andnekon.img_responder.bot.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name="resources")
public class Resource {
    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    Long id;
    Long chatId;
    String name;
    long size;
}
