package tp1.server;

import tp1.api.service.util.Files;
import tp1.api.service.util.Result;
import tp1.server.rest.resources.UsersResource;

import java.io.*;
import java.util.logging.Logger;

public class JavaFiles implements Files {

    private static final Logger Log = Logger.getLogger(UsersResource.class.getName());


    @Override
    public Result<Void> writeFile(String fileId, byte[] data, String token) {
        Log.info("writeFile : " + fileId);

        File file = new File(fileId);
        try {
            if (file.createNewFile())
                Log.info("File created.");
            else
                Log.info("Writing on existing .");
            FileOutputStream outputStream = new FileOutputStream(file);
            outputStream.write(data);
        } catch (IOException e) {
            e.printStackTrace();
            return Result.error(Result.ErrorCode.BAD_REQUEST);
         }
        return Result.ok();
    }

    @Override
    public Result<Void> deleteFile(String fileId, String token) {
        Log.info("deleteFile : " + fileId);

        File file = new File(fileId);
        if (file.delete()) {
            Log.info("File deleted.");
        } else
           return Result.error(Result.ErrorCode.NOT_FOUND);
        return Result.ok();
    }

    @Override
    public Result<byte[]> getFile(String fileId, String token) {
        Log.info("getFile : " + fileId);

        File file = new File(fileId);
        try {
            FileInputStream fis = new FileInputStream(file);
            return Result.ok(fis.readAllBytes());
        } catch (FileNotFoundException e) {
            return Result.error(Result.ErrorCode.NOT_FOUND);
        } catch (IOException e) {
            e.printStackTrace();
            return Result.error(Result.ErrorCode.BAD_REQUEST);
        }
    }
}
