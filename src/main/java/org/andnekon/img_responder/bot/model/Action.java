package org.andnekon.img_responder.bot.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name="actions")
public class Action {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    Long id;
    Long chatId;
    String type;
    String resource;
    String pattern;

    public Action() {}

    public Action(Long chatId, String type, String resource, String triggerText, String triggerCron) {
        this.chatId = chatId;
        this.type = type;
        this.resource = resource;
        this.pattern = triggerText;
    }

    public Long getId() {
        return id;
    }
    public Long getChatId() {
        return chatId;
    }
    public String getType() {
        return type;
    }
    public String getResource() {
        return resource;
    }
    public String getPattern() {
        return pattern;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setChatId(Long chatId) {
        this.chatId = chatId;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setResource(String resource) {
        this.resource = resource;
    }

    public void setPattern(String triggerText) {
        this.pattern = triggerText;
    }

    @Override
    public String toString() {
        return "Action [id=" + id + ", chatId=" + chatId + ", type=" + type + ", resource=" + resource + ", pattern="
                + pattern + "]";
    }
}
