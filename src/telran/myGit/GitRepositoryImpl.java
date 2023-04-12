package telran.myGit;

import java.nio.file.*;
import java.time.Instant;
import java.util.*;
import java.io.*;

public class GitRepositoryImpl implements GitRepository {
	private static final long serialVersionUID = 1L;

	private HashMap<String, Commit> commits;
	private HashMap<String, CommitFile> commitFiles;
	private HashMap<String, Branch> branches;
	private Instant lastCommitTimestamp;
	private String gitPath;
	private String head;

	private GitRepositoryImpl(Path git) {
		this.gitPath = git.toString();
		commits = new HashMap<>();
		commitFiles = new HashMap<>();
		branches = new HashMap<>();

	}

	public static GitRepository init() throws Exception {
		Path path = Path.of(".").toAbsolutePath();
		Path git = path.resolve(GIT_FILE);
		GitRepository res = null;
		if (Files.exists(git)) {
			res = restoreFile(git);
		} else {
			res = new GitRepositoryImpl(git.normalize());
		}
		return res;
	}

	private static GitRepository restoreFile(Path gitPath) throws Exception {
		try (ObjectInputStream input = new ObjectInputStream(Files.newInputStream(gitPath))) {
			return (GitRepository) input.readObject();
		}

	}

	@Override
	public String commit(String commitMessage) {
		return head == null ? commitHeadNull(commitMessage) : commitHeadNoNull(commitMessage);
	}

	private String commitHeadNull(String commitMessage) {
		Commit commit = createNewCommit(commitMessage, null);
		createInternalBranch("master", commit);
		return "Commit done";
	}

	private String commitHeadNoNull(String commitMessage) {
		String res = null;
		Branch branch = branches.get(head);
		if (branch == null) {
			res = "Commit allowed only for branch";
		} else {
			Commit commit = createNewCommit(commitMessage, commits.get(branch.nameOfcommit));
			branch.nameOfcommit = commit.nameOfCommit;
			res = "Commit done";
		}
		return res;
	}

	private Commit createNewCommit(String message, Commit prev) {
		Commit res = new Commit();
		res.messageOfCommit = message;
		res.prevCommit = prev;
		res.timestamp = Instant.now();
		res.fileOfCommit = getCommitContent(res.nameOfCommit);
		commits.put(res.nameOfCommit, res);
		lastCommitTimestamp = res.timestamp;
		return res;
	}

	private List<CommitFile> getCommitContent(String commitName) {
		List<FileState> files = info();
		return files.stream().filter(f -> f.status == FileStatus.UNTRACKED || f.status == FileStatus.MODIFIED)
				.map(f -> toCommitFile(f, commitName)).toList();
	}

	private CommitFile toCommitFile(FileState f, String commitName) {
		Instant timeModified;
		try {
			timeModified = Files.getLastModifiedTime(f.path).toInstant();
		} catch (IOException e) {
			throw new RuntimeException(e.toString());
		}
		List<String> content = getFileContent(f.path);
		CommitFile res = new CommitFile(f.path.toString(), timeModified, content, commitName);
		commitFiles.put(f.path.toString(), res);
		return res;
	}

	private List<String> getFileContent(Path path) {
		try {
			return Files.lines(path).toList();
		} catch (IOException e) {
			throw new RuntimeException(e.toString());
		}
	}

	private void createInternalBranch(String branchName, Commit commit) {
		if (branches.containsKey(branchName)) {
			throw new IllegalStateException(String.format("Branch %s already exists", branchName));
		}
		Branch branch = new Branch();
		branch.nameOfBranch = branchName;
		branch.nameOfcommit = commit.nameOfCommit;
		branches.put(branchName, branch);
		head = branchName;
	}

	@Override
	public List<FileState> info() {
		Path directoryPath = Path.of(".");
		try {
			return Files.list(directoryPath).map(p -> p.normalize()).filter(p -> !filter(p)).map(p -> {
				try {
					return new FileState(p, getStatus(p));
				} catch (IOException e) {
					throw new RuntimeException(e.toString());
				}
			}).toList();
		} catch (IOException e) {
			return Collections.emptyList();
		}
	}

	private boolean filter(Path path) {
		boolean isRegularFile = Files.isRegularFile(path);
		boolean matchesIgnorePath = path.toString().matches("(\\..*)");
		if (!isRegularFile || matchesIgnorePath) {
			return true;
		} else {
			return false;
		}
	}

	private FileStatus getStatus(Path path) throws IOException {
		CommitFile commitFile = commitFiles.get(path.toString());
		FileStatus fileStatus;
		if (commitFile == null) {
			fileStatus = FileStatus.UNTRACKED;
		} else {
			fileStatus = getStatusFromCommitFile(commitFile, path);
		}
		return fileStatus;
	}

	private FileStatus getStatusFromCommitFile(CommitFile commitFile, Path path) throws IOException {
		FileStatus fileStatus;
		if (Files.getLastModifiedTime(path).toInstant().compareTo(lastCommitTimestamp) > 0) {
			fileStatus = FileStatus.MODIFIED;
		} else {
			fileStatus = FileStatus.COMMITTED;
		}
		return fileStatus;
	}

