package tp1.server.soap.services;

import jakarta.jws.WebService;
import tp1.api.FileInfo;
import tp1.api.service.soap.DirectoryException;
import tp1.api.service.soap.SoapDirectory;
import tp1.api.service.util.Directory;
import tp1.api.service.util.Files;
import tp1.api.service.util.Result;
import tp1.clients.ClientFactory;
import tp1.server.JavaDirectory;

import java.net.MalformedURLException;
import java.util.List;

@WebService(serviceName = SoapDirectory.NAME, targetNamespace = SoapDirectory.NAMESPACE, endpointInterface = SoapDirectory.INTERFACE)
public class SoapDirectoryWebService implements SoapDirectory {

	private final Directory impl;
	private final ClientFactory clientFactory;
	private String token;

	public SoapDirectoryWebService(String token) {
		impl = new JavaDirectory(token);
		clientFactory = ClientFactory.getInstance();
		this.token = token;
	}

	@Override
	public FileInfo writeFile(String filename, byte[] data, String userId, String password) throws DirectoryException {
		Result<FileInfo> result;
		try {
			result = impl.writeFile(filename, data, userId, password);
		} catch (MalformedURLException e) {
			throw new DirectoryException(Result.ErrorCode.INTERNAL_ERROR.toString());
		}

		if (result.isOK()) {
			return result.value();
		} else {
			throw new DirectoryException(result.error().toString());
		}
	}

	@Override
	public void deleteFile(String filename, String userId, String password) throws DirectoryException {
		Result<Void> result;
		try {
			result = impl.deleteFile(filename, userId, password);
		} catch (MalformedURLException e) {
			throw new DirectoryException(Result.ErrorCode.INTERNAL_ERROR.toString());
		}
		if (!result.isOK()) {
			throw new DirectoryException(result.error().toString());
		}
	}

	@Override
	public void shareFile(String filename, String userId, String userIdShare, String password) throws DirectoryException {
		Result<Void> result;
		try {
			result = impl.shareFile(filename, userId, userIdShare, password);
		} catch (MalformedURLException e) {
			throw new DirectoryException(Result.ErrorCode.INTERNAL_ERROR.toString());
		}
		if (!result.isOK()) {
			throw new DirectoryException(result.error().toString());
		}
	}

	@Override
	public void unshareFile(String filename, String userId, String userIdShare, String password) throws DirectoryException {
		Result<Void> result;
		try {
			result = impl.unshareFile(filename, userId, userIdShare, password);
		} catch (MalformedURLException e) {
			throw new DirectoryException(Result.ErrorCode.INTERNAL_ERROR.toString());
		}
		if (!result.isOK()) {
			throw new DirectoryException(result.error().toString());
		}
	}

	@Override
	public byte[] getFile(String filename, String userId, String accUserId, String password) throws DirectoryException {
		Result<byte[]> resultDir;
		try {
			resultDir = impl.getFile(filename, userId, accUserId, password);

			if (resultDir.isOK()) {
				Files filesClient = clientFactory.getFilesClient(resultDir.redirectURI().toString()).second();

				Result<byte[]> resultFiles = filesClient.getFile(userId + "_" + filename, token);
				if (resultFiles == null) {
					throw new DirectoryException(Result.ErrorCode.INTERNAL_ERROR.toString());
				}

				if (resultFiles.isOK()) {
					return resultFiles.value();
				} else {
					throw new DirectoryException(resultFiles.error().toString());
				}
			} else {
				throw new DirectoryException(resultDir.error().toString());
			}
		} catch (MalformedURLException e) {
			throw new DirectoryException(Result.ErrorCode.INTERNAL_ERROR.toString());
		}
	}

	@Override
	public List<FileInfo> lsFile(String userId, String password) throws DirectoryException {
		Result<List<FileInfo>> result = impl.lsFile(userId, password);

		if (result.isOK()) {
			return result.value();
		} else {
			throw new DirectoryException(result.error().toString());
		}
	}

	@Override
	public void removeUser(String userId, String token) throws DirectoryException {
		Result<Void> result = impl.removeUserFiles(userId, token);

		if (!result.isOK()) {
			throw new DirectoryException(result.error().toString());
		}
	}
}
