package org.andnekon.img_responder.bot.dao;

import org.andnekon.img_responder.bot.model.Action;
import org.andnekon.img_responder.bot.model.Chat;
import org.andnekon.img_responder.bot.model.Resource;
import org.springframework.stereotype.Repository;

@Repository
public class ActionRepository {

    String resourceFile = "database.json";

    public ActionRepository() {

    }

    public void addAction(Action action, Chat chat, Resource r) {
        throw new UnsupportedOperationException("Unimplemented methdo 'addAction'");
    }

    public void removeAction(long actionId, long chatId) {
        throw new UnsupportedOperationException("Unimplemented methdo 'removeAction'");
    }

    public void getActions(long chatId) {
        throw new UnsupportedOperationException("Unimplemented methdo 'getActions'");
    }

    public void removeChat(long chatId) {
        throw new UnsupportedOperationException("Unimplemented methdo 'removeChat'");
    }
}
