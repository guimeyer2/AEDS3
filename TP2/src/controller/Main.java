package controller;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import javax.annotation.processing.FilerException;
import Algoritmos.ExternalSort;
import Algoritmos.HashExtensivel;
import Algoritmos.InvertedList;
import controller.*;
import Model.steam;



public class Main extends HashCrud {

    private int selected;
    private Scanner sc;
    public  Main() {
      this.selected = 0;
    }
    public void executeMenu() {
        try {
          selected = 0;
          this.openFile();
          this.selectOption();
          this.executeOption();
        } catch (Exception e) {
          System.err.println("Erro ao executar Menu: " + e);
        }
      }
      public void selectOption() {
        System.out.println("1. Carregar dados do CSV");
        System.out.println("2. Criar jogo");
        System.out.println("3. Ler jogo");
        System.out.println("4. Atualizar jogo");
        System.out.println("5. Deletar jogo");
        System.out.println("6. Carregar dados para o Hash");
        System.out.println("7. Ver game utilizando Hash");
        System.out.println("8. Criar game utilizando Hash");
        System.out.println("9. Deletar game utilizando Hash");
        System.out.println("10. Ordenar utilizando lista invertida");
        System.out.println("11. Indexar utilizando Árvore B");
        System.out.println("12. Sair");
        
        
        System.out.print("Escolha uma opção: ");
        int input = Integer.parseInt(sc.nextLine()); // input do usuário
    
        if (input < 1 || input > 12) {
          System.out.println(
            "\nOpção inválida inserida, por favor tente novamente:"
          );
          input = Integer.parseInt(sc.nextLine());
        }
    
        selected = input;
      }
      public void executeOption() throws FileNotFoundException {
        Actions actions = new Actions();
        ExternalSort sorter = new ExternalSort();
    
        try {
            actions.openFile(); // Tenta abrir o arquivo
        } catch (IOException e) {
            System.err.println("Erro ao abrir arquivo: " + e.getMessage());
            return;
        }
    
        try {
            switch (this.selected) {
                case 1:
                    actions.loadData();
                    break;
    
                case 2:
                    System.out.print("Digite o ID do jogo: ");
                    int id = Integer.parseInt(sc.nextLine());
                    System.out.print("Digite o nome do jogo: ");
                    String name = sc.nextLine();
                    System.out.print("Digite a data de lançamento (AAAA-MM-DD): ");
                    LocalDate date = LocalDate.parse(sc.nextLine());
                    System.out.print("Digite as plataformas (separadas por vírgula): ");
                    ArrayList<String> platforms = new ArrayList<>();
                    for (String p : sc.nextLine().split(",")) platforms.add(p.trim());
                    System.out.print("Digite o gênero do jogo: ");
                    String genre = sc.nextLine();
                    String launchBefore2010 = date.getYear() < 2010 ? "SIM" : "NAO";
    
                    steam newGame = new steam(id, name, date, platforms, genre, launchBefore2010);
                    System.out.println(actions.createGame(newGame) ? "Jogo criado com sucesso!" : "Erro ao criar jogo.");
                    break;
    
                case 3:
                    System.out.print("Digite o ID do jogo para leitura: ");
                    int searchId = Integer.parseInt(sc.nextLine());
                    steam game = actions.readGame(searchId);
                    if (game != null) {
                        System.out.println("\n===== 🎮 JOGO ENCONTRADO =====");
                        System.out.println(game);
                        System.out.println("================================");
                    } else {
                        System.out.println("🚫 Jogo não encontrado.");
                    }
                    break;
    
                case 4:
                    System.out.print("Digite o ID do jogo para atualizar: ");
                    int updateId = Integer.parseInt(sc.nextLine());
                    System.out.print("Digite o novo nome: ");
                    String newName = sc.nextLine();
                    System.out.print("Digite a nova data de lançamento (AAAA-MM-DD): ");
                    LocalDate newDate = LocalDate.parse(sc.nextLine());
                    System.out.print("Digite as novas plataformas: ");
                    ArrayList<String> newPlatforms = new ArrayList<>();
                    for (String p : sc.nextLine().split(",")) newPlatforms.add(p.trim());
                    System.out.print("Digite o novo gênero: ");
                    String newGenre = sc.nextLine();
                    String newLaunchBefore2010 = newDate.getYear() < 2010 ? "SIM" : "NAO";
                    steam updatedGame = new steam(updateId, newName, newDate, newPlatforms, newGenre, newLaunchBefore2010);
                    System.out.println(actions.updateGame(updateId, updatedGame) ? "Atualizado com sucesso!" : "Erro ao atualizar.");
                    break;
    
                case 5:
                    System.out.print("Digite o ID do jogo para deletar: ");
                    int deleteId = Integer.parseInt(sc.nextLine());
                    steam deletedGame = actions.deleteGame(deleteId);
                    System.out.println(deletedGame != null ? "Jogo deletado: " + deletedGame : "Erro ao deletar jogo.");
                    break;
    
                case 6:
                    System.out.println("Carregando dados para Hash...");
                    this.loadDataToHash();
                    System.out.println("Dados carregados para Hash com sucesso!");
                    break;
    
                case 7:
                    System.out.print("Insira o ID do game: ");
                    int searchHashId = Integer.parseInt(sc.nextLine());
                    steam foundHash = this.readHashh(searchHashId);
                    if (foundHash != null) {
                        System.out.println("\n===== JOGO ENCONTRADO COM HASH =====");
                        System.out.println(foundHash);
                    } else {
                        System.out.println("Game não encontrado ou deletado.");
                    }
                    break;
    
                case 8:
                    this.createHashh(new steam());
                    System.out.println("Game criado com Hash!");
                    break;
    
                case 9:
                    System.out.print("Insira o ID para deletar com Hash: ");
                    int delHashId = Integer.parseInt(sc.nextLine());
                    steam deletedHash = deleteHashh(delHashId);
                    System.out.println(deletedHash != null ? "Game deletado com Hash." : "Erro ao deletar com Hash.");
                    break;
                    
                case 10:
                    System.out.println("Executando Lista Invertida...");
                    actions.menuListaInvertida(sc);
                    break;
                    
                case 11:
                    System.out.println("Executando Árvore B...");
                    actions.menuArvoreB(sc);
                    break;
                    
                case 12:
                    System.out.println("\nObrigado por usar nosso Banco de Dados! :)");
                    sc.close();
                    this.closeFile();
                    break;
    
                default:
                    System.out.println("Opção inválida.");
                    break;
            }
    
        } catch (Exception e) {
            System.err.println("Erro na função executeOption: " + e.getMessage());
        }
    }
    public void setScanner(Scanner sc) {
        this.sc = sc;
    }
    public static void main(String[] args) {
        try (Scanner scanner = new Scanner(System.in)) {
            Main menu = new Main();
            menu.setScanner(scanner);  // Injeção do scanner externo
            menu.executeMenu();
        }
    }
}

//comentario e algumas melhorias feitas por IA