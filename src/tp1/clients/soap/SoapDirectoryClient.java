package tp1.clients.soap;

import jakarta.xml.ws.BindingProvider;
import jakarta.xml.ws.Service;
import tp1.api.FileInfo;
import tp1.api.service.soap.DirectoryException;
import tp1.api.service.soap.SoapDirectory;
import tp1.api.service.util.Directory;
import tp1.api.service.util.Result;

import javax.xml.namespace.QName;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.List;

public class SoapDirectoryClient extends SoapClient implements Directory {
	private final SoapDirectory directory;

	public SoapDirectoryClient(URI serverURI) throws MalformedURLException {
		super(serverURI);
		QName qname = new QName(SoapDirectory.NAMESPACE, SoapDirectory.NAME);
		Service service = Service.create(URI.create(serverURI + "?wsdl").toURL(), qname);
		directory = service.getPort(tp1.api.service.soap.SoapDirectory.class);
		SoapClient.setTimeouts((BindingProvider) directory);
	}


	@Override
	public Result<FileInfo> writeFile(String filename, byte[] data, String userId, String password) {
		return super.reTry(() -> {
			try {
				return Result.ok(directory.writeFile(filename, data, userId, password));
			} catch (DirectoryException e) {
				return Result.error(Result.ErrorCode.valueOf(e.getMessage()));
			}
		});
	}

	@Override
	public Result<Void> deleteFile(String filename, String userId, String password) {
		return super.reTry(() -> {
			try {
				directory.deleteFile(filename, userId, password);
				return Result.ok();
			} catch (DirectoryException e) {
				return Result.error(Result.ErrorCode.valueOf(e.getMessage()));
			}
		});
	}

	@Override
	public Result<Void> shareFile(String filename, String userId, String userIdShare, String password) {
		return super.reTry(() -> {
			try {
				directory.shareFile(filename, userId, userIdShare, password);
				return Result.ok();
			} catch (DirectoryException e) {
				return Result.error(Result.ErrorCode.valueOf(e.getMessage()));
			}
		});
	}

	@Override
	public Result<Void> unshareFile(String filename, String userId, String userIdShare, String password) {
		return super.reTry(() -> {
			try {
				directory.unshareFile(filename, userId, userIdShare, password);
				return Result.ok();
			} catch (DirectoryException e) {
				return Result.error(Result.ErrorCode.valueOf(e.getMessage()));
			}
		});
	}

	@Override
	public Result<byte[]> getFile(String filename, String userId, String accUserId, String password) {
		return super.reTry(() -> {
			try {
				return Result.ok(directory.getFile(filename, userId, accUserId, password));
			} catch (DirectoryException e) {
				return Result.error(Result.ErrorCode.valueOf(e.getMessage()));
			}
		});
	}

	@Override
	public Result<List<FileInfo>> lsFile(String userId, String password) {
		return super.reTry(() -> {
			try {
				return Result.ok(directory.lsFile(userId, password));
			} catch (DirectoryException e) {
				return Result.error(Result.ErrorCode.valueOf(e.getMessage()));
			}
		});
	}
}
