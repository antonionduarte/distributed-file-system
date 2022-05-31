package tp1.server;

import tp1.api.service.util.Files;
import tp1.api.service.util.Result;
import util.Secret;
import util.Token;

import java.io.*;
import java.util.logging.Logger;

public class JavaFiles implements Files {

	private static final Logger Log = Logger.getLogger(JavaFiles.class.getName());

	@Override
	public Result<Void> writeFile(String fileId, byte[] data, String token) {
		Log.info("writeFile : " + fileId);

		if (!Token.generate(Secret.get(), fileId).equals(token)) {
			return Result.error(Result.ErrorCode.FORBIDDEN);
		}

		File file = new File(fileId);
		try {
			synchronized (this) {
				try (FileOutputStream outputStream = new FileOutputStream(file)) {
					outputStream.write(data);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
			return Result.error(Result.ErrorCode.INTERNAL_ERROR);
		}
		return Result.ok();
	}

	@Override
	public Result<Void> deleteFile(String fileId, String token) {
		Log.info("deleteFile : " + fileId);

		File file = new File(fileId);
		if (file.exists()) {
			if (!Token.generate(Secret.get(), fileId).equals(token)) {
				return Result.error(Result.ErrorCode.FORBIDDEN);
			}

			file.delete();
			Log.info("File deleted.");
		} else {
			return Result.error(Result.ErrorCode.NOT_FOUND);
		}

		return Result.ok();
	}

	@Override
	public Result<byte[]> getFile(String fileId, String token) {
		Log.info("getFile : " + fileId);

		File file = new File(fileId);
		try {
			byte[] data;
			synchronized (this) {
				try (FileInputStream fis = new FileInputStream(file)) {
					data = fis.readAllBytes();
				}
			}
			if (!Token.generate(Secret.get(), fileId).equals(token)) {
				return Result.error(Result.ErrorCode.FORBIDDEN);
			}

			return Result.ok(data);
		} catch (FileNotFoundException e) {
			return Result.error(Result.ErrorCode.NOT_FOUND);
		} catch (IOException e) {
			e.printStackTrace();
			return Result.error(Result.ErrorCode.INTERNAL_ERROR);
		}
	}
}
