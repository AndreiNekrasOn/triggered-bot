package org.andnekon.img_responder.bot.model;

import java.time.LocalDate;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name="chats")
public class Chat {

    /** Chat unique identifeir */
    @Id
    Long id;

    /** Date when chat was added to authenticated list */
    LocalDate authFrom;

    /** Date when chat stops being authenticated */
    LocalDate authTo;

    public Chat() {
    }

    public Chat(Long id, LocalDate today, LocalDate monthAfter) {
        this.id = id;
        this.authFrom = today;
        this.authTo = monthAfter;
    }

    public Long getId() {
        return id;
    }

    public LocalDate getAuthFrom() {
        return authFrom;
    }

    public LocalDate getAuthTo() {
        return authTo;
    }
}
