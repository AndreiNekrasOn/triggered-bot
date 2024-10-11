package org.andnekon.img_responder.bot.response;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.generics.TelegramClient;

public class TextResponder extends MessageResponder  {

    public TextResponder(TelegramClient tc, long chatId, ResponseType rt) {
        super(tc, chatId, rt);
        assert switch (rt) {
            case IMAGE -> true;
            case CORRECTION -> true;
            default -> false;
        };
    }

    @Override
    void prepare() {
        String replyText = switch (rt) {
            case TEXT -> getReply();
            case FUN_TEXT -> getReplyWolf();
            default -> throw new IllegalStateException(
                    String.format("Unreachable ResponseType: %s", rt.toString()));
        };
        SendMessage sendMessage = SendMessage.builder()
            .chatId(chatId)
            .text(replyText)
            .build();
        response = sendMessage;
    }

    private String getReplyWolf() {
        return "Haha";
    }

    private String getReply() {
        return "Hello, World!";
    }
}

