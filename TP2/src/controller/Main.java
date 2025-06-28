package controller;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Scanner;
import Algoritmos.BoyerMoore;
import Algoritmos.CriptografiaSimples;
import Algoritmos.Huffman;
import Algoritmos.KMPMatcher;
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
        System.out.println();
        System.out.println("--- Ferramentas ---");
        System.out.println("9. Comprimir Base de Dados (Huffman + LZW)");
        System.out.println("10. Descomprimir Base de Dados");
        System.out.println("11. Buscar Padrão (KMP)");
        System.out.println("12. Buscar Padrão (Boyer-Moore)");
       System.out.println("13. Visualizar Criptografia Simples");
        System.out.println("14. Visualizar Criptografia Moderna (DES)");
        System.out.println("15. Sair");


        System.out.print("Escolha uma opção (1-13): ");

        while (true) {
            try {
                String line = sc.nextLine();
                int input = Integer.parseInt(line);
                if (input >= 1 && input <= 16) {
                    selected = input;
                    break;
                } else {
                    System.out.print("Opção inválida. Por favor, escolha entre 1 e 13: ");
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
                case 1: actions.loadData(); break;
                case 2: createNewGame(actions); break;
                case 3: readGame(actions); break;
                case 4: updateGame(actions); break;
                case 5: deleteGame(actions); break;
                case 6: executeHashMenu(); break;
                case 7:
                    System.out.println("Executando Lista Invertida...");
                    actions.menuListaInvertida(sc);
                    break;
                case 8:
                    System.out.println("Executando Árvore B...");
                    actions.menuArvoreB(sc);
                    break;
                case 9: 
                    compressDatabase(); // novo método unificado
                    break;
                case 10: 
                    decompressDatabase(); // novo método unificado
                    break;
                case 11:
                    searchWithKMP(actions);
                    break; 
                case 12:
                    buscaComBoyerMoore(actions);
                    break;
                 case 13:
    visualizarCriptografiaDB(actions); // simples
    break;
case 14:
    visualizarCriptografiaDES(actions); // nova opção DES
    break;
case 15:
    System.out.println("Obrigado por usar nosso Banco de Dados!");
    this.closeFile();
    return false;}
        } catch (Exception e) {
            System.err.println("Erro na função executeOption: " + e.getMessage());
            e.printStackTrace(); 
        }
        return true; 
    }
    
    

    /**
     * aqui ocorre a compressão unificada da base de dados utilizando Huffman e LZW.
     */
    private void compressDatabase() throws IOException {
        System.out.println("\n--- INICIANDO COMPRESSÃO UNIFICADA ---");
        
        Path originalPath = Paths.get(DATABASE_FILENAME);
        if (!Files.exists(originalPath)) {
            System.err.println("Erro: Arquivo de banco de dados '" + DATABASE_FILENAME + "' não encontrado.");
            return;
        }

        byte[] originalData = Files.readAllBytes(originalPath);
        if (originalData.length == 0) {
            System.out.println("Aviso: O arquivo de dados está vazio. Nenhuma compressão será realizada.");
            return;
        }

        int version = proximaVersaoCompressao();
        String basePath = DATABASE_FILENAME.replace(".db", "");

        // --- compressão com Huffman ---
        System.out.println("\n1) Executando compressão com Huffman...");
        Huffman huffmanCompressor = new Huffman();
        String huffmanOutputFile = basePath + "-Huffman-" + version + ".db";
        Huffman.HuffmanResult huffmanResult = huffmanCompressor.compress(DATABASE_FILENAME, huffmanOutputFile);
        System.out.println("Arquivo gerado: " + huffmanOutputFile);

        // --- compressão com LZW ---
        System.out.println("\n2) Executando compressão com LZW...");
        long lzwStartTime = System.nanoTime();
        byte[] compressedLZW = LZW.compress(originalData);
        long lzwEndTime = System.nanoTime();
        double lzwTimeMillis = (lzwEndTime - lzwStartTime) / 1e6;

        String lzwOutputFile = basePath + "-LZW-" + version + ".db";
        Files.write(Paths.get(lzwOutputFile), compressedLZW);
        double lzwRatio = 100.0 * (1 - ((double) compressedLZW.length / originalData.length));
        System.out.println("Arquivo gerado: " + lzwOutputFile);

        // --- Relatório Comparativo ---
        System.out.println("\n--- RELATÓRIO DE COMPRESSÃO (Versão " + version + ") ---");
        System.out.println("-------------------------------------------------");
        System.out.printf("| Algoritmo | Tempo (ms) | Taxa de Compressão |\n");
        System.out.println("-------------------------------------------------");
        System.out.printf("| Huffman   | %-10d | %17.2f%% |\n", huffmanResult.tempoPercorridoMillis, huffmanResult.porcentagemCompressao);
        System.out.printf("| LZW       | %-10.2f | %17.2f%% |\n", lzwTimeMillis, lzwRatio);
        System.out.println("-------------------------------------------------");

        if (huffmanResult.porcentagemCompressao > lzwRatio) {
            System.out.println("Conclusão: Huffman obteve a melhor taxa de compressão.");
        } else if (lzwRatio > huffmanResult.porcentagemCompressao) {
            System.out.println("Conclusão: LZW obteve a melhor taxa de compressão.");
        } else {
            System.out.println("Conclusão: Ambos os algoritmos tiveram taxas de compressão similares.");
        }
        System.out.println("-------------------------------------------------");
    }
    
    /**
     * usuário escolhe a versão e o algoritmo para descomprimir o arquivo principal.
     */
    private void decompressDatabase() throws IOException {
        System.out.println("\n--- INICIANDO DESCOMPRESSÃO ---");
        System.out.print("Digite a versão (X) dos arquivos que deseja descompactar: ");
        int version;
        try {
            version = Integer.parseInt(sc.nextLine());
        } catch (NumberFormatException e) {
            System.err.println("Versão inválida. Por favor, digite um número.");
            return;
        }

        System.out.println("Qual algoritmo deseja usar para restaurar o arquivo principal?");
        System.out.println("1. Huffman");
        System.out.println("2. LZW");
        System.out.print("Escolha uma opção: ");
        
        int choice;
        try {
            choice = Integer.parseInt(sc.nextLine());
        } catch (NumberFormatException e) {
            System.err.println("Opção inválida.");
            return;
        }
        
        String basePath = DATABASE_FILENAME.replace(".db", "");
        String inputFile;
        
        switch (choice) {
            case 1:
                // --- Descompressão com Huffman ---
                inputFile = basePath + "-Huffman-" + version + ".db";
                if (!Files.exists(Paths.get(inputFile))) {
                    System.err.println("Erro: Arquivo '" + inputFile + "' não encontrado.");
                    return;
                }
                System.out.println("Restaurando '" + DATABASE_FILENAME + "' a partir de '" + inputFile + "'...");
                Huffman huffmanDecompressor = new Huffman();
                huffmanDecompressor.decompress(inputFile, DATABASE_FILENAME);
                System.out.println("Sucesso! Arquivo principal restaurado com a versão " + version + " de Huffman.");
                break;
                
            case 2:
                // --- Descompressão com LZW ---
                inputFile = basePath + "-LZW-" + version + ".db";
                 if (!Files.exists(Paths.get(inputFile))) {
                    System.err.println("Erro: Arquivo '" + inputFile + "' não encontrado.");
                    return;
                }
                System.out.println("Restaurando '" + DATABASE_FILENAME + "' a partir de '" + inputFile + "'...");
                byte[] compressedData = Files.readAllBytes(Paths.get(inputFile));
                byte[] decompressedData = LZW.decompress(compressedData);
                Files.write(Paths.get(DATABASE_FILENAME), decompressedData);
                System.out.println("Sucesso! Arquivo principal restaurado com a versão " + version + " de LZW.");
                break;
                
            default:
                System.err.println("Opção de algoritmo inválida.");
                break;
        }
    }
    
    /**
     * busca a próxima versão de compressão
     */
    private int proximaVersaoCompressao() {
        int version = 1;
        String basePath = DATABASE_FILENAME.replace(".db", "");
        while (true) {
            File huffmanFile = new File(basePath + "-Huffman-" + version + ".db");
            File lzwFile = new File(basePath + "-LZW-" + version + ".db");
            if (!huffmanFile.exists() && !lzwFile.exists()) {
                return version;
            }
            version++;
        }
    }
    private void buscaComBoyerMoore(Actions actions) throws IOException {
        System.out.print("Digite o padrão a ser buscado (Boyer-Moore): ");
        String pattern = sc.nextLine();

        if (pattern.isEmpty()) {
            System.out.println("O padrão de busca não pode ser vazio.");
            return;
        }

      
        
        byte[] data = actions.readAllBytesFromDb(); 
        String text = new String(data);

        BoyerMoore bm = new BoyerMoore();
        long start = System.nanoTime();
        int count = bm.search(text, pattern); 
        long end = System.nanoTime();

        System.out.println("\n--- Resultados da Busca (Boyer-Moore) ---");
        System.out.printf("O padrão \"%s\" apareceu %d vez(es).\n", pattern, count);
        System.out.printf("Tempo de busca: %.2f ms\n", (end - start) / 1e6);
    }
       
