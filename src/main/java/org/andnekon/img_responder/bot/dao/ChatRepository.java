package org.andnekon.img_responder.bot.dao;

import org.andnekon.img_responder.bot.model.Chat;
import org.springframework.data.repository.CrudRepository;

public interface ChatRepository extends CrudRepository<Chat, Long> {
}
