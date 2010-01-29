package cuke4duke.mojo;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.DefaultArtifactRepository;
import org.apache.maven.artifact.repository.layout.DefaultRepositoryLayout;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class CucumberMojoTest {

    private CucumberMojo mojo;

    @Before
    public void setUp() {
        mojo = new CucumberMojo();
        mojo.launchDirectory = new File(".");
        mojo.mavenProject = new MavenProject();
        mojo.mavenProject.setFile(new File("../."));
        mojo.compileClasspathElements = new ArrayList<String>();
        mojo.pluginArtifacts = new ArrayList<Artifact>();
        mojo.testClasspathElements = new ArrayList<String>();
        mojo.localRepository = new DefaultArtifactRepository("", "", new DefaultRepositoryLayout());
    }

    @Test
    public void shouldBuildGemFileFromGems() throws Exception {
        mojo.gems = new ArrayList<Gem>();
        Gem g = new Gem();
        g.setName("gem");
        g.setVersion("1.2.3");
        mojo.gems.add(g);
        File gemFile = mojo.gemFileFromGems();
        assertTrue(gemFile.exists());
        Scanner scanner = new Scanner(gemFile).useDelimiter("\\Z");
        String contents = scanner.next();
        scanner.close();
        assertTrue(contents.matches("gem \"gem\", \"1\\.2\\.3\""));
    }

    @Test
    public void shouldAddCucumberArgs() {
        String cucumberArg = "testArg";
        mojo.cucumberArgs = new ArrayList<String>();
        mojo.cucumberArgs.add(cucumberArg);
        assertTrue(mojo.allCucumberArgs().contains(cucumberArg));
    }

    @Test
    public void shouldAllowZeroAddCucumberArgs() {
        mojo.extraCucumberArgs = null;
        mojo.allCucumberArgs();
    }

    @Test
    public void shouldSplitAddCucumberArgsIntoRealCucumberArgs() {
        mojo.extraCucumberArgs = "arg1 arg2 arg3";
        assertEquals("arg1 arg2 arg3 features", mojo.allCucumberArgs());
    }

    @Test
    public void shouldIgnoreNullJvmArg() throws MojoExecutionException {
        mojo.jvmArgs = Arrays.asList("-Dfoo=bar", null, "");
        assertEquals(Arrays.asList("-Dfoo=bar", ""), Arrays.asList(mojo.cucumber("").getCommandLine().getVmCommand().getArguments()));
    }
}
