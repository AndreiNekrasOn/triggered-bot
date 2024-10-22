package org.andnekon.triggered_bot.bot.dao;

import java.util.List;
import java.util.Optional;

import org.andnekon.triggered_bot.bot.model.Resource;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ResourceRepository extends CrudRepository<Resource, Long> {

    List<Resource> findAllByChatId(long chatId);
    Optional<Resource> findByChatIdAndName(long chatId, String name);
    List<Resource> findAllByChatIdAndName(long chatId, String name);
    boolean existsByChatIdAndName(long chatId, String name);
}
