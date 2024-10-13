package org.andnekon.img_responder.bot.controller;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.message.Message;

@Component
public class ActionController {

    public void createAction(Message message) {
        // parse message
        // call ActionService
        throw new UnsupportedOperationException("Unimplemented method");
    }

    public void removeAction(Message message) {
        throw new UnsupportedOperationException("Unimplemented method");
    }

    public void listActions() {
        throw new UnsupportedOperationException("Unimplemented method");
    }

}
