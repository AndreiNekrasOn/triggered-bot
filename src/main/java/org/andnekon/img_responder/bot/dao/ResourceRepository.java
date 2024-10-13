package org.andnekon.img_responder.bot.dao;

import org.andnekon.img_responder.bot.model.Resource;
import org.springframework.data.repository.CrudRepository;

public interface ResourceRepository extends CrudRepository<Resource, Long> {
}
