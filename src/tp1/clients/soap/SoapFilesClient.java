package tp1.clients.soap;

import jakarta.xml.ws.BindingProvider;
import jakarta.xml.ws.Service;
import tp1.api.service.soap.FilesException;
import tp1.api.service.soap.SoapFiles;
import tp1.api.service.util.Files;
import tp1.api.service.util.Result;

import javax.xml.namespace.QName;
import java.net.MalformedURLException;
import java.net.URI;

public class SoapFilesClient extends SoapClient implements Files {
	private final SoapFiles files;

	public SoapFilesClient(URI serverURI) throws MalformedURLException {
		super(serverURI);
		QName qname = new QName(SoapFiles.NAMESPACE, SoapFiles.NAME);
		Service service = Service.create(URI.create(serverURI + "?wsdl").toURL(), qname);
		files = service.getPort(tp1.api.service.soap.SoapFiles.class);
		SoapClient.setTimeouts((BindingProvider) files);
	}

	@Override
	public Result<Void> writeFile(String fileId, byte[] data, String token) {
		return super.reTry(() -> {
			try {
				files.writeFile(fileId, data, token);
				return Result.ok();
			} catch (FilesException e) {
				return Result.error(Result.ErrorCode.valueOf(e.getMessage()));
			}
		});
	}

	@Override
	public Result<Void> deleteFile(String fileId, String token) {
		return super.reTry(() -> {
			try {
				files.deleteFile(fileId, token);
				return Result.ok();
			} catch (FilesException e) {
				return Result.error(Result.ErrorCode.valueOf(e.getMessage()));
			}
		});
	}

	@Override
	public Result<byte[]> getFile(Long version, String fileId, String token) {
		return super.reTry(() -> {
			try {
				return Result.ok(files.getFile(fileId, token));
			} catch (FilesException e) {
				return Result.error(Result.ErrorCode.valueOf(e.getMessage()));
			}
		});
	}
}
