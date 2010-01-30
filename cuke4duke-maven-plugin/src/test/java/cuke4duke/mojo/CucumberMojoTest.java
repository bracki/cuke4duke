package cuke4duke.mojo;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
        mojo.buildDirectory = new File(".");
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
        assertTrue("Gemfile should have been created.", gemFile.exists());
        // Now look for expected tokens in file.
        Scanner scanner = new Scanner(gemFile).useDelimiter("\\Z");
        String contents = scanner.next();
        scanner.close();
        assertTrue("Gemfile should have specified gem and version", contents.contains("gem \"gem\", \"1.2.3\""));
        assertTrue("Gemfile should have bundle_path set to #jrubyHome()/gems", contents.contains("bundle_path \""+ new File(mojo.jrubyHome(), "gems").getAbsolutePath() + "\""));
        assertTrue("Gemfile should have bin_path set to #jrubyHome()/bin", contents.contains("bin_path \""+ new File(mojo.jrubyHome(), "bin").getAbsolutePath() + "\""));
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
