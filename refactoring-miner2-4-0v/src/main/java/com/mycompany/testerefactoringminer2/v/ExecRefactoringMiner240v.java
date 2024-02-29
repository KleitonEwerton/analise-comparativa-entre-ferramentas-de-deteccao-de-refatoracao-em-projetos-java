/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */
package com.mycompany.testerefactoringminer2.v;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mycompany.testerefactoringminer2.v.CLI.CLIExecute;
import com.mycompany.testerefactoringminer2.v.CLI.CLIExecution;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;
import org.eclipse.jgit.lib.Repository;
import org.refactoringminer.api.GitHistoryRefactoringMiner;
import org.refactoringminer.api.GitService;
import org.refactoringminer.api.Refactoring;
import org.refactoringminer.api.RefactoringHandler;
import org.refactoringminer.rm1.GitHistoryRefactoringMinerImpl;
import org.refactoringminer.util.GitServiceImpl;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class ExecRefactoringMiner240v {

    // Configurações
    public static String dataPasta = "data/";
    public static String logFile = "data/log.json";
    public static long exeMaxTime = 3600000;
    public static Date dataInicio;

    // Main class
    public static void main(String[] args) throws Exception {
        String caminhoDoArquivo = dataPasta + "java-teste.xlsx";

        Map<String, String[]> projetos = lerPlanilha(caminhoDoArquivo);

        for (Map.Entry<String, String[]> projeto : projetos.entrySet()) {
            String nomeProjeto = projeto.getKey();
            String[] atributos = projeto.getValue();

            String url = atributos[0];
            dataInicio = new Date();

            final Thread[] thread = new Thread[1];
            final boolean[] isTimedOut = { false };

            // Cria uma thread para executar a tarefa
            thread[0] = new Thread(() -> {
                long startTime = System.currentTimeMillis();
                try {
                    checar(nomeProjeto, url);
                } catch (Exception e) {
                    salvarLog(nomeProjeto, dataInicio, new Date(), 0, false, 0, "Mensagem: \n   " + e.getMessage()
                            + "\n\nException: \n   " + e.toString() + "\n\nTEMPO MAXIMO ATINGIDO\n");
                }
                long endTime = System.currentTimeMillis();
                long executionTime = endTime - startTime;
                if (executionTime > exeMaxTime) { // timeout
                    isTimedOut[0] = true;
                }

            });

            // Inicia a thread
            thread[0].start();

            try {
                thread[0].join(exeMaxTime); // Aguarda a conclusão da tarefa ou o tempo de execução
                if (isTimedOut[0]) {

                    // Interrompe a thread se exceder o tempo
                    thread[0].interrupt(); // Interrompe a thread se exceder o tempo
                }
            } catch (InterruptedException e) {
                salvarLog(nomeProjeto, dataInicio, new Date(), 0, false, 0, "Mensagem: \n   " + e.getMessage()
                        + "\n\nException: \n   " + e.toString() + "\n\nTEMPO MAXIMO ATINGIDO\n");
            }

        }

    }

    // Funções
    public static void checar(String projectName, String projectUrl) throws Exception {

        long tempoInicial = System.currentTimeMillis();

        GitService gitService = new GitServiceImpl();
        GitHistoryRefactoringMiner miner = new GitHistoryRefactoringMinerImpl();

        Repository repo = gitService.cloneIfNotExists(
                "tmp/" + projectName,
                projectUrl);

        Map<String, List<String>> refactoringMap = new HashMap<>();
        List<String> commits = printCommits("tmp/" + projectName);
        List<ErroCommit> erros = new ArrayList<>();

        System.out.println("Iniciando...");
        System.out.println("Projeto " + projectUrl);

        try {
            for (String hashCommit : commits) {

                try {
                    miner.detectAtCommit(repo, hashCommit, new RefactoringHandler() {
                        @Override
                        public void handle(String commitId, List<Refactoring> refactorings) {
                            List<String> refactoringList = new ArrayList<>();

                            for (Refactoring ref : refactorings) {

                                String refactoringType = ref.getRefactoringType().toString();
                                String referencia = ref.getName() + " " + ref.toString();
                                referencia = referencia.replace(",", " ");
                                referencia = referencia.replace(";", " ");
                                refactoringList.add(refactoringType + ", " + referencia);
                            }

                            refactoringMap.put(commitId, refactoringList);
                        }
                    });
                } catch (Exception e) {
                    erros.add(new ErroCommit(hashCommit, e));
                }
            }

        } catch (Exception e) {
        }

        long tempoFinal = System.currentTimeMillis();
        long tempoDecorrido = tempoFinal - tempoInicial;
        Date dataTermino = new Date();
        int quantidadeDecommitsAnalisados = contarCommitsAnalisados(refactoringMap);

        System.out.println("Salvando dados...");

        salvarPlanilha(refactoringMap, projectName);
        salvarLog(projectName, dataInicio, dataTermino, tempoDecorrido, true, quantidadeDecommitsAnalisados,
                "Sucesso!");
        salvarLogsErros(erros, projectName);

        System.out.println("Finalizado!");
    }

    public static List<String> printCommits(String path) throws IOException {

        String command = "git log --all --pretty=\"format:'%H''%P'\"";

        CLIExecution execute = CLIExecute.execute(command, path);

        List<String> hashs = new ArrayList<>();
        if (!execute.getError().isEmpty()) {
            throw new RuntimeException("The path does not have a Git Repository.");
        }

        for (String line : execute.getOutput()) {

            int hashBegin = line.indexOf("\'");
            int hashEnd = line.indexOf("\'", hashBegin + 1);
            int parentsBegin = line.indexOf("\'", hashEnd + 1);
            int parentsEnd = line.indexOf("\'", parentsBegin + 1);

            String hash = line.substring(hashBegin + 1, hashEnd);
            String parents = line.substring(parentsBegin + 1, parentsEnd);

            String parenstsArray[] = parents.split(" ");
            List<String> parentsList = new ArrayList<>();
            for (String parent : parenstsArray) {
                parentsList.add(parent);
            }

            if (parentsList.size() == 1) {
                hashs.add(hash);

            }
        }

        return hashs;

    }

    public static void salvarPlanilha(Map<String, List<String>> refactoringMap, String projectName) {
        try (PrintWriter writer = new PrintWriter(new File(dataPasta + projectName + "-ref-miner.csv"))) {

            StringBuilder sb = new StringBuilder();
            sb.append("Hash");
            sb.append(',');
            sb.append("Refatoração");
            sb.append(',');
            sb.append("Descrição");

            sb.append('\n');

            for (Map.Entry<String, List<String>> entry : refactoringMap.entrySet()) {
                String hash = entry.getKey();
                List<String> refactoringList = entry.getValue();

                for (String ref : refactoringList) {
                    String refatocao[] = ref.split(",");
                    sb.append(hash);
                    sb.append(',');
                    sb.append(refatocao[0]);
                    sb.append(',');
                    sb.append(refatocao[1]);
                    sb.append('\n');

                }
            }

            writer.write(sb.toString());
            System.out.println("done!");

        } catch (FileNotFoundException e) {

        }
    }

    public static Map<String, String[]> lerPlanilha(String caminhoDoArquivo) {
        Map<String, String[]> projetos = new HashMap<>();

        try (FileInputStream arquivo = new FileInputStream(caminhoDoArquivo)) {
            Workbook workbook = new XSSFWorkbook(arquivo);
            Sheet sheet = workbook.getSheetAt(0); // Assumindo que a planilha que você deseja ler está na primeira aba
                                                  // (índice 0)
            System.out.println("Projetos Listados:");
            for (Row row : sheet) {
                String nomeProjeto = row.getCell(0).getStringCellValue();
                String url = row.getCell(1).getStringCellValue();
                String[] atributos = { url };
                projetos.put(nomeProjeto, atributos);
                System.out.println(" - " + nomeProjeto + " URL:" + url);
            }

            arquivo.close();
        } catch (IOException e) {
        }
        System.out.println("\n");
        return projetos;
    }

    public static void salvarTempo(String projectName, long tempoDecorrido) throws IOException {

        FileWriter arquivo = new FileWriter(dataPasta + projectName + ".txt", true); // O segundo argumento "true"
                                                                                     // indica que você vai acrescentar
                                                                                     // ao arquivo existente, se houver.
        // Substitua pelo nome do seu projeto.
        try (PrintWriter gravarArquivo = new PrintWriter(arquivo)) {
            // Substitua pelo nome do seu projeto.
            gravarArquivo.println("Projeto: " + projectName);
            gravarArquivo.println("Tempo da função: " + tempoDecorrido + " ms");
        }
    }

    public static void salvarLog(String nomeProjeto, Date dataInicio, Date dataTermino, long tempoDecorrido,
            boolean sucesso, int numeroCommits, String mensage) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

        // Criar um objeto de log
        LogEntry logEntry = new LogEntry(nomeProjeto, dateFormat.format(dataInicio), dateFormat.format(dataTermino),
                tempoDecorrido, sucesso, numeroCommits, mensage);

        // Converter o objeto em JSON
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String logJson = gson.toJson(logEntry);

        try {
            // Abrir o arquivo JSON em modo de anexação
            FileWriter fileWriter = new FileWriter(logFile, true);
            // Adicionar o log JSON ao arquivo
            try (BufferedWriter writer = new BufferedWriter(fileWriter)) {
                // Adicionar o log JSON ao arquivo
                writer.write(logJson);
                writer.newLine();
                // Fechar o arquivo
            }
        } catch (IOException e) {
        }
    }

    public static void salvarLogErro(ErroCommit erro, String projectName) {

        String fullPath = "data/" + projectName + "-refactoring-miner-logerros.json";

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String logErroJson = gson.toJson(erro);

        try {

            FileWriter fileWriter = new FileWriter(fullPath, true);

            try (BufferedWriter writer = new BufferedWriter(fileWriter)) {

                writer.write(logErroJson);
                writer.newLine();

            }
        } catch (IOException e) {
        }

    }

    public static void salvarLogsErros(List<ErroCommit> erros, String projectName) {

        for (ErroCommit erroCommit : erros) {
            salvarLogErro(erroCommit, projectName);
        }

    }

    public static <K, V> int contarCommitsAnalisados(Map<K, V> map) {
        Set<K> chavesUnicas = new HashSet<>();

        for (K chave : map.keySet()) {
            chavesUnicas.add(chave);
        }

        return chavesUnicas.size();
    }

}
