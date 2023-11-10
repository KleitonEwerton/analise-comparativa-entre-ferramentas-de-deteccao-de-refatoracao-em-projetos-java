package com.mycompany.testerefactoringminer2.v.CLI;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class ReadStream implements Runnable {

    /**
     * @return the output
     */
    public List<String> getOutput() {
        return output;
    }

    /**
     * @param output the output to set
     */
    public void setOutput(List<String> output) {
        this.output = output;
    }
    InputStream is;
    Thread thread;
    private List<String> output;

    public ReadStream(InputStream is) {

        this.is = is;
        this.output = new ArrayList<>();
    }

    public void start() {
        thread = new Thread(this);
        thread.start();
    }

    public void run() {
        try {
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);
            while (true) {
                String s = br.readLine();
                if (s == null) {
                    break;
                }
                getOutput().add(s);
            }
            is.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
