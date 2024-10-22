package org.andnekon.triggered_bot.bot.model;

import org.andnekon.triggered_bot.utils.StringUtils;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name="actions")
public class Action {

    /** Action unique identifier */
    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    Long id;

    /** Id of the chat where the action is registered */
    Long chatId;

    /** Action type: {@code "match"} or {@code "time"} */
    String type;

    /** Resource path, by filename or by directory */
    String resource;

    /** Pattern that triggers action. Can be either regex or cron-format datetime.
      * Determines whether to reply with image or text, based on / at the end.
      */
    String pattern;

    /** Optional caption for image or required reply for text */
    String reply;

    public Action() {}

    public Action(Long chatId, String type, String resource, String triggerText, String triggerCron, String reply) {
        this.chatId = chatId;
        this.type = type;
        this.resource = resource;
        this.pattern = triggerText;
        this.reply = reply;
    }

    /** Checks if this action should contain corresponding image */
    public boolean hasImage() {
        boolean result = true;
        result &= result && "match".equals(type);
        result &= result && !StringUtils.isEmtpy(resource);
        result &= result && resource.endsWith("/");
        return result;
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

    public String getReply() {
        return reply;
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

    public void setReply(String reply) {
        this.reply = reply;
    }

    @Override
    public String toString() {
        return "Action [id=" + id + ", chatId=" + chatId + ", type=" + type + ", resource=" + resource + ", pattern="
                + pattern + ", reply=" + reply + "]";
    }
}

