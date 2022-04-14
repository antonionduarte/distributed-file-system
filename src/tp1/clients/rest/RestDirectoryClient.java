package tp1.clients.rest;

import jakarta.ws.rs.client.WebTarget;
import tp1.api.FileInfo;
import tp1.api.service.rest.RestUsers;
import tp1.api.service.util.Directory;
import tp1.api.service.util.Result;

import java.net.URI;
import java.util.List;

public class RestDirectoryClient extends RestClient implements Directory {

    final WebTarget target;

    public RestDirectoryClient(URI serverURI) {
        super(serverURI);
        target = client.target(serverURI).path(RestUsers.PATH);
    }

    @Override
    public Result<FileInfo> writeFile(String filename, byte[] data, String userId, String password) {
        return super.reTry(() -> clt_writeFile(filename, data, userId, password));
    }

    @Override
    public Result<Void> deleteFile(String filename, String userId, String password) {
        return super.reTry(() -> clt_deleteFile(filename, userId, password));
    }

    @Override
    public Result<Void> shareFile(String filename, String userId, String userIdShare, String password) {
        return super.reTry(() -> clt_shareFile(filename, userId, userIdShare, password));
    }

    @Override
    public Result<Void> unshareFile(String filename, String userId, String userIdShare, String password) {
        return super.reTry(() -> clt_unshareFile(filename, userId, userIdShare, password));
    }

    @Override
    public Result<byte[]> getFile(String filename, String userId, String accUserId, String password) {
        return super.reTry(() -> clt_getFile(filename, userId, accUserId, password));
    }

    @Override
    public Result<List<FileInfo>> lsFile(String userId, String password) {
        return super.reTry(() -> clt_lsFile(userId, password));

    }

    private Result<FileInfo> clt_writeFile(String filename, byte[] data, String userId, String password) {
        return null;
    }

    private Result<Void> clt_deleteFile(String filename, String userId, String password) {
        return null;
    }

    private Result<Void> clt_shareFile(String filename, String userId, String userIdShare, String password) {
        return null;
    }

    private Result<Void> clt_unshareFile(String filename, String userId, String userIdShare, String password) {
        return null;
    }

    private Result<byte[]> clt_getFile(String filename, String userId, String accUserId, String password) {
        return null;
    }

    private Result<List<FileInfo>> clt_lsFile(String userId, String password) {
        return null;
    }

}
