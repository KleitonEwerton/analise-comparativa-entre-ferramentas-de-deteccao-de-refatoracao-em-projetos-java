/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.testerefdiff2.v;

/**
 *
 * @author kleit
 */
public class ErroCommit {

    private String reference;
    private String exception;
    private String exceptionMessage;

    public ErroCommit(String hash, Exception exception) {
        this.reference = hash;
        this.exception = exception.getClass().getName();
        this.exceptionMessage = exception.getMessage();
    }

    public String getReference() {
        return reference;
    }

    public String getException() {
        return exception;
    }

    public String getExceptionMessage() {
        return exceptionMessage;
    }
}
