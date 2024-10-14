package org.andnekon.img_responder.bot.controller;

import org.andnekon.img_responder.bot.service.action.ActionService;
import org.andnekon.img_responder.bot.service.response.MessageResponder;
import org.andnekon.img_responder.bot.service.response.MessageResponderFactory;
import org.andnekon.img_responder.bot.service.response.ResponseType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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

    private final String botToken;

    private final String allowedChats;

    private static final Logger logger = LoggerFactory.getLogger(ArchivistBot.class);

    @Autowired
    private ActionService actionService;

    public ArchivistBot(@Value("${tgAuthToken}") String botToken, @Value("${tgChatId}") String allowedChats) {
        this.botToken = botToken;
        this.allowedChats = allowedChats;
        this.telegramClient = new OkHttpTelegramClient(getBotToken());
    }

    /**
      * Structured logging, prepends chatId.
      * @param format Format string
      * @param chatId Chat identifier
      * @param args Remaining arguments for {@code String.format}
      */
    void log(String format, long chatId, Object... args) {
        // TODO: move to utils
        format = "[chat %d] " + format;
        if (args.length == 0) {
            logger.info(String.format(format, chatId));
        } else {
            logger.info(String.format(format, chatId, args));
        }
    }

    @Override
    public String getBotToken() {
        return botToken;
    }

    @Override
    public void consume(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            Message message = update.getMessage();
            long chatId = message.getChatId();
            String messageText = message.getText();
            if (!isChatAllowed(chatId)) {
                log("Chat not allowed", chatId);
                return;
            }
            if (message.isCommand()) {
                log("Recieved command", message.getChatId());
                processCommand(chatId, messageText);
                return;
            }
            processReply(chatId, messageText);
        }
    }

    /**
      * Replies to message based on the triggers specifed by created actions
      * or ignores message.
      * @param chatId Chat identifier
      * @param messageText Recieved text
      */
    private void processReply(long chatId, String messageText) {
        ResponseType responseType = getResponseType(messageText);
        log("Replying with responce type %s", chatId, responseType.toString());
        MessageResponder responder = MessageResponderFactory.getResponder(
                telegramClient, chatId, responseType);
        try {
            responder.execute();
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    /**
      * Controller for the defined commands
      * @param chatId Chat identifier
      * @param messageText Recieved text
      */
    private void processCommand(long chatId, String messageText) {
        String[] commandText = messageText.split(" ", 2);
        String command = commandText[0];
        String text = commandText.length > 1 ? commandText[1] : "";
        try {
            switch (command) {
                case "/create_action" ->
                    actionService.createAction(chatId, text);
                case "/list_actions" ->
                    actionService.listActions(chatId);
                case "/remove_action" ->
                    actionService.removeAction(chatId, text);
                default ->
                    replyError(chatId, "Not a valid command");
            }
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    /**
      * Use {@code telegramClient} to reply with error
      * @param chatId Chat identifier
      * @param error Error text
      */
    private void replyError(long chatId, String error) throws TelegramApiException {
        // TODO: move to ReplyService
        MessageResponder responder = MessageResponderFactory.getResponder(
                telegramClient, chatId, ResponseType.ERROR);
        responder.execute();
        log(error, chatId);
    }

    @Override
    public LongPollingUpdateConsumer getUpdatesConsumer() {
        return this;
    }

    /**
      * Checks whether {@code chatId} is in the list of allowed chats, defined by enviromental variable.
      * @param chatId Chat identifier
      * @return Whether env variabl has chatId in it
      */
    private boolean isChatAllowed(long chatId) {
        return allowedChats.contains(String.valueOf(chatId));
    }

    /**
      * Determines response type based on the text provided.
      * @param text Recieved text
      * @return response type
      */
    private ResponseType getResponseType(String text) {
        // TODO: update logic for actions, move to ReplyService
        if (text.contains("img")) {
            return ResponseType.IMAGE;
        } else if (text.length() < 3) {
            return ResponseType.TEXT;
        }
        return ResponseType.NONE;
    }
}
