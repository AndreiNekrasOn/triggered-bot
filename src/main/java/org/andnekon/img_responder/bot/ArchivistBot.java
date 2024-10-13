package org.andnekon.img_responder.bot;

import org.andnekon.img_responder.bot.service.ActionService;
import org.andnekon.img_responder.bot.service.response.MessageResponder;
import org.andnekon.img_responder.bot.service.response.MessageResponderFactory;
import org.andnekon.img_responder.bot.service.response.ResponseType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.interfaces.LongPollingUpdateConsumer;
import org.telegram.telegrambots.longpolling.starter.SpringLongPollingBot;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

@Component
public class ArchivistBot implements SpringLongPollingBot, LongPollingSingleThreadUpdateConsumer {

    private final TelegramClient telegramClient;

    private static final String AUTH_TOKEN_ENV = "tgAuthToken";

    private static final Logger logger = LoggerFactory.getLogger(ArchivistBot.class);

    @Autowired
    private ActionService actionService;

    public ArchivistBot() {
        telegramClient = new OkHttpTelegramClient(getBotToken());
    }

    void log(String format, long chatId, Object... args) {
        format = "[chat %d] " + format;
        if (args.length == 0) {
            logger.info(String.format(format, chatId));
        } else {
            logger.info(String.format(format, chatId, args));
        }
    }

    @Override
    public String getBotToken() {
        return System.getenv(AUTH_TOKEN_ENV);
    }

    @Override
    public void consume(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            Message message = update.getMessage();
            if (message.isCommand()) {
                log("Recieved command", message.getChatId());
                processCommand(message);
                return;
            }
            processReply(message);
        }
    }

    private void processReply(Message message) {
        long chatId = message.getChatId();
        ResponseType responseType = getResponseType(message.getText());
        if (!isChatAllowed(chatId)) {
            log("Chat not allowed", chatId);
            return;
        }
        log("Chat allowed %s", chatId, responseType.toString());
        MessageResponder responder = MessageResponderFactory.getResponder(
                telegramClient, message.getChatId(), responseType);
        try {
            responder.execute();
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void processCommand(Message message) {
        String text = message.getText();
        long chatId = message.getChatId();
        try {
            switch (message.getText()) {
                case "/create_action" ->
                    actionService.createAction(chatId, text);
                case "/list_actions" ->
                    actionService.listActions(chatId, text);
                case "/remove_action" ->
                    actionService.removeAction(chatId, text);
                default ->
                    replyError(message.getChatId(), "Not a valid command");
            }
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void replyError(long chatId, String error) throws TelegramApiException {
        MessageResponder responder = MessageResponderFactory.getResponder(
                telegramClient, chatId, ResponseType.ERROR);
        responder.execute();
        log(error, chatId);
    }

    @Override
    public LongPollingUpdateConsumer getUpdatesConsumer() {
        return this;
    }

    private boolean isChatAllowed(long chatId) {
        return String.valueOf(chatId).equals(System.getenv("tgChatId"));
    }

    private ResponseType getResponseType(String text) {
        if (text.contains("img")) {
            return ResponseType.IMAGE;
        } else if (text.length() < 3) {
            return ResponseType.TEXT;
        }
        return ResponseType.NONE;
    }
}
