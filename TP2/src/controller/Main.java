package controller;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Scanner;
import Algoritmos.ExternalSort;
import Algoritmos.InvertedList;
import controller.*;
import Model.steam;

public class Main {
    public static void main(String[] args) {
        Actions actions = new Actions(); // Instância da classe que gerencia as operações do CRUD
        Scanner scanner = new Scanner(System.in); // Scanner para entrada de dados pelo usuário
        ExternalSort sorter = new ExternalSort(); // Instância do algoritmo de ordenação externa
        
        try {
            actions.openFile(); // Abre o arquivo contendo os dados
        } catch (IOException e) {
            System.err.println("Erro ao abrir arquivo: " + e.getMessage());
            return; // Sai do programa caso haja erro ao abrir o arquivo
        }

        while (true) {
            // Exibição do menu
            System.out.println("\n==== MENU ====");
            System.out.println("1. Carregar dados do CSV(Se o arquivo Db estiver vazio)");
            System.out.println("2. Criar jogo");
            System.out.println("3. Ler jogo");
            System.out.println("4. Atualizar jogo");
            System.out.println("5. Deletar jogo");
            System.out.println("6. Ordenação Externa");
            System.out.println("7. Ordenação Indexada");
            System.out.println("8. Sair");
            
            System.out.print("Escolha uma opção: ");

            int choice = scanner.nextInt(); // Captura a escolha do usuário
            scanner.nextLine();

            switch (choice) {
                case 1:
                    actions.loadData(); // Carrega dados do CSV se o banco estiver vazio
                    break;
                case 2:
                    // Criação de um novo jogo
                    System.out.print("Digite o ID do jogo: ");
                    int id = scanner.nextInt();
                    scanner.nextLine();
                    System.out.print("Digite o nome do jogo: ");
                    String name = scanner.nextLine();
                    System.out.print("Digite a data de lançamento (AAAA-MM-DD): ");
                    LocalDate date = LocalDate.parse(scanner.nextLine());
                    System.out.print("Digite as plataformas (separadas por vírgula): ");
                    String[] plats = scanner.nextLine().split(",");
                    ArrayList<String> platforms = new ArrayList<>();
                    for (String p : plats) {
                        platforms.add(p.trim());
                    }
                    System.out.print("Digite o gênero do jogo: ");
                    String genre = scanner.nextLine();
                    String launchBefore2010 = date.getYear() < 2010 ? "SIM" : "NAO";
                    steam newGame = new steam(id, name, date, platforms, genre, launchBefore2010);
                    if (actions.createGame(newGame)) {
                        System.out.println("Jogo criado com sucesso!");
                    } else {
                        System.out.println("Erro ao criar jogo.");
                    }
                    break;
                case 3:
                    // Leitura de um jogo pelo ID
                    try {
                        System.out.print("Digite o ID do jogo para leitura: ");
                        int searchId = scanner.nextInt();
                        steam game = actions.readGame(searchId);
                        if (game != null) {
                            System.out.println("\n===== 🎮 JOGO ENCONTRADO =====");
                            System.out.println(game);
                            System.out.println("================================");
                        } else {
                            System.out.println("🚫 Jogo não encontrado.");
                        }
                    } catch (IOException e) {
                        System.err.println("❌ Erro ao ler jogo: " + e.getMessage());
                    }
                    break;
                case 4:
                    // Atualização de um jogo existente
                    System.out.print("Digite o ID do jogo para atualizar: ");
                    int updateId = scanner.nextInt();
                    scanner.nextLine();
                    System.out.print("Digite o novo nome do jogo: ");
                    String newName = scanner.nextLine();
                    System.out.print("Digite a nova data de lançamento (AAAA-MM-DD): ");
                    LocalDate newDate = LocalDate.parse(scanner.nextLine());
                    System.out.print("Digite as novas plataformas (separadas por vírgula): ");
                    String[] newPlats = scanner.nextLine().split(",");
                    ArrayList<String> newPlatforms = new ArrayList<>();
                    for (String p : newPlats) {
                        newPlatforms.add(p.trim());
                    }
                    System.out.print("Digite o novo gênero do jogo: ");
                    String newGenre = scanner.nextLine();
                    String newLaunchBefore2010 = newDate.getYear() < 2010 ? "SIM" : "NAO";
                    steam updatedGame = new steam(updateId, newName, newDate, newPlatforms, newGenre, newLaunchBefore2010);
                    if (actions.updateGame(updateId, updatedGame)) {
                        System.out.println("Jogo atualizado com sucesso!");
                    } else {
                        System.out.println("Erro ao atualizar jogo.");
                    }
                    break;
                case 5:
                    // Exclusão de um jogo
                    System.out.print("Digite o ID do jogo para deletar: ");
                    int deleteId = scanner.nextInt();
                    steam deletedGame = actions.deleteGame(deleteId);
                    if (deletedGame != null) {
                        System.out.println("Jogo deletado: " + deletedGame);
                    } else {
                        System.out.println("Erro ao deletar jogo.");
                    }
                    break;
                case 6:
                    // Ordenação externa
                    System.out.print("Digite o número de caminhos: ");
                    int numCaminhos = scanner.nextInt();
                    System.out.print("Digite o número máximo de registros na memória: ");
                    int maxRegistrosMemoria = scanner.nextInt();
                    sorter.externalSort(numCaminhos, maxRegistrosMemoria);
                    break;
                // No arquivo Main.java, modifique o case 7 (opção de indexação):
                case 7:
                System.out.println("=== Tipo de Indexação ===");
                System.out.println("1 - Árvore");
                System.out.println("2 - Hash");
                System.out.println("3 - Lista Invertida");
                System.out.print("Escolha o tipo de indexação: ");
                int tipo = scanner.nextInt();
                scanner.nextLine(); // Limpa o buffer
                
                switch (tipo) {
                    case 1:
                        System.out.println("Árvore ainda não implementada.");
                        break;
                    case 2:
                        System.out.println("Hash ainda não implementado.");
                        break;
                    case 3:
                        System.out.println("Executando Lista Invertida...");
                        actions.menuListaInvertida(scanner);
                        break;
                    default:
                        System.out.println("Opção inválida.");
                }
                break;
case 8:
    System.out.println("Saindo...");
    scanner.close();
    return;

                default:
                    System.out.println("Opção inválida, tente novamente.");
            }
        }
    }
}//comentario e a