private void createNewGame(Actions actions) throws IOException {
    System.out.print("Digite o ID do jogo: ");
    int id = Integer.parseInt(sc.nextLine());

    System.out.print("Digite o nome do jogo: ");
    String name = sc.nextLine();

    // Submenu de escolha de criptografia
    System.out.println("Escolha o tipo de criptografia para o nome:");
    System.out.println("1. Simples (Substituição)");
    System.out.println("2. Moderna (DES)");
    System.out.print("Opção: ");
    int tipoCript = Integer.parseInt(sc.nextLine());

    String nameCriptografado;
    if (tipoCript == 2) {
        nameCriptografado = Algoritmos.CriptografiaModerna.criptografarDES(name);
    } else {
        nameCriptografado = Algoritmos.CriptografiaSimples.substituir(name, 3);
    }

    System.out.print("Digite a data de lançamento (AAAA-MM-DD): ");
    LocalDate date = LocalDate.parse(sc.nextLine());

    System.out.print("Digite as plataformas (separadas por vírgula): ");
    ArrayList<String> platforms = new ArrayList<>();
    for (String p : sc.nextLine().split(",")) platforms.add(p.trim());

    System.out.print("Digite o gênero do jogo: ");
    String genre = sc.nextLine();

    String launchBefore2010 = date.getYear() < 2010 ? "SIM" : "NAO";

    steam newGame = new steam(id, nameCriptografado, date, platforms, genre, launchBefore2010);
    System.out.println(actions.createGame(newGame) ? "Jogo criado com sucesso!" : "Erro ao criar jogo.");
}

