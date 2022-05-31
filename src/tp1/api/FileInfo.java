package tp1.api;

import java.net.URI;
import java.util.Objects;
import java.util.Set;

/**
 * Represents a File in the system
 */
public class FileInfo {
	/**
	 * userId of the owner
	 */
	private String owner;
	private String filename;
	/**
	 * URLs for direct access to a file
	 */
	private Set<URI> fileURIs;
	/**
	 * List of user with whom the file has been shared
	 */
	private Set<String> sharedWith;

	public FileInfo(String owner, String filename, Set<URI> fileURIs, Set<String> sharedWith) {
		this.owner = owner;
		this.filename = filename;
		this.fileURIs = fileURIs;
		this.sharedWith = sharedWith;
	}

	public String getOwner() {
		return owner;
	}

	public void setOwner(String owner) {
		this.owner = owner;
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public Set<URI> getFileURIs() {
		return fileURIs;
	}

	public void setFileURL(Set<URI> fileURIs) {
		this.fileURIs = fileURIs;
	}

	public Set<String> getSharedWith() {
		return sharedWith;
	}

	public void setSharedWith(Set<String> sharedWith) {
		this.sharedWith = sharedWith;
	}

	@Override
	public int hashCode() {
		return Objects.hash(fileURIs, filename);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		FileInfo other = (FileInfo) obj;
		return Objects.equals(fileURIs, other.fileURIs) && Objects.equals(filename, other.filename);
	}

	@Override
	public String toString() {
		return "FileInfo [owner=" + owner + ", filename=" + filename + ", fileURIs=" + fileURIs + ", sharedWith="
				+ sharedWith + "]";
	}


}
