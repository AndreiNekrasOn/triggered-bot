package org.andnekon.img_responder.bot.service.resource;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.List;
import java.util.NoSuchElementException;

import org.andnekon.img_responder.bot.dao.ChatRepository;
import org.andnekon.img_responder.bot.dao.ResourceRepository;
import org.andnekon.img_responder.bot.model.Chat;
import org.andnekon.img_responder.bot.model.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.objects.Document;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import net.lingala.zip4j.ZipFile;

@Service
public class ResourceService {

    class Occupied {
        long current;
        long limit;
        public Occupied(long current, long limit) {
            this.current = current;
            this.limit = limit;
        }
    }

    @Autowired
    ResourceRepository resourceRepository;

    @Autowired
    ChatRepository chatRepository;

    @Autowired
    TelegramClient telegramClient;

    Logger logger = LoggerFactory.getLogger(ResourceRepository.class);

    /**
      * TODO: Creates chatId folder if absent<br>
      * Checks if user with chatId has enough space<br>
      * If document is zip, extracts it<br>
      * Downloads file using telegramClient<br>
      * If not enough space for chatId, cleans up<br>
      * @param chatId Identifier for the chat
      * @param doc Telegram document
      * @param dest Filename or directory name on the local filesystem
     * @throws TelegramApiException
     * @throws IOException
     * @throws ChatMemoryExceededException
      */
    public void saveFile(long chatId, Document doc, String dest)
            throws NoSuchElementException, IOException, TelegramApiException,
                              ChatMemoryExceededException, IllegalStateException {
        Occupied occupied = getResourceUsage(chatId);
        boolean isDir = dest.endsWith("/");
        if (occupied.current + doc.getFileSize() >= occupied.limit) {
            throw new ChatMemoryExceededException();
        }
        String localFilename = isDir ?
            String.format("./data/%d/%s/%s", chatId, dest, doc.getFileName()) :
            String.format("./data/%d/%s", chatId, dest);
        if (isDir && !localFilename.endsWith(".zip")) {
            throw new IllegalStateException("Resource is a directory but no zip-file provided");
        }
        downloadFile(doc, localFilename);
        if (isDir) {
            unzip(localFilename, dest, occupied.current, occupied.limit);
            deleteDirOrFile(new File(localFilename));
        }
        long uploadedSize = getDirSize(new File(localFilename));
        if (occupied.current + uploadedSize >= occupied.limit) {
            deleteDirOrFile(new File(dest));
            throw new ChatMemoryExceededException();
        }
        Resource updated = resourceRepository.findByChatIdAndName(chatId, dest)
            .orElse(new Resource(chatId, dest));
        updated.setSize(uploadedSize);
        resourceRepository.save(updated);
        logger.info("[chat {}] downlowded document {} of size {} ",
                chatId, doc.getFileName(), doc.getFileSize());
    }

    private Occupied getResourceUsage(long chatId) throws NoSuchElementException {
        Chat chat = chatRepository.findById(chatId).orElseThrow();
        List<Resource> resources = resourceRepository.findAllByChatId(chatId);
        long totalSize = resources.stream().mapToLong(Resource::getSize).sum();
        long limitSize = chat.getMemLimit();
        return new Occupied(totalSize, limitSize);
    }

    private void unzip(String source, String dst, long totalSize, long limitSize) {
        try (ZipFile zipFile = new ZipFile(source)) {
            zipFile.extractAll(dst);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void downloadFile(Document document, String localFilename) throws IOException, TelegramApiException {
        // TODO: create TgApiService
        GetFile getFile = new GetFile(document.getFileId());
        org.telegram.telegrambots.meta.api.objects.File file = telegramClient.execute(getFile);
        logger.info("downlowding file...: {}", file.getFilePath());
        InputStream downlowded = telegramClient.downloadFileAsStream(file);
        File localFile = new File(localFilename);
        OutputStream out = new FileOutputStream(localFile);
        out.write(downlowded.readAllBytes());
        downlowded.close();
        out.close();
    }

    private long getDirSize(File dir) {
        long size = 0;
        for (File file : dir.listFiles()) {
            if (file.isFile()) {
                size += file.length();
            }
            else {
                size += getDirSize(file);
            }
        }
        return size;
    }

    // https://stackoverflow.com/a/29175213/9817178
    private void deleteDirOrFile(File file) {
        File[] contents = file.listFiles();
        if (contents != null) {
            for (File f : contents) {
                if (!Files.isSymbolicLink(f.toPath())) {
                    deleteDirOrFile(f);
                }
            }
        }
        file.delete();
    }

}

