package org.andnekon.triggered_bot.bot.dao;

import org.andnekon.triggered_bot.bot.model.Chat;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatRepository extends CrudRepository<Chat, Long> {
}
