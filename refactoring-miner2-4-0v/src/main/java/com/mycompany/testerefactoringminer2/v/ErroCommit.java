/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.testerefactoringminer2.v;

/**
 *
 * @author kleit
 */
public class ErroCommit {

    private String hash;
    private String exception;
    private String exceptionMessage;

    public ErroCommit(String hash, Exception exception) {
        this.hash = hash;
        this.exception = exception.getClass().getName();
        this.exceptionMessage = exception.getMessage();
    }

    public String getHash() {
        return hash;
    }

    public String getException() {
        return exception;
    }

    public String getExceptionMessage() {
        return exceptionMessage;
    }
}
