package org.andnekon.img_responder.bot.service.resource;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
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
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.objects.Document;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;

@Service
public class ResourceService {

    @Autowired
    ResourceRepository resourceRepository;

    @Autowired
    ChatRepository chatRepository;

    @Autowired
    TelegramClient telegramClient;

    Logger logger = LoggerFactory.getLogger(ResourceRepository.class);

    /**
      * TODO: needs refactoring, e.g. split processing file and directory<br>
      * TODO: Creates chatId folder if absent<br>
      * Checks if user with chatId has enough space<br>
      * If document is zip, extracts it<br>
      * Downloads file using telegramClient<br>
      * If not enough space for chatId, cleans up<br>
      * @param chatId Identifier for the chat
      * @param document Telegram document
      * @param destination Filename or directory name on the local filesystem
      */
    public void saveFile(long chatId, Document document, String destination) {
        Optional<Chat> chatOpt = chatRepository.findById(chatId);
        if (chatOpt.isEmpty()) {
            return; // maybe throw here
        }
        Chat chat = chatOpt.get();

        List<Resource> resources = resourceRepository.findAllByChatId(chatId);
        long totalSize = resources.stream().mapToLong(Resource::getSize).sum();
        long limitSize = chat.getMemLimit();
        boolean isDir = destination.endsWith("/");

        if (totalSize + document.getFileSize() >= limitSize) {
            logger.info("[chat {}] exceeded download limit");
            return;
        }

        // downloads file/zip archive
        String localFilename = isDir ? String.format("./data/%s/%s/%s",String.valueOf(chatId), destination, document.getFileName()) :
            String.format("./data/%s/%s", String.valueOf(chatId), destination);
        if (isDir && !localFilename.endsWith(".zip")) {
            logger.info("[chat {}] recource is directory but no zip provided");
            return;
        }

        try {
            downloadFile(document, localFilename);
        } catch (IOException | TelegramApiException e) {
            e.printStackTrace();
        }

        if (isDir) {
            unzip(localFilename, destination, totalSize, limitSize);
            deleteFile(localFilename);
        }

        long uploadedSize = getDirSize(new File(localFilename));
        if (totalSize + uploadedSize >= limitSize) {
            if (isDir) {
                deleteDir(new File(localFilename));
            } else {
                deleteFile(localFilename);
            }
            logger.info("[chat {}] exceeded memory limit after download, files deleted");
            return;
        }

        Resource updated = resourceRepository.findByChatIdAndName(chatId, destination)
            .orElse(new Resource(chatId, destination));
        updated.setSize(uploadedSize);
        resourceRepository.save(updated);

        logger.info("[chat {}] downlowded document {} of size {} ",
                chatId,
                document.getFileName(),
                document.getFileSize()
                );
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

    private void deleteFile(String name) {
        try {
            Files.deleteIfExists(new File(name).toPath());
        } catch (IOException e) {
            e.printStackTrace();
        }
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
    private void deleteDir(File file) {
        File[] contents = file.listFiles();
        if (contents != null) {
            for (File f : contents) {
                if (!Files.isSymbolicLink(f.toPath())) {
                    deleteDir(f);
                }
            }
        }
        file.delete();
    }

}

