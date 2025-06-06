package controller;

import java.io.File; 
import java.io.FileNotFoundException;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Scanner;
import Algoritmos.Huffman;
import Algoritmos.LZW;
import Model.steam;


public class Main extends HashCrud {

    private int selected;
    private Scanner sc;
    
    private static final String DATABASE_FILENAME = "TP2\\src\\steam.db";

    public Main() {
        this.selected = 0;
    }

    public void executeMenu() {
        try {
            selected = 0;
            this.openFile();
            while (true) {
                this.selectOption();
                if (!this.executeOption()) break;
            }
        } catch (Exception e) {
            System.err.println("Erro ao executar Menu: " + e);
            e.printStackTrace(); 
        }
    }

    public void selectOption() {
        System.out.println("\n=== MENU PRINCIPAL ===");
        System.out.println("1. Carregar dados do CSV");
        System.out.println("2. Criar jogo");
        System.out.println("3. Ler jogo");
        System.out.println("4. Atualizar jogo");
        System.out.println("5. Deletar jogo");
        System.out.println("6. Indexar com Hash");
        System.out.println("7. Ordenar utilizando lista invertida");
        System.out.println("8. Indexar utilizando Árvore B");
        System.out.println("9. Comprimir base de dados (Huffman)");   // Nova opção
        System.out.println("10. Descomprimir base de dados (Huffman)"); // Nova opção
        System.out.println("11. Descomprimir base de dados (LZW)"); // Nova opção
        System.out.println("12. Sair");                               

        System.out.print("Escolha uma opção (1-11): ");
        
        
        while (true) {
            try {
                String line = sc.nextLine();
                int input = Integer.parseInt(line);
                if (input >= 1 && input <= 12) {
                    selected = input;
                    break;
                } else {
                    System.out.print("Opção inválida. Por favor, escolha entre 1 e 11: ");
                }
            } catch (NumberFormatException e) {
                System.out.print("Entrada inválida. Por favor, digite um número: ");
            }
        }
    }

    public boolean executeOption() throws FileNotFoundException {
        Actions actions = new Actions();
        

        try {
            
            actions.openFile();
        } catch (IOException e) {
            System.err.println("Erro ao abrir arquivo para Actions: " + e.getMessage());
            return true; 
        }

        try {
            switch (this.selected) {
                case 1:
                    actions.loadData();
                    break;
                case 2:
                    createNewGame(actions);
                    break;
                case 3:
                    readGame(actions);
                    break;
                case 4:
                    updateGame(actions);
                    break;
                case 5:
                    deleteGame(actions);
                    break;
                case 6:
                    executeHashMenu();
                    break;
                case 7:
                    System.out.println("Executando Lista Invertida...");
                    actions.menuListaInvertida(sc);
                    break;
                case 8:
                    System.out.println("Executando Árvore B...");
                    actions.menuArvoreB(sc);
                    break;
                case 9: 
                    compressDatabaseHuffman();
                    break;
                case 10: 
                    decompressDatabaseHuffman();
                    break;
                case 11:
                    menuCompressaoLZW();
                    break;
                    case 12: 
                    System.out.println("\nObrigado por usar nosso Banco de Dados! :)");
                    if (sc != null) { 
                        
                    }
                    this.closeFile(); 
                    return false; 
                default:
                    System.out.println("Opção inválida.");
                    break;
            }
        } catch (Exception e) {
            System.err.println("Erro na função executeOption: " + e.getMessage());
            e.printStackTrace(); 
        }
        return true; 
    }

