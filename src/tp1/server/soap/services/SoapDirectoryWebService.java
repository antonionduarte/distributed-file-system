package tp1.server.soap.services;

import jakarta.jws.WebService;
import tp1.api.FileInfo;
import tp1.api.service.soap.DirectoryException;
import tp1.api.service.soap.SoapDirectory;
import tp1.api.service.util.Directory;
import tp1.api.service.util.Result;
import tp1.server.JavaDirectory;

import java.net.MalformedURLException;
import java.util.List;

@WebService(serviceName = SoapDirectory.NAME, targetNamespace = SoapDirectory.NAMESPACE, endpointInterface = SoapDirectory.INTERFACE)
public class SoapDirectoryWebService implements SoapDirectory {

	final Directory impl = new JavaDirectory();

	@Override
	public FileInfo writeFile(String filename, byte[] data, String userId, String password) throws DirectoryException {
		Result<FileInfo> result;
		try {
			result = impl.writeFile(filename, data, userId, password);
		} catch (MalformedURLException e) {
			throw new DirectoryException(Result.ErrorCode.BAD_REQUEST.toString());
		}

		if (result.isOK())
			return result.value();
		else
			throw new DirectoryException(result.error().toString());
	}

	@Override
	public void deleteFile(String filename, String userId, String password) throws DirectoryException {
		Result<Void> result;
		try {
			result = impl.deleteFile(filename, userId, password);
		} catch (MalformedURLException e) {
			throw new DirectoryException(Result.ErrorCode.BAD_REQUEST.toString());
		}
		if (!result.isOK())
			throw new DirectoryException(result.error().toString());
	}

	@Override
	public void shareFile(String filename, String userId, String userIdShare, String password) throws DirectoryException {
		Result<Void> result;
		try {
			result = impl.shareFile(filename, userId, userIdShare, password);
		} catch (MalformedURLException e) {
			throw new DirectoryException(Result.ErrorCode.BAD_REQUEST.toString());
		}
		if (!result.isOK())
			throw new DirectoryException(result.error().toString());
	}

	@Override
	public void unshareFile(String filename, String userId, String userIdShare, String password) throws DirectoryException {
		Result<Void> result;
		try {
			result = impl.unshareFile(filename, userId, userIdShare, password);
		} catch (MalformedURLException e) {
			throw new DirectoryException(Result.ErrorCode.BAD_REQUEST.toString());
		}
		if (!result.isOK())
			throw new DirectoryException(result.error().toString());
	}

	@Override
	public byte[] getFile(String filename, String userId, String accUserId, String password) throws DirectoryException {
		Result<byte[]> result;
		try {
			result = impl.getFile(filename, userId, accUserId, password);
		} catch (MalformedURLException e) {
			throw new DirectoryException(Result.ErrorCode.BAD_REQUEST.toString());
		}

		if (result.isOK()) {
			//TODO ignorar o result, (tem a uri para o caso de ser REST),
			// buscar cliente de ficheiros, pedir o ficheiro e devolver o resultado desse pedido
			// throw a DirectoryException se o resultado for erro, okay se for os bytes

			return null;
		} else
			throw new DirectoryException(result.error().toString());
	}

	@Override
	public List<FileInfo> lsFile(String userId, String password) throws DirectoryException {
		Result<List<FileInfo>> result = impl.lsFile(userId, password);

		if (result.isOK())
			return result.value();
		else
			throw new DirectoryException(result.error().toString());
	}
}
