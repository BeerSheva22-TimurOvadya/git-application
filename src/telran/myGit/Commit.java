package telran.myGit;

import java.io.Serializable;
import java.time.Instant;
import java.util.*;

public class Commit implements Serializable {

	private static final long serialVersionUID = 1L;
	public Instant timestamp;
	public String nameOfCommit;
	public String messageOfCommit;
	public Commit prevCommit;
	public List<CommitFile> fileOfCommit;

}