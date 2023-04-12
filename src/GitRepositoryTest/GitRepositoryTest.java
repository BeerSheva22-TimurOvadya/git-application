package GitRepositoryTest;

import org.junit.jupiter.api.*;
import telran.myGit.GitRepositoryImpl;
import static org.junit.jupiter.api.Assertions.*;
import java.util.*;

public class GitRepositoryTest {
    private GitRepositoryImpl gitRepository;

    @BeforeEach
    public void setUp() throws Exception {
        gitRepository = (GitRepositoryImpl) GitRepositoryImpl.init();
    }

    @AfterEach
    public void tearDown() {
        gitRepository = null;
    }

    @Test
    public void testCommit() {
        String commitMessage = "Test commit";
        String response = gitRepository.commit(commitMessage);
        assertNotNull(response);
        assertTrue(response.contains("Commit done"));
    }

    @Test
    public void testCreateBranch() {
        String branchName = "testBranch";
        String response = gitRepository.createBranch(branchName);
        assertNotNull(response);
          }

    @Test
    public void testRenameBranch() {
        String branchName = "testBranch";
        String newBranchName = "renamedTestBranch";
        gitRepository.createBranch(branchName);
        String response = gitRepository.renameBranch(branchName, newBranchName);
        assertNotNull(response);
       
    }

    @Test
    public void testDeleteBranch() {
        String branchName = "testBranch";
        gitRepository.createBranch(branchName);
        String response = gitRepository.deleteBranch(branchName);
        assertNotNull(response);
        
    }

    @Test
    public void testBranches() {
        List<String> branches = gitRepository.branches();
        assertNotNull(branches);
    }

   

    @Test
    public void testAddIgnoredFileNameExp() {
        String regex = ".*\\.txt";
        String response = gitRepository.addIgnoredFileNameExp(regex);
        assertNotNull(response);
        assertTrue(response.contains("Regex for files ignored is"));
    }
}