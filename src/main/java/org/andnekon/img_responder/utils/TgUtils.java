package org.andnekon.img_responder.utils;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.meta.api.objects.message.Message;

public class TgUtils {

    private static Logger logger = LoggerFactory.getLogger(TgUtils.class);

    public static Optional<String> getMessageText(Message message) {
        if (message.hasText()) {
            return Optional.of(message.getText());
        } else if (message.hasCaption()) {
            return Optional.of(message.getCaption());
        }
        logger.error("Illegal message type: {}", message.toString());
        return Optional.empty();
    }

}
