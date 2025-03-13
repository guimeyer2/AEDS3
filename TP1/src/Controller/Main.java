package controller;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Scanner;
import controller.Actions;
import Model.steam;

public class Main {
    public static void main(String[] args) {
        Actions actions = new Actions();
        Scanner scanner = new Scanner(System.in);
        
        try {
            actions.openFile();
        } catch (IOException e) {
            System.err.println("Erro ao abrir arquivo: " + e.getMessage());
            return;
        }

        while (true) {
            System.out.println("\n==== MENU ====");
            System.out.println("1. Carregar dados do CSV");
            System.out.println("2. Criar jogo");
            System.out.println("3. Ler jogo");
            System.out.println("4. Atualizar jogo");
            System.out.println("5. Deletar jogo");
            System.out.println("6. Sair");
            System.out.print("Escolha uma opção: ");
            
            int choice = scanner.nextInt();
            scanner.nextLine();
            
            switch (choice) {


                case 1: //LOAD
                    actions.loadData();
                    break;




                    case 2: // CREATE
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
                
                    // Determina o valor de LaunchBefore2010 com base na data de lançamento
                    String launchBefore2010 = date.getYear() < 2010 ? "SIM" : "NAO";
                
                    // Cria o objeto steam com o campo LaunchBefore2010
                    steam newGame = new steam(id, name, date, platforms, genre, launchBefore2010);
                
                    if (actions.createGame(newGame)) {
                        System.out.println("Jogo criado com sucesso!");
                    } else {
                        System.out.println("Erro ao criar jogo.");
                    }
                    break;


                    case 3: // READ
                    try {
                        System.out.print("Digite o ID do jogo para leitura: ");
                        int searchId = scanner.nextInt();
                        steam game = actions.readGame(searchId);
                
                        if (game != null) {
                            System.out.println("\n===== 🎮 JOGO ENCONTRADO =====");
                            System.out.println(game); // Usa o toString() para exibir os detalhes corretamente
                            System.out.println("================================");
                        } else {
                            System.out.println("🚫 Jogo não encontrado.");
                        }
                    } catch (IOException e) {
                        System.err.println("❌ Erro ao ler jogo: " + e.getMessage());
                    }
                    break;
                



                    case 4: // UPDATE
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
                
                    // Determina o valor de LaunchBefore2010 com base na nova data de lançamento
                    String newLaunchBefore2010 = newDate.getYear() < 2010 ? "SIM" : "NAO";
                
                    // Cria o objeto steam com o campo LaunchBefore2010
                    steam updatedGame = new steam(updateId, newName, newDate, newPlatforms, newGenre, newLaunchBefore2010);
                
                    if (actions.updateGame(updateId, updatedGame)) {
                        System.out.println("Jogo atualizado com sucesso!");
                    } else {
                        System.out.println("Erro ao atualizar jogo.");
                    }
                    break;


                case 5: //DELETE
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
                    try {
                        actions.closeFile();
                    } catch (IOException e) {
                        System.err.println("Erro ao fechar arquivo: " + e.getMessage());
                    }
                    System.out.println("Saindo...");
                    scanner.close();
                    return;
                default:
                    System.out.println("Opção inválida, tente novamente.");
            }
        }
    }
}
