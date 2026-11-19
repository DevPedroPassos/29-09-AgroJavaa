package org.example;

import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;

/**
 * Nome: Pedro Luiz Agrella Passos
 * Versão 1 - AgroJava
 *
 * V1 (Analytics): histórico de chuvas em CSV (RF04 a RF07)
 * V2 (SmartFarm): mapa de umidade, classificação e relatórios (RF08 a RF12)
 */
public class AgroJava_v2 {

    // ===================== Arquivos =====================
    static final String ARQ_CHUVAS = "historico_chuvas.csv";
    static final String ARQ_TALHOES = "mapa_umidade.csv";
    static final String ARQ_CLASSIFICADOS = "talhoes_classificados.csv";
    static final String ARQ_RELATORIO = "relatorio_fazenda.txt";

    static final String CAB_CHUVAS = "data,seg,ter,qua,qui,sex,sab,dom,media,total";
    static final String[] DIAS = {"segunda-feira", "terça-feira", "quarta-feira",
            "quinta-feira", "sexta-feira", "sábado", "domingo"};

    static final DateTimeFormatter FORMATO_DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // ===================== Dados em memória =====================
    // Matriz 4x4 de umidade (linhas A-D, colunas 1-4)
    static double[][] umidadeSolo = new double[4][4];
    static boolean talhoesCadastrados = false;

    static Scanner input = new Scanner(System.in);

    public static void main(String[] args) {
        carregarMapaSalvo(); // permite usar os talhões salvos em execuções anteriores
        menuPrincipal();
        input.close();
    }

    // ===================== Menu =====================
    static void menuPrincipal() {
        int opcao;
        do {
            System.out.println("\n==============================================");
            System.out.println("    SISTEMA DE GESTÃO AGRÍCOLA INTELIGENTE    ");
            System.out.println("==============================================");
            System.out.println("--- AgroJava Analytics (Chuvas) ---");
            System.out.println("1) Registrar Semana Agrícola");
            System.out.println("2) Consultar Histórico de Chuvas");
            System.out.println("3) Identificar Semana Mais Chuvosa");
            System.out.println("4) Dashboard Climático");
            System.out.println("--- AgroJava SmartFarm (Talhões) ---");
            System.out.println("5) Cadastrar Umidade dos Talhões");
            System.out.println("6) Exibir Mapa do Campo");
            System.out.println("7) Relatório de Alertas de Irrigação");
            System.out.println("8) Relatório Gerencial da Fazenda");
            System.out.println("9) Exportar Relatório (relatorio_fazenda.txt)");
            System.out.println("0) Sair");

            opcao = lerInt("Escolha uma opção: ");

            switch (opcao) {
                case 1:
                    cadastrarChuvas();
                    break;
                case 2:
                    consultarHistoricoChuvas();
                    break;
                case 3:
                    identificarSemanaMaisChuvosa();
                    break;
                case 4:
                    gerarDashboardClimatico();
                    break;
                case 5:
                    cadastrarTalhoes();
                    break;
                case 6:
                    exibirMapaCampo();
                    break;
                case 7:
                    gerarRelatorioIrrigacao();
                    break;
                case 8:
                    gerarRelatorioGerencial();
                    break;
                case 9:
                    exportarRelatorio();
                    break;
                case 0:
                    System.out.println("\nEncerrando o sistema agrícola...");
                    break;
                default:
                    System.out.println("\nOpção inválida. Digite um número de 0 a 9.");
            }
        } while (opcao != 0);
    }

    //  V1 - ANALYTICS

    // ---------- RF04 ----------
    static void cadastrarChuvas() {
        System.out.println("\n--- [RF04] Registrar Semana Agrícola ---");

        String data = lerData("Data da semana (dd/MM/yyyy): ");

        double[] chuvas = new double[7];
        double total = 0;
        for (int i = 0; i < chuvas.length; i++) {
            chuvas[i] = lerDouble("Chuva de " + DIAS[i] + " (mm): ", 0, Double.MAX_VALUE);
            total += chuvas[i];
        }
        double media = total / chuvas.length;

        System.out.printf(Locale.US, "%nMédia semanal: %.1f mm | Total: %s mm%n", media, formatar(total));

        salvarHistoricoChuvas(data, chuvas, media, total);
    }

