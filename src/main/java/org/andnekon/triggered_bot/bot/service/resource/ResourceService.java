package org.andnekon.triggered_bot.bot.service.resource;

import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Random;

import org.andnekon.triggered_bot.bot.dao.ChatRepository;
import org.andnekon.triggered_bot.bot.dao.ResourceRepository;
import org.andnekon.triggered_bot.bot.model.Chat;
import org.andnekon.triggered_bot.bot.model.Resource;
import org.andnekon.triggered_bot.utils.StringUtils;
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
      * Checks if the name is taken
      * Checks if user with chatId has enough space<br>
      * If document is zip, extracts it<br>
      * Downloads file using telegramClient<br>
      * If not enough space for chatId, cleans up<br>
      * @param chatId Identifier for the chat
      * @param doc Telegram document
      * @param dest Filename or directory name on the local filesystem
      */
    public void saveFile(long chatId, Document doc, String dest)
            throws NoSuchElementException, IOException, TelegramApiException,
                              ChatMemoryExceededException, IllegalStateException {
        if (resourceRepository.existsByChatIdAndName(chatId, dest)) {
            throw new IllegalStateException("Resource already exists");
        }
        Occupied occupied = getResourceUsage(chatId);
        boolean isDir = dest.endsWith("/");
        if (occupied.current + doc.getFileSize() >= occupied.limit) {
            throw new ChatMemoryExceededException();
        }
        if (StringUtils.isEmtpy(dest) || !dest.matches("\\w+/")) {
            throw new IllegalStateException("Invalid resource destination");
        }
        String userDest = getResourceFilename(chatId, dest);
        Files.createDirectories(Paths.get(userDest));
        String downloadFilename = !isDir ? userDest : String.format("%s/%s", userDest, doc.getFileName());
        if (isDir && !downloadFilename.endsWith(".zip")) {
            throw new IllegalStateException("Resource is a directory but no zip-file provided");
        }
        downloadFile(doc, downloadFilename);
        if (isDir) {
            unzip(downloadFilename, userDest, occupied.current, occupied.limit);
            deleteDirOrFile(new File(downloadFilename));
        }
        long uploadedSize = getResourceSize(new File(downloadFilename));
        if (occupied.current + uploadedSize >= occupied.limit) {
            deleteDirOrFile(new File(userDest));
            throw new ChatMemoryExceededException();
        }
        Resource updated = resourceRepository.findByChatIdAndName(chatId, dest)
            .orElse(new Resource(chatId, dest));
        updated.setSize(uploadedSize);
        resourceRepository.save(updated);
        logger.info("[chat {}] downlowded document {} of size {} ",
                chatId, doc.getFileName(), doc.getFileSize());
    }

    public File getRandomImage(long chatId, String resourceName) throws NoSuchElementException {
        Resource resource = resourceRepository.findByChatIdAndName(chatId, resourceName).orElseThrow();
        if (!resource.getName().endsWith("/")) {
            throw new UnsupportedOperationException("Unimplemented");
        }
        File dir = new File(String.format("./data/%d/%s", chatId, resourceName));
        String imageRe = ".*\\.(png|jpg|jpeg|gif|webp|svg)";
        File[] files = dir.listFiles((FileFilter) pathname ->
                pathname.getName().toLowerCase().matches(imageRe));
        if (files == null) {
            throw new NoSuchElementException("Emtpy directory");
        }
        Random rand = new Random();
        return files[rand.nextInt(files.length)];
    }

    public void removeResource(long chatId, String name) throws NoSuchElementException {
        // check resource exists
        resourceRepository.findByChatIdAndName(chatId, name).orElseThrow();
        deleteDirOrFile(new File(getResourceFilename(chatId, name)));
    }

    private String getResourceFilename(long chatId, String name) {
        return String.format("./data/%d/%s", chatId, name);
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

    private long getResourceSize(File resource) {
        long size = 0;
        if (!resource.isDirectory()){
            return resource.length();
        }
        for (File file : resource.listFiles()) {
            if (file.isFile()) {
                size += file.length();
            } else {
                size += getResourceSize(file);
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