	@Override
	public String createBranch(String branchName) {
		String res = null;
		if (commits.isEmpty()) {
			res = "A branch can be created to an existing commit";
		} else if (branches.containsKey(branchName)) {
			res = "Branch already exists";
		} else {
			Commit commit = getCommit();
			createInternalBranch(branchName, commit);
			res = "Branch created";
		}
		return res;
	}

	private Commit getCommit() {
		Branch branch = branches.get(head);
		String commitName = commitName(branch);	
		Commit res = commits.get(commitName);
		if (res == null) {
			throw new IllegalStateException("no commit with the name " + commitName);
		}
		return res;
	}

	@Override
	public String renameBranch(String branchName, String newName) {
		String res = branchName  + " Branch No Exists";
		Branch branch = branches.get(branchName);
		if (branch != null) {
			if (branches.containsKey(newName)) {
				res = newName  + " Branch already exists";
			} else {
				branch.nameOfBranch = newName;
				branches.remove(branchName);
				branches.put(newName, branch);
				if (head.equals(branchName)) {
					head = newName;
				}
				res = "The branch has been renamed";
			}
		}
		return res;
	}

	@Override
	public String deleteBranch(String branchName) {
		String res = "Branch No Exists";
		if (branches.containsKey(branchName)) {
			if (head.equals(branchName)) {
				res = "Active branch cannot be deleted";
			} else if (branches.size() == 1) {
				res = "There must be at least one branch";
			} else {
				branches.remove(branchName);
				res = "Branch deleted";
			}
		}
		return res;
	}

	@Override
	public List<CommitMessage> log() {
		List<CommitMessage> res = new ArrayList<>();
		if (head != null) {
			Branch branch = branches.get(head);
			String commitName = commitName(branch);			
			Commit commit = commits.get(commitName);
			if (commit == null) {
				throw new IllegalStateException("no commit with name " + commitName);
			}
			while (commit != null) {
				res.add(new CommitMessage(commit.nameOfCommit, commit.messageOfCommit));
				commit = commit.prevCommit;
			}
		}
		return res;
	}

	private String commitName(Branch branch) {
		String commitName;
		if (branch != null) {
			commitName = branch.nameOfcommit;
		} else {
			commitName = head;
		}
		return commitName;		
	}

	@Override
	public List<String> branches() {
		return branches.values().stream().map(b -> {
			String res = b.nameOfBranch;
			if (head.equals(res)) {
				res += "*";
			}
			return res;
		}).toList();
	}

	@Override
	public List<Path> commitContent(String commitName) {
		Commit commit = commits.get(commitName);
		if (commit == null) {
			throw new IllegalArgumentException(commitName + " doesn't exist");
		}
		return commit.fileOfCommit.stream().map(cf -> Path.of(cf.path)).toList();
	}

	@Override
	public String switchTo(String name) {
		List<FileState> fileStates = info();
		String res = "Switched to " + name;
		Commit commitTo = commitSwitched(name);
		Commit commitHead = getCommit();
		if (commitTo != null) {
			if (head.equals(name) || commitTo.nameOfCommit.equals(commitHead.nameOfCommit)) {
				res = name + "The same commit as the current one";
			} else if (fileStates.stream().anyMatch(f -> f.status != FileStatus.COMMITTED)) {
				res = "Commit before switching";
			} else {
				info().stream().forEach(f -> {
					try {
						Files.delete(f.path);
					} catch (IOException e) {
						throw new IllegalStateException("error in deleting files " + e.getMessage());
					}
				});
				switchProcess(commitTo);
				head = name;
				lastCommitTimestamp = Instant.now();
			}

		}

		return res;
	}

	private void switchProcess(Commit commitTo) {
		Set<String> restoredFiles = new HashSet<>();
		try {
			while (commitTo != null) {
				commitTo.fileOfCommit.stream().forEach(c -> {
					if (!restoredFiles.contains(c.path)) {
						try (PrintWriter writer = new PrintWriter(c.path)) {
							c.content.stream().forEach(writer::println);
						} catch (Exception e) {
							throw new IllegalStateException(e.toString());
						}
						restoredFiles.add(c.path);
					}
				});
				commitTo = commitTo.prevCommit;
			}
		} catch (Exception e) {
			throw new IllegalStateException("error in switchForward functionality ");
		}

	}

	private Commit commitSwitched(String name) {
		Commit res = null;
		String commitName = name;
		Branch branch = branches.get(name);
		if (branch != null) {
			commitName = branch.nameOfcommit;
		}
		res = commits.get(commitName);
		if (res == null) {
			throw new IllegalArgumentException("Wrong commit name" + commitName);
		}
		return res;
	}

	@Override
	public String getHead() {
		String res = "No head yet";
		if (head != null) {
			res = branches.containsKey(head) ? "branch name" : "commit name";
			res += head;
		}
		return res;
	}

	@Override
	public void save() {
		try (ObjectOutputStream output = new ObjectOutputStream(Files.newOutputStream(Path.of(gitPath)))) {
			output.writeObject(this);
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		}

	}

	@Override
	public String addIgnoredFileNameExp(String regex) {
		String ignore = "(\\..*)";
		try {
			"git".matches(regex);
		} catch (Exception e) {
			throw new IllegalArgumentException(regex + "Wrong regex");
		}
		ignore += String.format("|(%s)", regex);
		return String.format("Regex for files ignored is %s", ignore);
	}

}