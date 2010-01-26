package cuke4duke.ant;

import org.apache.tools.ant.BuildException;

import java.io.File;

public class GemBundlerTask extends JRubyTask {
    private File gemFile;

    public GemBundlerTask() {
        createJvmarg().setValue("-Xmx384m"); 
    }

    public void execute() {
        createArg().setValue("-S");
        createArg().setValue("gem");
        createArg().setValue("bundle");
        createArg().setValue("-m");
        createArg().setFile(gemFile);
        super.execute();
    }

    public void setGemFile(File gemFile) {
        this.gemFile = gemFile;
    }
}