    static void salvarHistoricoChuvas(String data, double[] chuvas, double media, double total) {
        File arquivo = new File(ARQ_CHUVAS);
        boolean novo = !arquivo.exists() || arquivo.length() == 0;

        // append = true: não sobrescreve semanas anteriores
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(arquivo, true))) {
            if (novo) {
                bw.write(CAB_CHUVAS);
                bw.newLine();
            }
            StringBuilder linha = new StringBuilder(data);
            for (double c : chuvas) {
                linha.append(",").append(formatar(c));
            }
            linha.append(",").append(String.format(Locale.US, "%.1f", media));
            linha.append(",").append(formatar(total));
            bw.write(linha.toString());
            bw.newLine();
            System.out.println("Semana registrada com sucesso em " + ARQ_CHUVAS + "!");
        } catch (IOException e) {
            System.out.println("Erro ao gravar o histórico: " + e.getMessage());
        }
    }

    /** Lê o CSV de chuvas e devolve cada registro como String[] (cabeçalho ignorado). */
    static List<String[]> lerHistoricoChuvas() {
        List<String[]> registros = new ArrayList<>();
        File arquivo = new File(ARQ_CHUVAS);
        if (!arquivo.exists()) {
            return registros;
        }
        try (BufferedReader br = new BufferedReader(new FileReader(arquivo))) {
            String linha = br.readLine(); // ignora o cabeçalho
            while ((linha = br.readLine()) != null) {
                if (linha.trim().isEmpty()) {
                    continue;
                }
                String[] campos = linha.split(",");
                if (campos.length == 10) {
                    registros.add(campos);
                }
            }
        } catch (IOException e) {
            System.out.println("Erro ao ler o histórico: " + e.getMessage());
        }
        return registros;
    }

    // ---------- RF05 ----------
    static void consultarHistoricoChuvas() {
        System.out.println("\n--- [RF05] Histórico de Chuvas ---");
        List<String[]> registros = lerHistoricoChuvas();

        if (registros.isEmpty()) {
            System.out.println("Nenhuma semana registrada ainda. Use a opção 1.");
            return;
        }
        for (String[] r : registros) {
            System.out.println("\nSemana: " + r[0]);
            System.out.println("Média: " + formatar(Double.parseDouble(r[8])) + " mm");
            System.out.println("Total: " + formatar(Double.parseDouble(r[9])) + " mm");
        }
    }

    // ---------- RF06 ----------
    static void identificarSemanaMaisChuvosa() {
        System.out.println("\n--- [RF06] Semana Mais Chuvosa ---");
        List<String[]> registros = lerHistoricoChuvas();

        if (registros.isEmpty()) {
            System.out.println("Nenhuma semana registrada ainda. Use a opção 1.");
            return;
        }
        String[] maisChuvosa = registros.get(0);
        for (String[] r : registros) {
            if (Double.parseDouble(r[9]) > Double.parseDouble(maisChuvosa[9])) {
                maisChuvosa = r;
            }
        }
        System.out.println("Semana mais chuvosa:");
        System.out.println(maisChuvosa[0]);
        System.out.println("Total:");
        System.out.println(formatar(Double.parseDouble(maisChuvosa[9])) + " mm");
    }

    // ---------- RF07 ----------
    static void gerarDashboardClimatico() {
        List<String[]> registros = lerHistoricoChuvas();

        if (registros.isEmpty()) {
            System.out.println("\nNenhuma semana registrada ainda. Use a opção 1.");
            return;
        }
        double acumulado = 0;
        double maior = Double.parseDouble(registros.get(0)[9]);
        double menor = maior;
        String semanaSeca = registros.get(0)[0];
        String semanaChuvosa = registros.get(0)[0];

        for (String[] r : registros) {
            double total = Double.parseDouble(r[9]);
            acumulado += total;
            if (total > maior) {
                maior = total;
                semanaChuvosa = r[0];
            }
            if (total < menor) {
                menor = total;
                semanaSeca = r[0];
            }
        }
        double mediaGeral = acumulado / registros.size();

        System.out.println("\n===== DASHBOARD =====");
        System.out.println("Semanas cadastradas: " + registros.size());
        System.out.println("Volume acumulado: " + formatar(acumulado) + " mm");
        System.out.println("Maior chuva:");
        System.out.println(formatar(maior) + " mm (semana " + semanaChuvosa + ")");
        System.out.println("Menor chuva:");
        System.out.println(formatar(menor) + " mm (semana " + semanaSeca + ")");
        System.out.println("Média geral:");
        System.out.println(String.format(Locale.US, "%.1f mm", mediaGeral));
    }

    //  V2 - SMARTFARM

    // ---------- Cadastro da matriz + RF08 ----------
    static void cadastrarTalhoes() {
        System.out.println("\n--- Mapeamento de Umidade do Solo (Matriz 4x4) ---");
        for (int linha = 0; linha < 4; linha++) {
            for (int coluna = 0; coluna < 4; coluna++) {
                umidadeSolo[linha][coluna] = lerDouble(
                        "Umidade (%) do talhão " + nomeTalhao(linha, coluna) + ": ", 0, 100);
            }
        }
        talhoesCadastrados = true;
        System.out.println("\nTodos os dados foram cadastrados com sucesso!");

        salvarMapaTalhoes();
        salvarTalhoesClassificados();
    }

    // ---------- RF08 ----------
    static void salvarMapaTalhoes() {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(ARQ_TALHOES))) {
            bw.write("talhao,umidade");
            bw.newLine();
            for (int linha = 0; linha < 4; linha++) {
                for (int coluna = 0; coluna < 4; coluna++) {
                    bw.write(nomeTalhao(linha, coluna) + "," + formatar(umidadeSolo[linha][coluna]));
                    bw.newLine();
                }
            }
            System.out.println("Mapa de umidade salvo em " + ARQ_TALHOES + " (16 talhões).");
        } catch (IOException e) {
            System.out.println("Erro ao salvar o mapa de umidade: " + e.getMessage());
        }
    }

    // ---------- RF09 (persistência da classificação) ----------
    static void salvarTalhoesClassificados() {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(ARQ_CLASSIFICADOS))) {
            bw.write("talhao,umidade,status");
            bw.newLine();
            for (int linha = 0; linha < 4; linha++) {
                for (int coluna = 0; coluna < 4; coluna++) {
                    double u = umidadeSolo[linha][coluna];
                    bw.write(nomeTalhao(linha, coluna) + "," + formatar(u) + "," + classificarTalhao(u));
                    bw.newLine();
                }
            }
        } catch (IOException e) {
            System.out.println("Erro ao salvar a classificação dos talhões: " + e.getMessage());
        }
    }

    /** Carrega o mapa salvo anteriormente (se existir) para a matriz em memória. */
    static void carregarMapaSalvo() {
        File arquivo = new File(ARQ_TALHOES);
        if (!arquivo.exists()) {
            return;
        }
        double[][] temp = new double[4][4];
        int lidos = 0;
        try (BufferedReader br = new BufferedReader(new FileReader(arquivo))) {
            String linha = br.readLine(); // cabeçalho
            while ((linha = br.readLine()) != null && lidos < 16) {
                String[] campos = linha.split(",");
                if (campos.length != 2) {
                    continue;
                }
                temp[lidos / 4][lidos % 4] = Double.parseDouble(campos[1]);
                lidos++;
            }
        } catch (IOException | NumberFormatException e) {
            return; // arquivo inválido: começa sem dados
        }
        if (lidos == 16) {
            umidadeSolo = temp;
            talhoesCadastrados = true;
        }
    }

    static void exibirMapaCampo() {
        if (!verificarTalhoes()) {
            return;
        }
        System.out.println("\n--- Mapa de Umidade do Campo (Matriz 4x4) ---");
        System.out.println("      1         2         3         4");
        for (int linha = 0; linha < 4; linha++) {
            System.out.print((char) ('A' + linha) + "  ");
            for (int coluna = 0; coluna < 4; coluna++) {
                System.out.printf(Locale.US, "[%5.1f%%]  ", umidadeSolo[linha][coluna]);
            }
            System.out.println();
        }
        System.out.println("\n--- [RF09] Classificação dos Talhões ---");
        System.out.println("talhao,umidade,status");
        for (int linha = 0; linha < 4; linha++) {
            for (int coluna = 0; coluna < 4; coluna++) {
                double u = umidadeSolo[linha][coluna];
                System.out.println(nomeTalhao(linha, coluna) + "," + formatar(u) + "," + classificarTalhao(u));
            }
        }
    }

    // ---------- RF09 ----------
    /** < 20 Crítico | 20 a 30 Atenção | > 30 Adequado */
    static String classificarTalhao(double umidade) {
        if (umidade < 20) {
            return "Crítico";
        } else if (umidade <= 30) {
            return "Atenção";
        } else {
            return "Adequado";
        }
    }

    // ---------- RF10 ----------
    static void gerarRelatorioIrrigacao() {
        if (!verificarTalhoes()) {
            return;
        }
        System.out.println();
        System.out.print(montarRelatorioIrrigacao());
    }

    static String montarRelatorioIrrigacao() {
        StringBuilder sb = new StringBuilder("===== ALERTAS =====\n");
        int alertas = 0;
        for (int linha = 0; linha < 4; linha++) {
            for (int coluna = 0; coluna < 4; coluna++) {
                String status = classificarTalhao(umidadeSolo[linha][coluna]);
                if (!status.equals("Adequado")) {
                    sb.append(nomeTalhao(linha, coluna)).append(" - ").append(status).append("\n");
                    alertas++;
                }
            }
        }
        if (alertas == 0) {
            sb.append("Nenhum talhão necessita de irrigação.\n");
        }
        sb.append("Total de alertas: ").append(alertas).append("\n");
        return sb.toString();
    }

    // ---------- RF11 ----------
    static void gerarRelatorioGerencial() {
        if (!verificarTalhoes()) {
            return;
        }
        System.out.println();
        System.out.print(montarRelatorioGerencial(false));
    }

    /** Monta o relatório gerencial; com data=true inclui a linha "Data:" (usado no arquivo). */
    static String montarRelatorioGerencial(boolean comData) {
        double soma = 0;
        int criticos = 0, atencao = 0, adequados = 0;
        double menor = umidadeSolo[0][0], maior = umidadeSolo[0][0];
        String secoNome = nomeTalhao(0, 0), umidoNome = nomeTalhao(0, 0);

        for (int linha = 0; linha < 4; linha++) {
            for (int coluna = 0; coluna < 4; coluna++) {
                double u = umidadeSolo[linha][coluna];
                soma += u;
                switch (classificarTalhao(u)) {
                    case "Crítico":
                        criticos++;
                        break;
                    case "Atenção":
                        atencao++;
                        break;
                    default:
                        adequados++;
                }
                if (u < menor) {
                    menor = u;
                    secoNome = nomeTalhao(linha, coluna);
                }
                if (u > maior) {
                    maior = u;
                    umidoNome = nomeTalhao(linha, coluna);
                }
            }
        }
        double media = soma / 16;
        double percIrrigacao = (criticos + atencao) * 100.0 / 16;

        StringBuilder sb = new StringBuilder("===== RELATORIO DA FAZENDA =====\n");
        if (comData) {
            sb.append("Data: ").append(LocalDate.now().format(FORMATO_DATA)).append("\n");
        }
        sb.append(String.format(Locale.US, "Media Geral: %.1f%%%n", media));
        sb.append("Talhoes Criticos: ").append(criticos).append("\n");
        sb.append("Talhoes em Atencao: ").append(atencao).append("\n");
        sb.append("Talhoes Adequados: ").append(adequados).append("\n");
        sb.append("Necessidade de Irrigacao:\n");
        sb.append(String.format(Locale.US, "%.1f%%%n", percIrrigacao));
        sb.append("Talhao mais seco:\n");
        sb.append(secoNome).append(" - ").append(formatar(menor)).append("%\n");
        sb.append("Talhao mais umido:\n");
        sb.append(umidoNome).append(" - ").append(formatar(maior)).append("%\n");
        return sb.toString();
    }

    // ---------- RF12 ----------
    static void exportarRelatorio() {
        if (!verificarTalhoes()) {
            return;
        }
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(ARQ_RELATORIO))) {
            bw.write(montarRelatorioGerencial(true));
            bw.newLine();
            bw.write(montarRelatorioIrrigacao());
            System.out.println("\nRelatório exportado com sucesso para " + ARQ_RELATORIO + "!");
        } catch (IOException e) {
            System.out.println("Erro ao exportar o relatório: " + e.getMessage());
        }
    }

    //  UTILITÁRIOS

    static boolean verificarTalhoes() {
        if (!talhoesCadastrados) {
            System.out.println("\nAviso: Cadastre a umidade dos talhões na Opção 5 primeiro.");
            return false;
        }
        return true;
    }

    /** Linha 0-3 vira A-D e coluna 0-3 vira 1-4 (ex.: [0][0] = A1, [3][3] = D4). */
    static String nomeTalhao(int linha, int coluna) {
        return "" + (char) ('A' + linha) + (coluna + 1);
    }

    /** Remove o ".0" de números inteiros (70.0 -> 70) e mantém decimais (12.5). */
    static String formatar(double valor) {
        if (valor == Math.rint(valor)) {
            return String.valueOf((long) valor);
        }
        return String.valueOf(valor);
    }

    static int lerInt(String mensagem) {
        while (true) {
            System.out.print(mensagem);
            try {
                return Integer.parseInt(input.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Entrada inválida. Digite um número inteiro.");
            }
        }
    }

    /** Aceita vírgula ou ponto como separador decimal e valida o intervalo. */
    static double lerDouble(String mensagem, double min, double max) {
        while (true) {
            System.out.print(mensagem);
            try {
                double v = Double.parseDouble(input.nextLine().trim().replace(',', '.'));
                if (v >= min && v <= max) {
                    return v;
                }
                System.out.println("Valor fora do intervalo permitido.");
            } catch (NumberFormatException e) {
                System.out.println("Entrada inválida. Digite um número.");
            }
        }
    }

    static String lerData(String mensagem) {
        while (true) {
            System.out.print(mensagem);
            String texto = input.nextLine().trim();
            try {
                return LocalDate.parse(texto, FORMATO_DATA).format(FORMATO_DATA);
            } catch (DateTimeParseException e) {
                System.out.println("Data inválida. Use o formato dd/MM/yyyy (ex.: 10/11/2026).");
            }
        }
    }
}