private void updateGame(Actions actions) throws IOException {
    System.out.print("Digite o ID do jogo para atualizar: ");
    int updateId = Integer.parseInt(sc.nextLine());

    System.out.print("Digite o novo nome: ");
    String newName = sc.nextLine();

    // Submenu de escolha de criptografia
    System.out.println("Escolha o tipo de criptografia para o nome:");
    System.out.println("1. Simples (Substituição)");
    System.out.println("2. Moderna (DES)");
    System.out.print("Opção: ");
    int tipoCript = Integer.parseInt(sc.nextLine());

    String nameCriptografado;
    if (tipoCript == 2) {
        nameCriptografado = Algoritmos.CriptografiaModerna.criptografarDES(newName);
    } else {
        nameCriptografado = Algoritmos.CriptografiaSimples.substituir(newName, 3);
    }

    System.out.print("Digite a nova data de lançamento (AAAA-MM-DD): ");
    LocalDate newDate = LocalDate.parse(sc.nextLine());

    System.out.print("Digite as novas plataformas: ");
    ArrayList<String> newPlatforms = new ArrayList<>();
    for (String p : sc.nextLine().split(",")) newPlatforms.add(p.trim());

    System.out.print("Digite o novo gênero: ");
    String newGenre = sc.nextLine();

    String newLaunchBefore2010 = newDate.getYear() < 2010 ? "SIM" : "NAO";

    steam updatedGame = new steam(updateId, nameCriptografado, newDate, newPlatforms, newGenre, newLaunchBefore2010);
    System.out.println(actions.updateGame(updateId, updatedGame) ? "Atualizado com sucesso!" : "Erro ao atualizar.");
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

    private void deleteGame(Actions actions) throws IOException {
        System.out.print("Digite o ID do jogo para deletar: ");
        int deleteId = Integer.parseInt(sc.nextLine());
        steam deletedGame = actions.deleteGame(deleteId);
        System.out.println(deletedGame != null ? "Jogo deletado: " + deletedGame : "Erro ao deletar jogo.");
    }

   private void visualizarCriptografiaDB(Actions actions) throws IOException {
    System.out.print("Digite o ID do jogo que deseja inspecionar: ");
    int id = Integer.parseInt(sc.nextLine());

    try (RandomAccessFile raf = new RandomAccessFile("TP2/src/steam.db", "r")) {
        raf.seek(12); // pula cabeçalho

        while (raf.getFilePointer() < raf.length()) {
            long regInicio = raf.getFilePointer();
            byte tombstone = raf.readByte();
            int tam = raf.readInt();
            if (tam <= 0 || tam > raf.length() - raf.getFilePointer()) break;

            byte[] dados = new byte[tam];
            raf.readFully(dados);

            try (DataInputStream dis = new DataInputStream(new ByteArrayInputStream(dados))) {
                int appid = dis.readInt();
                String nomeCriptografado = dis.readUTF();

                if (appid == id && tombstone == 0) {
                    String nomeDescriptografado = Algoritmos.CriptografiaSimples.reverterSubstituicao(nomeCriptografado, 3);

                    System.out.println("\n--- REGISTRO ENCONTRADO ---");
                    System.out.println("AppID: " + appid);
                    System.out.println("Nome criptografado: " + nomeCriptografado);
                    System.out.println("Nome descriptografado: " + nomeDescriptografado);
                    System.out.println("----------------------------");
                    return;
                }
            }
        }

        System.out.println("Jogo com ID " + id + " não encontrado.");
    } catch (Exception e) {
        System.err.println("Erro ao inspecionar criptografia: " + e.getMessage());
    }
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

   


    private void visualizarCriptografiaDES(Actions actions) throws IOException {
    System.out.print("Digite o ID do jogo (DES): ");
    int id = Integer.parseInt(sc.nextLine());

    try (RandomAccessFile raf = new RandomAccessFile("TP2/src/steam.db", "r")) {
        raf.seek(12); // pula cabeçalho

        while (raf.getFilePointer() < raf.length()) {
            long inicio = raf.getFilePointer();
            byte tombstone = raf.readByte();
            int tam = raf.readInt();
            if (tombstone == 1 || tam <= 0 || tam > raf.length() - raf.getFilePointer()) {
                raf.seek(inicio + 5 + Math.max(tam, 0));
                continue;
            }

            byte[] dados = new byte[tam];
            raf.readFully(dados);

            try (DataInputStream dis = new DataInputStream(new ByteArrayInputStream(dados))) {
                int appid = dis.readInt();
                String nomeCriptografado = dis.readUTF();

                if (appid == id) {
                    String nomeDescriptografado = Algoritmos.CriptografiaModerna.descriptografarDES(nomeCriptografado);
                    System.out.println("\n--- REGISTRO DES ENCONTRADO ---");
                    System.out.println("AppID: " + appid);
                    System.out.println("Nome criptografado (DES): " + nomeCriptografado);
                    System.out.println("Nome descriptografado (DES): " + nomeDescriptografado);
                    System.out.println("--------------------------------");
                    return;
                }
            } catch (Exception e) {
                System.out.println("Erro ao processar registro: " + e.getMessage());
            }
        }

        System.out.println("Jogo com ID " + id + " não encontrado.");
    }
}

private void searchWithKMP(Actions actions) throws IOException {
    System.out.print("Digite o padrão a ser buscado: ");
    String pattern = sc.nextLine();

    // Lê todos os dados do arquivo .db
    byte[] data = actions.readAllBytesFromDb(); 
    String text = new String(data); // converte os bytes para string (válido se não for binário puro)

    KMPMatcher kmp = new KMPMatcher();
    long start = System.nanoTime();
    int count = kmp.search(text, pattern);
    long end = System.nanoTime();

    System.out.printf("O padrão \"%s\" apareceu %d vez(es).\n", pattern, count);
    System.out.printf("Tempo de busca: %.2f ms\n", (end - start) / 1e6);
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
