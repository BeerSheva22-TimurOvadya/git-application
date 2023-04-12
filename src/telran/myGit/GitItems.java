package telran.myGit;



import telran.view.*;

public class GitItems {
	private static GitRepository gitRepository;

	public static Item[] getGitRepositoryItems(GitRepository gitRepository) {
		GitItems.gitRepository = gitRepository;
		Item[] items = { 
				Item.of("do commit", GitItems::commit), 
				Item.of("commit content", GitItems::commitContent), 				
				Item.of("create branch", GitItems::createBranch),
				Item.of("rename branch", GitItems::renameBranch),								
				Item.of("list of branches", GitItems::branches),		
				Item.of("delete branch", GitItems::deleteBranch), 
				Item.of("switch to", GitItems::switchTo),
				Item.of("add ignore regex", GitItems::addRegex),
				Item.of("info", GitItems::info),
				Item.of("log", GitItems::log),
				Item.of("exit", io -> gitRepository.save(), true) 
			};
		
		return items;
	}

	static void commit(InputOutput io) {
		io.writeLine(gitRepository.commit(io.readString("Enter commit message")));
	}
	
	static void commitContent(InputOutput io) {
		io.writeLine(gitRepository.commitContent(io.readString("Enter commit name")));
	}
	
	static void createBranch(InputOutput io) {	
		io.writeLine(gitRepository.createBranch(enterBranch(io, "branch name")));
	}
	
	static void renameBranch(InputOutput io) {
		String oldBranchName = enterBranch(io, "old branch name");
		String newBranchName = enterBranch(io, "new branch name");
		if (isExist(oldBranchName)) {			
			io.writeLine(gitRepository.renameBranch(oldBranchName, newBranchName));
		} else {
			io.writeLine("branch not exist");
		}
	}	
	
	static void deleteBranch(InputOutput io) {
		String branchName = enterBranch(io, "branch name");
		if (isExist(branchName)) {
			io.writeLine(gitRepository.deleteBranch(branchName));
		} else {
			io.writeLine("branch not exist");
		}
	}
	
	private static boolean isExist(String branchName) {		
		return gitRepository.branches().stream().anyMatch(name -> name.contains(branchName));
	}
	
	private static String enterBranch(InputOutput io, String prompt) {
		return io.readStringPredicate("Enter " + prompt, "Wrong name of branch", t -> t.matches("\\w+"));
	}
	

	static void branches(InputOutput io) {
		gitRepository.branches().forEach(io::writeLine);
	}
	
	
	static void switchTo(InputOutput io) {
		io.writeLine(gitRepository.switchTo(io.readString("Enter either branch or commit name for switching to")));
	}

	static void addRegex(InputOutput io) {
		String regex = io.readStringPredicate("Enter regular expression", "Wrong regular expression", e -> {
			boolean res = true;
			try {
				"git".matches(e);
			} catch (Exception error) {
				res = false;
			}
			return res;
		});
		io.writeLine(gitRepository.addIgnoredFileNameExp(regex));
	}

	static void info(InputOutput io) {
		gitRepository.info().forEach(io::writeLine);
	}
	
	static void log(InputOutput io) {
		gitRepository.log().forEach(io::writeLine);
	}
	
	
}