package com.mycompany.testerefdiff2.v;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mycompany.testerefdiff2.v.CLI.CLIExecution;
import com.mycompany.testerefdiff2.v.CLI.CLIExecute;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import refdiff.core.RefDiff;
import refdiff.core.diff.CstDiff;
import refdiff.core.diff.Relationship;
import refdiff.parsers.java.JavaPlugin;

/**
 *
 * @author kleit
 */
public class ExecRefDiff20v {

    public static String dataPasta = "data/";
    public static String logFile = "data/log.json";
    public static long exeMaxTime = 3600000;
    public static Date dataInicio;

    public static void main(String[] args) throws Exception {
        String caminhoDoArquivo = dataPasta + "java-teste.xlsx";

        Map<String, String[]> projetos = lerPlanilha(caminhoDoArquivo);

        for (Map.Entry<String, String[]> projeto : projetos.entrySet()) {
            String nomeProjeto = projeto.getKey();
            String[] atributos = projeto.getValue();

            String url = atributos[0];
            dataInicio = new Date();

            final Thread[] thread = new Thread[1];
            final boolean[] isTimedOut = {false};

            thread[0] = new Thread(() -> {
                long startTime = System.currentTimeMillis();
                try {
                    checar(nomeProjeto, url);
                } catch (Exception e) {
                    salvarLog(nomeProjeto, dataInicio, new Date(), 0, false, 0, "Mensagem: \n   " + e.getMessage() + "\n\nException: \n   " + e.toString() + "\n\nTEMPO MAXIMO ATINGIDO\n");
                }
                long endTime = System.currentTimeMillis();
                long executionTime = endTime - startTime;
                if (executionTime > exeMaxTime) { // timeout
                    isTimedOut[0] = true;
                }

            });

            thread[0].start();

            try {
                thread[0].join(exeMaxTime); // Aguarda a conclusão da tarefa ou o tempo de execução
                if (isTimedOut[0]) {

                    thread[0].interrupt(); // Interrompe a thread se exceder o tempo
                }
            } catch (InterruptedException e) {
                salvarLog(nomeProjeto, dataInicio, new Date(), 0, false, 0, "Mensagem: \n   " + e.getMessage() + "\n\nException: \n   " + e.toString() + "\n\nTEMPO MAXIMO ATINGIDO\n");
            }

        }
    }

    static void checar(String projectName, String projectUrl) throws Exception {
        long tempoInicial = System.currentTimeMillis();
        File tempFolder = new File("tmp");
        JavaPlugin javaPlugin = new JavaPlugin(tempFolder);
        RefDiff refDiffJava = new RefDiff(javaPlugin);
        File gitRepo = refDiffJava.cloneGitRepository(new File(tempFolder, projectName), projectUrl);
        Map<String, List<String>> refactoringMap = new HashMap<>();
        List<String> commits = printCommits("tmp/" + projectName);
        List<ErroCommit> erros = new ArrayList<>();

        System.out.println("Iniciando...");
        System.out.println("Projeto " + projectUrl);

        try {

            for (String commitId : commits) {

                try {

                    CstDiff diff = refDiffJava.computeDiffForCommit(gitRepo, commitId);

                    List<String> refactoringList = new ArrayList<>();
                    for (Relationship rel : diff.getRefactoringRelationships()) {

                        String referencia =  rel.getStandardDescription() + " " + rel.toString();
                        referencia = referencia.replace(",", " ");
                        referencia = referencia.replace(";", " ");
                        refactoringList.add(rel.getType().toString() + ", " + referencia);
                       
                    }
                    refactoringMap.put(commitId, refactoringList);

                } catch (Exception e) {
                    erros.add(new ErroCommit(commitId, e));
                }
            }
        } catch (Exception e) {
            System.out.println("ERRO " + e.getMessage());
        }

        long tempoFinal = System.currentTimeMillis();
        long tempoDecorrido = tempoFinal - tempoInicial;
        Date dataTermino = new Date();
        int quantidadeDecommitsAnalisados = contarCommitsAnalisados(refactoringMap);

        System.out.println("Salvando dados...");

        salvarPlanilha(refactoringMap, projectName);
        salvarLog(projectName, dataInicio, dataTermino, tempoDecorrido, true, quantidadeDecommitsAnalisados, "Sucesso!");
        //salvarLogsErros(erros, projectName);

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
        try (PrintWriter writer = new PrintWriter(new File(dataPasta + projectName + "-ref-diff.csv"))) {

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
                    sb.append(',');
                    String definicao = "";
                    for (int i = 2; i < refatocao.length; i++) {
                        definicao += refatocao[i];
                    }
                    sb.append(definicao);
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
            Sheet sheet = workbook.getSheetAt(0); // Assumindo que a planilha que você deseja ler está na primeira aba (índice 0)
            System.out.println("Projetos Listados:");
            for (Row row : sheet) {
                String nomeProjeto = row.getCell(0).getStringCellValue();
                String url = row.getCell(1).getStringCellValue();
                String[] atributos = {url};
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

        FileWriter arquivo = new FileWriter(dataPasta + projectName + ".txt", true); // O segundo argumento "true" indica que você vai acrescentar ao arquivo existente, se houver.
        // Substitua pelo nome do seu projeto.
        try (PrintWriter gravarArquivo = new PrintWriter(arquivo)) {
            // Substitua pelo nome do seu projeto.
            gravarArquivo.println("Projeto: " + projectName);
            gravarArquivo.println("Tempo da função: " + tempoDecorrido + " ms");
        }
    }

    public static void salvarLog(String nomeProjeto, Date dataInicio, Date dataTermino, long tempoDecorrido, boolean sucesso, int numeroCommits, String mensage) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

        // Criar um objeto de log
        LogEntry logEntry = new LogEntry(nomeProjeto, dateFormat.format(dataInicio), dateFormat.format(dataTermino), tempoDecorrido, sucesso, numeroCommits, mensage
        );

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

        String fullPath = "data/" + projectName + "-ref-diff-logerros.json";

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
