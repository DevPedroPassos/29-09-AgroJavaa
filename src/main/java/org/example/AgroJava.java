package org.example;

import java.util.Scanner;

public class AgroJava {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        double[] chuvas = new double[7];
        double[][] umidade = new double[4][4];

        boolean dadosCadastrados = false;
        int opcao;

        do {
            System.out.println("\n=== AGROJAVA - SENAI ===");
            System.out.println("1) Cadastrar Dados");
            System.out.println("2) Exibir Mapa do Campo");
            System.out.println("3) Relatório de Alertas de Irrigação");
            System.out.println("4) Sair");
            System.out.print("Escolha uma opção: ");
            opcao = scanner.nextInt();

            switch (opcao) {
                case 1:
                    System.out.println("\n=== REGISTRO DE CHUVAS ===");

                    for (int i = 0; i < chuvas.length; i++) {
                        System.out.print("Informe a chuva do dia " + (i + 1) + " em mm: ");
                        chuvas[i] = scanner.nextDouble();
                    }

                    System.out.println("\n=== REGISTRO DE UMIDADE ===");

                    for (int i = 0; i < umidade.length; i++) {
                        for (int j = 0; j < umidade[i].length; j++) {
                            System.out.print("Informe a umidade do talhão [" + i + "][" + j + "] em %: ");
                            umidade[i][j] = scanner.nextDouble();
                        }
                    }

                    dadosCadastrados = true;

                    double somaChuva = 0;
                    double maiorChuva = chuvas[0];
                    int diaMaiorChuva = 0;

                    for (int i = 0; i < chuvas.length; i++) {
                        somaChuva += chuvas[i];

                        if (chuvas[i] > maiorChuva) {
                            maiorChuva = chuvas[i];
                            diaMaiorChuva = i;
                        }
                    }

                    double mediaChuva = somaChuva / chuvas.length;

                    System.out.println("\nMédia semanal de pluviosidade: " + mediaChuva + " mm");
                    System.out.println("Maior índice de chuva: " + maiorChuva + " mm");
                    System.out.println("Dia com maior chuva: " + (diaMaiorChuva + 1));

                    break;

                case 2:
                    if (!dadosCadastrados) {
                        System.out.println("Cadastre os dados primeiro.");
                        break;
                    }

                    System.out.println("\n=== MAPA DO CAMPO ===");

                    for (int i = 0; i < umidade.length; i++) {
                        for (int j = 0; j < umidade[i].length; j++) {
                            System.out.printf("%.1f%%\t", umidade[i][j]);
                        }
                        System.out.println();
                    }

                    break;

                case 3:
                    if (!dadosCadastrados) {
                        System.out.println("Cadastre os dados primeiro.");
                        break;
                    }

                    System.out.println("\n=== ALERTAS DE IRRIGAÇÃO ===");

                    boolean encontrouAlerta = false;

                    for (int i = 0; i < umidade.length; i++) {
                        for (int j = 0; j < umidade[i].length; j++) {
                            if (umidade[i][j] < 30) {
                                System.out.println("Talhão [" + i + "][" + j + "] necessita de irrigação. Umidade: " + umidade[i][j] + "%");
                                encontrouAlerta = true;
                            }
                        }
                    }

                    if (!encontrouAlerta) {
                        System.out.println("Nenhum talhão necessita de irrigação.");
                    }

                    break;

                case 4:
                    System.out.println("Programa encerrado.");
                    break;

                default:
                    System.out.println("Opção inválida.");
            }

        } while (opcao != 4);

        scanner.close();
    }
}