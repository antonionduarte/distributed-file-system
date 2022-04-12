package tp1.server.soap;

import jakarta.jws.WebService;
import tp1.api.service.soap.FilesException;
import tp1.api.service.soap.SoapFiles;
import tp1.api.service.util.Result;

import java.io.*;
import java.util.logging.Logger;

@WebService(serviceName = SoapFiles.NAME, targetNamespace = SoapFiles.NAMESPACE, endpointInterface = SoapFiles.INTERFACE)
public class SoapFilesWebService implements SoapFiles {

    private static final Logger Log = Logger.getLogger(SoapFilesWebService.class.getName());

    public SoapFilesWebService() {}

    @Override
    public byte[] getFile(String fileId, String token) throws FilesException {
        Log.info("SOAP getFile : " + fileId);

        File file = new File(fileId);
        try {
            FileInputStream fis = new FileInputStream(file);
            return fis.readAllBytes();
        } catch (FileNotFoundException e) {
            throw new FilesException(Result.ErrorCode.NOT_FOUND.toString());
        } catch (IOException e) {
            e.printStackTrace();
            throw new FilesException(Result.ErrorCode.BAD_REQUEST.toString());
        }
    }

    @Override
    public void deleteFile(String fileId, String token) throws FilesException {
        Log.info("SOAP deleteFile : " + fileId);

        File file = new File(fileId);
        if (file.delete()) {
            Log.info("File deleted.");
        } else
            throw new FilesException(Result.ErrorCode.NOT_FOUND.toString());
    }

    @Override
    public void writeFile(String fileId, byte[] data, String token) throws FilesException {
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
            throw new FilesException(Result.ErrorCode.BAD_REQUEST.toString());
        }
    }
}
