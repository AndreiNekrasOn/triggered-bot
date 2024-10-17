package org.andnekon.img_responder.bot.service.chat;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.Optional;

import org.andnekon.img_responder.bot.dao.ChatRepository;
import org.andnekon.img_responder.bot.model.Chat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ChatService {

    @Autowired
    ChatRepository chatRepository;

    /**
      * Checks whether {@code chatId} is in the list of allowed chats, defined by enviromental variable.
      * @param chatId Chat identifier
      * @return Whether env variabl has chatId in it
      */
    public boolean isChatAllowed(long chatId) {
        Optional<Chat> chatResult = chatRepository.findById(chatId);
        if (chatResult.isEmpty()) {
            return false;
        }
        Chat chat = chatResult.get();
        Instant now = Instant.now();
        ZonedDateTime zonedDateTime = now.atZone(ZoneId.systemDefault());
        LocalDate today = zonedDateTime.toLocalDate();
        return chat.getAuthFrom().compareTo(today) >= 0 &&
            chat.getAuthTo().compareTo(today) < 0;
    }

    /**
      * Adds chat to the database with the current date and expiration date a month from now.
      * @param chatId Chat identifier
      */
    public void registerChat(long chatId) {
        Instant now = Instant.now();
        ZonedDateTime zonedDateTime = now.atZone(ZoneId.systemDefault());
        LocalDate today = zonedDateTime.toLocalDate();
        LocalDate monthAfter = today.plusMonths(1);
        Chat chat = new Chat(chatId, today, monthAfter);
        chatRepository.save(chat);
    }
}

