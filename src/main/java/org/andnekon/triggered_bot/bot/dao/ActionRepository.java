package org.andnekon.triggered_bot.bot.dao;

import java.util.List;

import org.andnekon.triggered_bot.bot.model.Action;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ActionRepository extends CrudRepository<Action, Long> {
    List<Action> findAllByChatId(long chatId);
    List<Action> findByIdAndChatId(long id, long chatId);
}