    // verifica quantos huffmans ja foram criados e retorna o próximo número de versão
    private int getNextHuffmanversao(String baseFileName) {
        int versao = 1;
        while (true) {
           
            File compressedFile = new File(baseFileName + "Huffman" + versao);
            if (!compressedFile.exists()) {
                return versao;
            }
            versao++;
        }
    }

    
    private void compressDatabaseHuffman() {
        Huffman huffmanCompressor = new Huffman();
        int versao = getNextHuffmanversao(DATABASE_FILENAME);
        String inputFile = DATABASE_FILENAME;
        String outputFile = DATABASE_FILENAME + "Huffman" + versao; 

        System.out.println("\nIniciando compressão com Huffman...");
        System.out.println("Arquivo de entrada: " + inputFile);
        System.out.println("Arquivo de saída: " + outputFile);
       


        File dbFile = new File(inputFile);
        if (!dbFile.exists()) {
            System.err.println("Erro: Arquivo de dados '" + inputFile + "' não encontrado para compressão.");
            
            return;
        }
        if (dbFile.length() == 0) {
            System.out.println("Aviso: O arquivo de dados '" + inputFile + "' está vazio.");
        }


        try {
           

            Huffman.HuffmanResult result = huffmanCompressor.compress(inputFile, outputFile);
            
            

            System.out.println("\n--- Resultado da Compressão Huffman ---");
            System.out.println(result);
            System.out.println("------------------------------------");
        } catch (FileNotFoundException e) {
            // Esta verificação já foi feita acima.
            System.err.println("Erro.");
        } catch (IOException e) {
            System.err.println("Erro de I/O durante a compressão Huffman: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("Erro: " + e.getMessage());
            e.printStackTrace();
        }
    }

    
    private void decompressDatabaseHuffman() {
        System.out.print("Digite a versão (X) do arquivo Huffman que deseja descompactar (ex: 1 para " + DATABASE_FILENAME + "Huffman1): ");
        String versaoStr = sc.nextLine();
        int versao;
        try {
            versao = Integer.parseInt(versaoStr);
            if (versao <= 0) {
                System.out.println("Versão inválida. Deve ser um número positivo.");
                return;
            }
        } catch (NumberFormatException e) {
            System.out.println("Entrada inválida para a versão. Por favor, insira um número.");
            return;
        }

        String inputFile = DATABASE_FILENAME + "Huffman" + versao;
        String outputFile = DATABASE_FILENAME; // Vai substituir o arquivo original

        System.out.println("\nIniciando descompressão com Huffman...");
        System.out.println("Arquivo de entrada (comprimido): " + inputFile);
        System.out.println("Arquivo de saída (descomprimido): " + outputFile);
        System.out.println("Atenção: Esta operação substituirá o arquivo '" + outputFile + "' existente.");


        File compressedFile = new File(inputFile);
        if (!compressedFile.exists()) {
            System.err.println("Erro: Arquivo comprimido '" + inputFile + "' não encontrado.");
            return;
        }
         if (compressedFile.length() == 0) {
            System.out.println("Aviso: O arquivo comprimido '" + inputFile + "' está vazio.");
        }

        Huffman huffmanDecompressor = new Huffman();
        try {
            

            Huffman.HuffmanResult result = huffmanDecompressor.decompress(inputFile, outputFile);

        

            System.out.println("\n--- Resultado da Descompressão Huffman ---");
            System.out.println(result);
            System.out.println("O arquivo '" + outputFile + "' foi substituído pela versão descomprimida.");
            System.out.println("Pode ser necessário recarregar índices ou reiniciar operações que dependem deste arquivo.");
            System.out.println("---------------------------------------");

        } catch (FileNotFoundException e) {
             
            System.err.println("Erro: Arquivo comprimido '" + inputFile + "' não encontrado no momento da descompressão.");
        } catch (IOException e) {
            System.err.println("Erro de I/O durante a descompressão Huffman: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("Erro: " + e.getMessage());
            e.printStackTrace();
        }
    }


    
    private void createNewGame(Actions actions) throws IOException {
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
    }

    private void readGame(Actions actions) throws IOException {
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
    }

    private void updateGame(Actions actions) throws IOException {
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
    }

    private void deleteGame(Actions actions) throws IOException {
        System.out.print("Digite o ID do jogo para deletar: ");
        int deleteId = Integer.parseInt(sc.nextLine());
        steam deletedGame = actions.deleteGame(deleteId);
        System.out.println(deletedGame != null ? "Jogo deletado: " + deletedGame : "Erro ao deletar jogo.");
    }

    private void executeHashMenu() throws IOException {
        System.out.println("\n=== MENU HASH ===");
        System.out.println("1. Ver game utilizando Hash");
        System.out.println("2. Criar game utilizando Hash");
        System.out.println("3. Deletar game utilizando Hash");
        System.out.print("Escolha uma opção: ");

        int hashOption = Integer.parseInt(sc.nextLine());

        switch (hashOption) {
            case 1:
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
            case 2:
                
                System.out.println("Para criar um jogo (que será indexado pelo Hash), use a opção 2 do menu principal.");
                System.out.println("A indexação por Hash é tipicamente automática ou uma operação de reconstrução.");
                
                break;
            case 3:
                System.out.print("Insira o ID para deletar com Hash: ");
                int delHashId = Integer.parseInt(sc.nextLine());
                steam deletedHash = deleteHashh(delHashId); 
                System.out.println(deletedHash != null ? "Game deletado com Hash." : "Erro ao deletar com Hash.");
                break;
            default:
                System.out.println("Opção inválida no menu Hash.");
                break;
        }
    }

    public void setScanner(Scanner sc) {
        this.sc = sc;
    }

    private void menuCompressaoLZW() {
    try {
        System.out.println("\n=== COMPACTAÇÃO COM LZW ===");

        // Lê os dados do arquivo original
        byte[] dados = java.nio.file.Files.readAllBytes(new java.io.File("TP2/src/steam.db").toPath());

        // Compressão LZW
        System.out.println("\n🔵 Compactando com LZW...");
        long inicio = System.nanoTime();
        byte[] comprimidoLZW = LZW.compress(dados);
        long fim = System.nanoTime();
        System.out.printf("Tempo de compressão: %.2f ms\n", (fim - inicio) / 1e6);
        java.nio.file.Files.write(new java.io.File("steamLZW1.db").toPath(), comprimidoLZW);

        // Descompressão LZW
        System.out.println("🟢 Descompactando com LZW...");
        inicio = System.nanoTime();
        byte[] descomprimidoLZW = LZW.decompress(comprimidoLZW);
        fim = System.nanoTime();
        System.out.printf("Tempo de descompressão: %.2f ms\n", (fim - inicio) / 1e6);
        java.nio.file.Files.write(new java.io.File("steamDescomprimidoLZW.db").toPath(), descomprimidoLZW);

        // Taxa de compressão
        double taxa = 100.0 * (1 - ((double) comprimidoLZW.length / dados.length));
        System.out.printf("📉 Taxa de compressão: %.2f%%\n", taxa);

        // Verificação de integridade
        boolean ok = java.util.Arrays.equals(dados, descomprimidoLZW);
        System.out.println("✅ Verificação de integridade: " + (ok ? "SUCESSO" : "FALHA"));

    } catch (IOException e) {
        System.err.println("Erro na compressão/descompressão com LZW: " + e.getMessage());
    }
}


    public static void main(String[] args) {
        //  try-with-resources para garantir que o Scanner seja fechado 
        try (Scanner scanner = new Scanner(System.in)) {
            Main menu = new Main();
            menu.setScanner(scanner);
            menu.executeMenu();
        } catch (Exception e) {
            System.err.println("Erro fatal na aplicação: " + e.getMessage());
            e.printStackTrace();
        }
        System.out.println("Programa encerrado.");
    }
}
