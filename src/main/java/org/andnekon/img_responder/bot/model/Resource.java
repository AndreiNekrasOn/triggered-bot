package org.andnekon.img_responder.bot.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name="resources")
public class Resource {

    /** Resource unique identifier */
    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    Long id;

    /** Id of the chat where the action is registered */
    Long chatId;

    /** Name of the resource, represents its path */
    String name;

    /** Total size occupied by the resource */
    long size;

    public Resource() {
    }

    public Resource(Long chatId, String name) {
        this.chatId = chatId;
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public Long getChatId() {
        return chatId;
    }

    public String getName() {
        return name;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }
}
