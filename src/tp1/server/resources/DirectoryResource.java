package tp1.server.resources;

import jakarta.inject.Singleton;
import tp1.api.FileInfo;
import tp1.api.User;
import tp1.api.service.rest.RestDirectory;
import tp1.api.service.rest.RestUsers;
import tp1.clients.DiscoveryHelper;
import tp1.clients.GetUserClient;
import tp1.clients.RestUsersClient;
import tp1.server.Discovery;
import tp1.server.UsersServer;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

@Singleton
public class DirectoryResource implements RestDirectory {
	@Override
	public FileInfo writeFile(String filename, byte[] data, String userId, String password) {
		Discovery discovery = Discovery.getInstance();
		ArrayList<URI> userServerURI = discovery.knownUrisOf(UsersServer.SERVICE);

		return null;
	}

	@Override
	public void deleteFile(String filename, String userId, String password) {

	}

	@Override
	public void shareFile(String filename, String userId, String userIdShare, String password) {

	}

	@Override
	public void unshareFile(String filename, String userId, String userIdShare, String password) {

	}

	@Override
	public byte[] getFile(String filename, String userId, String accUserId, String password) {
		return new byte[0];
	}

	@Override
	public List<FileInfo> lsFile(String userId, String password) {
		return null;
	}
}
