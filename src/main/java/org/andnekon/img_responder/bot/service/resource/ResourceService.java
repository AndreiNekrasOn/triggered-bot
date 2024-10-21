package org.andnekon.img_responder.bot.service.resource;

import java.util.List;
import java.util.Optional;

import org.andnekon.img_responder.bot.dao.ChatRepository;
import org.andnekon.img_responder.bot.dao.ResourceRepository;
import org.andnekon.img_responder.bot.model.Chat;
import org.andnekon.img_responder.bot.model.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Document;

@Service
public class ResourceService {

    @Autowired
    ResourceRepository resourceRepository;

    @Autowired
    ChatRepository chatRepository;

    Logger logger = LoggerFactory.getLogger(ResourceRepository.class);

    public void saveFile(long chatId, Document document) {
        // check if chatId has enough space
        // create chatId folder if absent
        // if document is zip, iterate through directory non-recursively
        // if the file is image/text -> check size, if space avaliable, then save it

        Optional<Chat> chatOpt = chatRepository.findById(chatId);
        if (chatOpt.isEmpty()) {
            return; // maybe throw here
        }
        Chat chat = chatOpt.get();

        List<Resource> resources = resourceRepository.findByChatId(chatId);
        long totalSize = resources.stream().mapToLong(Resource::getSize).sum();
        long limitSize = chat.getMemLimit();

        logger.info("[chat {}] saving document {} of size {} ",
                chatId,
                document.getFileName(),
                document.getFileSize()
                );
    }
}

