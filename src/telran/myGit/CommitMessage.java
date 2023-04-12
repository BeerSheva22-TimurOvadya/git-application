package telran.myGit;

public class CommitMessage {
	public String commitName;
	public String commitMessage;


	public String toString() {
		return String.format("%s: %s", commitName, commitMessage);
	}

	public CommitMessage(String commitName, String commitMessage) {
		this.commitName = commitName;
		this.commitMessage = commitMessage;
	}
}