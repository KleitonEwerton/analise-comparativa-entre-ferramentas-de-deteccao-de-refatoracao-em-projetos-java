/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.testerefactoringminer2.v;

/**
 *
 * @author kleit
 */
public class LogEntry {

    private String nomeProjeto;
    private String dataInicio;
    private String dataTermino;
    private long tempoDecorrido;
    private boolean sucesso;
    private int numeroCommits;
    private String mensagemExcecao;

    public LogEntry(String nomeProjeto, String dataInicio, String dataTermino, long tempoDecorrido, boolean sucesso, int numeroCommits, String mensagemExcecao) {
        this.nomeProjeto = nomeProjeto;
        this.dataInicio = dataInicio;
        this.dataTermino = dataTermino;
        this.tempoDecorrido = tempoDecorrido;
        this.sucesso = sucesso;
        this.numeroCommits = numeroCommits;
        this.mensagemExcecao = mensagemExcecao;
    }
}
