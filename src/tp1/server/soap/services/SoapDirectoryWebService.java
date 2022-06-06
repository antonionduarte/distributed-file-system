package tp1.server.soap.services;

import jakarta.jws.WebService;
import tp1.api.FileInfo;
import tp1.api.service.soap.DirectoryException;
import tp1.api.service.soap.SoapDirectory;
import tp1.api.service.util.Directory;
import tp1.api.service.util.Files;
import tp1.api.service.util.Result;
import tp1.clients.ClientFactory;
import tp1.server.common.JavaDirectory;
import util.Secret;
import util.Token;

import java.util.List;

@WebService(serviceName = SoapDirectory.NAME, targetNamespace = SoapDirectory.NAMESPACE, endpointInterface = SoapDirectory.INTERFACE)
public class SoapDirectoryWebService implements SoapDirectory {

	private final Directory impl = new JavaDirectory();
	private final ClientFactory clientFactory = ClientFactory.getInstance();

	public SoapDirectoryWebService() {}

	@Override
	public FileInfo writeFile(String filename, byte[] data, String userId, String password) throws DirectoryException {
		Result<FileInfo> result;
		result = impl.writeFile(filename, data, userId, password);

		if (result.isOK()) {
			return result.value();
		} else {
			throw new DirectoryException(result.error().toString());
		}
	}

	@Override
	public void deleteFile(String filename, String userId, String password) throws DirectoryException {
		Result<Void> result;
		result = impl.deleteFile(filename, userId, password);
		if (!result.isOK()) {
			throw new DirectoryException(result.error().toString());
		}
	}

	@Override
	public void shareFile(String filename, String userId, String userIdShare, String password) throws DirectoryException {
		Result<Void> result;
		result = impl.shareFile(filename, userId, userIdShare, password);
		if (!result.isOK()) {
			throw new DirectoryException(result.error().toString());
		}
	}

	@Override
	public void unshareFile(String filename, String userId, String userIdShare, String password) throws DirectoryException {
		Result<Void> result;
		result = impl.unshareFile(filename, userId, userIdShare, password);
		if (!result.isOK()) {
			throw new DirectoryException(result.error().toString());
		}
	}

	@Override
	public byte[] getFile(String filename, String userId, String accUserId, String password) throws DirectoryException {
		Result<byte[]> resultDir;
		resultDir = impl.getFile(filename, userId, accUserId, password);

		if (resultDir.isOK()) {
			Files filesClient = clientFactory.getFilesClient(resultDir.redirectURI()).second();

			String fileId = userId + "_" + filename;
			Result<byte[]> resultFiles = filesClient.getFile(fileId, Token.generate(Secret.get(), fileId));
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
}
