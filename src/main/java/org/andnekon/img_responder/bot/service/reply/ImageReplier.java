package org.andnekon.img_responder.bot.service.reply;

import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.generics.TelegramClient;

public class ImageReplier extends MessageReplier {

    public ImageReplier(TelegramClient tc, long chatId, ReplyType rt) {
        super(tc, chatId, rt);
        assert switch (rt) {
            case IMAGE -> true;
            default -> false;
        };
    }

    @Override
    void prepare() {
        if (true) {
            throw new UnsupportedOperationException("Unimplemented method");
        }
        InputFile photoReply = null; // TODO: read
        String caption = "yay!";
        SendPhoto sendPhoto = SendPhoto.builder()
            .chatId(chatId)
            .photo(photoReply)
            .caption(caption)
            .build();
    }
}

