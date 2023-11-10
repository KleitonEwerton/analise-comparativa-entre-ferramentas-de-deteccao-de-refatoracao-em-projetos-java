package com.mycompany.testerefactoringminer2.v.CLI;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

public class CLIExecute {

    public static CLIExecution execute(String command, String directory) throws IOException {

        CLIExecution execution = new CLIExecution();

        Runtime runtime = Runtime.getRuntime();
        
        Process exec = runtime.exec(command, null,
                new File(directory));

        String s;

        BufferedReader stdInput = new BufferedReader(new InputStreamReader(exec.getInputStream()));
        BufferedReader stdError = new BufferedReader(new InputStreamReader(exec.getErrorStream()));

        // read the output from the command
        while ((s = stdInput.readLine()) != null) {
            execution.addOutput(s);
        }

        // read any errors from the attempted command
        while ((s = stdError.readLine()) != null) {
            execution.addError(s);
        }

        return execution;
    }

    public static CLIExecution execute(String[] command, String directory) throws IOException {

        CLIExecution execution = new CLIExecution();

        Runtime runtime = Runtime.getRuntime();
        Process exec = runtime.exec(command, null,
                new File(directory));

        String s;

        BufferedReader stdInput = new BufferedReader(new InputStreamReader(exec.getInputStream()));
        BufferedReader stdError = new BufferedReader(new InputStreamReader(exec.getErrorStream()));

        // read the output from the command
        if (stdInput.ready()) {
            while ((s = stdInput.readLine()) != null) {
                execution.addOutput(s);
            }
        }

        // read any errors from the attempted command
        if (stdError.ready()) {
            while ((s = stdError.readLine()) != null) {
                execution.addError(s);
            }
        }
        return execution;
    }

    public static CLIExecution executeParallel(String command, String directory) throws IOException {

        try {
            CLIExecution execution = new CLIExecution();

            Runtime runtime = Runtime.getRuntime();
            Process exec = runtime.exec(command, null,
                    new File(directory));

            ReadStream in = new ReadStream(exec.getInputStream());
            ReadStream er = new ReadStream(exec.getErrorStream());
            in.start();
            er.start();
            exec.waitFor();

            execution.setError(er.getOutput());
            execution.setOutput(in.getOutput());
            return execution;
        } catch (InterruptedException ex) {
            return new CLIExecution();
        }
    }

    public static CLIExecution execute(String cmd) throws IOException {

        CLIExecution execution = new CLIExecution();

        Runtime runtime = Runtime.getRuntime();
        Process exec = runtime.exec(cmd);
        String s;

        BufferedReader stdInput = new BufferedReader(new InputStreamReader(exec.getInputStream()));
        BufferedReader stdError = new BufferedReader(new InputStreamReader(exec.getErrorStream()));

        // read the output from the command
        while ((s = stdInput.readLine()) != null) {
            execution.addOutput(s);
        }

        // read any errors from the attempted command
        while ((s = stdError.readLine()) != null) {
            execution.addError(s);
        }

        return execution;
    }

    public static CLIExecution execute(String[] cmd) throws IOException {

        CLIExecution execution = new CLIExecution();

        Runtime runtime = Runtime.getRuntime();
        Process exec = runtime.exec(cmd);
        String s;

        BufferedReader stdInput = new BufferedReader(new InputStreamReader(exec.getInputStream()));
        BufferedReader stdError = new BufferedReader(new InputStreamReader(exec.getErrorStream()));

        // read the output from the command
        while ((s = stdInput.readLine()) != null) {
            execution.addOutput(s);
        }

        // read any errors from the attempted command
        while ((s = stdError.readLine()) != null) {
            execution.addError(s);
        }

        return execution;
    }

}
