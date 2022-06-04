package tp1.server;

import static tp1.api.service.util.Result.ErrorCode.*;
import static tp1.api.service.util.Result.error;
import static tp1.api.service.util.Result.ok;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Comparator;

import tp1.api.service.util.Files;
import tp1.api.service.util.Result;
import util.IO;
import util.Secret;
import util.Token;

public class JavaFiles implements Files {

	static final String DELIMITER = "_";
	private static final String ROOT = "/tmp/";
	private static final String DELETE_USER_TOPIC = "delete_user";

	public JavaFiles() {
		new File( ROOT ).mkdirs();
	}

	@Override
	public Result<byte[]> getFile(String fileId, String token) {
		if (!Token.validate(token, Secret.get(), fileId))
			return error(FORBIDDEN);

		fileId = fileId.replace( DELIMITER, "/");
		byte[] data = IO.read( new File( ROOT + fileId ));
		return data != null ? ok( data) : error( NOT_FOUND );
	}

	@Override
	public Result<Void> deleteFile(String fileId, String token) {
		if (!Token.validate(token, Secret.get(), fileId))
			return error(FORBIDDEN);

		fileId = fileId.replace( DELIMITER, "/");
		boolean res = IO.delete( new File( ROOT + fileId ));
		return res ? ok() : error( NOT_FOUND );
	}

	@Override
	public Result<Void> writeFile(String fileId, byte[] data, String token) {
		if (!Token.validate(token, Secret.get(), fileId))
			return error(FORBIDDEN);

		fileId = fileId.replace( DELIMITER, "/");
		File file = new File(ROOT + fileId);
		file.getParentFile().mkdirs();
		IO.write( file, data);
		return ok();
	}

	@Override
	public Result<Void> deleteUserFiles(String userId, String token) {
		if (!Token.validate(token, Secret.get(), userId))
			return error(FORBIDDEN);

		File file = new File(ROOT + userId);
		try {
			java.nio.file.Files.walk(file.toPath())
					.sorted(Comparator.reverseOrder())
					.map(Path::toFile)
					.forEach(File::delete);
		} catch (IOException e) {
			e.printStackTrace();
			return error(INTERNAL_ERROR);
		}
		return ok();
	}

	public static String fileId(String filename, String userId) {
		return userId + JavaFiles.DELIMITER + filename;
	}
}

