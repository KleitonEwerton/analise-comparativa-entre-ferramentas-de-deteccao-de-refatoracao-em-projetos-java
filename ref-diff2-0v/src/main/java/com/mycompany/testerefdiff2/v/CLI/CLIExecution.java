package com.mycompany.testerefdiff2.v.CLI;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author gleiph
 */
public class CLIExecution {

    private List<String> output;
    private List<String> error;

    public CLIExecution() {
        this.output = new ArrayList<>();
        this.error = new ArrayList<>();
    }

    /**
     * @return the output
     */
    public List<String> getOutput() {
        return output;
    }

    /**
     * @return number of line output
     */
    public int getNumberLine() {
        return output.size();
    }

    /**
     * @param output the output to set
     */
    public void setOutput(List<String> output) {
        this.output = output;
    }

    /**
     * @return the error
     */
    public List<String> getError() {
        return error;
    }

    /**
     * @param error the error to set
     */
    public void setError(List<String> error) {
        this.error = error;
    }

    public void addOutput(String line) {
        this.output.add(line);
    }

    public void addError(String line) {
        this.error.add(line);
    }

    @Override
    public String toString() {
        String result = "";

        result += "==========================Output==============================\n";
        for (String string : output) {
            result += string + "\n";
        }

        if (!error.isEmpty()) {
            result += "==========================Error==============================\n";
            for (String string : error) {
                result += string + "\n";
            }
        }
        return result;

    }

    public String getOutputString() {

        List<String> output = this.getOutput();

        StringBuilder sb = new StringBuilder();
        for (String line : output) {
            sb.append(line).append("\n");
        }
        return sb.toString();
    }

}
