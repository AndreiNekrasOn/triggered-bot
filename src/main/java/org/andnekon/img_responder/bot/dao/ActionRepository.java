package org.andnekon.img_responder.bot.dao;

import java.util.List;

import org.andnekon.img_responder.bot.model.Action;
import org.springframework.data.repository.CrudRepository;

public interface ActionRepository extends CrudRepository<Action, Long> {
    List<Action> findByChatId(long chatId);
}
