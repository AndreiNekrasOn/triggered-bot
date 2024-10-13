package org.andnekon.img_responder.bot.service.response;

import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.generics.TelegramClient;

public class ImageResponder extends MessageResponder {

    public ImageResponder(TelegramClient tc, long chatId, ResponseType rt) {
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
        // TODO Auto-generated method stub
        SendPhoto sendPhoto = SendPhoto.builder()
            .chatId(chatId)
            .photo(photoReply)
            .caption(caption)
            .build();
    }
}

