package telran.myGit;

import java.nio.file.Path;
import java.util.Objects;

public class FileState {
	public Path path;
	public FileStatus status;

	public FileState(Path path, FileStatus status) {
		this.path = path;
		this.status = status;
	}


}

