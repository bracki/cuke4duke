package cuke4duke.mojo;

import cuke4duke.ant.CucumberTask;
import cuke4duke.internal.Utils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.tools.ant.types.Commandline;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @goal cucumber
 */
public class CucumberMojo extends AbstractJRubyMojo {

    /**
     * @parameter expression="${cucumber.features}"
     */
    protected String features = "features";

    /**
     * @parameter expression="${cucumber.installGems}"
     */
    protected boolean installGems = false;

    /**
     * @parameter expression="${cucumber.bundleGems}"
     */
    protected boolean bundleGems = false;

    /**
     * The target directory of the compiler if fork is true.
     *
     * @parameter default-value="${project.build.directory}"
     * @required
     * @readonly
     */
    protected File buildDirectory;

    /**
     * Will cause the project build to look successful, rather than fail, even if there are Cucumber test failures.
     * This can be useful on a continuous integration server, if your only option to be able to collect output files,
     * is if the project builds successfully.
     *
     * @parameter expression="${cucumber.failOnError}"
     */
    protected boolean failOnError = true;

    /**
     * @parameter
     */
    protected List<Gem> gems;

    /**
     * @parameter
     */
    protected List<String> cucumberArgs = Collections.<String>emptyList();

    /**
     * Appends additional arguments on the command line. e.g.
     * <code>-Dcucumber.extraArgs="--format profile --out target/profile.txt"</code>
     * These arguments will be appended to the cucumberArgs you declare
     * in your POM.
     *
     * @parameter expression="${cucumber.extraArgs}
     */
    protected String extraCucumberArgs;

    /**
     * Extra JVM arguments to pass when running JRuby.
     *
     * @parameter
     */
    protected List<String> jvmArgs;

    public void execute() throws MojoExecutionException {
        /*
        if (installGems && !bundleGems) {
            for (String gemSpec : gems) {
                installGem(gemSpec);
            }
        }*/

        if (!installGems && bundleGems) {
            bundleGems(gemFileFromGems());
        }

        CucumberTask cucumber = cucumber(allCucumberArgs());
        try {
            cucumber.execute();
        } catch (Exception e) {
            if (failOnError) {
                throw new MojoExecutionException("JRuby failed.", e);
            }
        }
    }

    public File gemFileFromGems() throws MojoExecutionException {
        if (!buildDirectory.exists()) {
            //noinspection ResultOfMethodCallIgnored
            buildDirectory.mkdirs();
        }
        try {
            File gemFile = new File(buildDirectory, "Gemfile");
            PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(gemFile)));
            for (Gem gem : gems) {
                writer.printf("gem \"%s\", \"%s\"", gem.getName(), gem.getVersion());
                writer.println();
            }
            writer.printf("bundle_path \"%s\"", relativeToJrubyHome("gems"));
            writer.println();
            writer.printf("bin_path \"%s\"", relativeToJrubyHome("bin"));
            writer.println();
            writer.println("source \"http://gems.github.com\"");
            writer.close();
            return gemFile;
        } catch (IOException e) {
            throw new MojoExecutionException("Couldn't create Gemfile.", e);
        }
    }

    /**
     * Get an absolute path relative to {@link #jrubyHome()}. Replace file separator with "/" so we
     * have Ruby compliant path representations. At least on Windows.
     * @param child the child
     * @return a string representing an absolute path
     */
    protected String relativeToJrubyHome(String child) {
        return new File(jrubyHome(), child).getAbsolutePath().replace(File.separator, "/");
    }

    public CucumberTask cucumber(String args) throws MojoExecutionException {
        CucumberTask cucumber = new CucumberTask();
        cucumber.setProject(getProject());
        for(String jvmArg : getJvmArgs()) {
            if(jvmArg != null) {
                Commandline.Argument arg = cucumber.createJvmarg();
                arg.setValue(jvmArg);
            }
        }
        cucumber.setArgs(args);
        return cucumber;
    }

    String allCucumberArgs() {
        List<String> allCucumberArgs = new ArrayList<String>();
        if (cucumberArgs != null)
            allCucumberArgs.addAll(cucumberArgs);
        if (extraCucumberArgs != null)
            allCucumberArgs.add(extraCucumberArgs);
        allCucumberArgs.add(features);
        return Utils.join(allCucumberArgs.toArray(), " ");
    }

    protected List<String> getJvmArgs() {
        return (jvmArgs != null) ? jvmArgs : Collections.<String>emptyList();
    }
}
