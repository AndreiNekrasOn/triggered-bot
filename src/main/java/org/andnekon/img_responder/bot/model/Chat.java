package org.andnekon.img_responder.bot.model;

import java.sql.Date;

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
    Date authFrom;

    /** Date when chat stops being authenticated */
    Date authTo;
}
