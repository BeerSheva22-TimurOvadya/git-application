package telran.myGit;

import java.io.Serializable;
import java.nio.file.Path;
import java.util.*;

public interface GitRepository extends Serializable {
	String GIT_FILE = ".mygit";
	String commit(String commitMessage);
	String createBranch(String branchName);
	String renameBranch(String branchName, String newName);
	String deleteBranch(String branchName);
	String switchTo(String name); 
	String getHead(); 
	String addIgnoredFileNameExp(String regex);
	List<FileState> info();	
	List<CommitMessage> log();
	List<String> branches(); 
	List<Path> commitContent(String commitName);	
	void save(); 
	
}