package telran.myGit;

import telran.view.*;

public class GitRepositoryAppl {

	public static void main(String[] args) {
		InputOutput io = new StandardInputOutput();
		try {
			Item[] items = GitItems.getGitRepositoryItems(GitRepositoryImpl.init());
			Menu menu = new Menu("Git Console Commands Menu", items);
			menu.perform(io);
		} catch (Exception e) {
			io.writeLine("Error: " + e.getMessage());
		}

	}

}