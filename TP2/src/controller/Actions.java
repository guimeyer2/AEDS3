package controller;

import Model.steam;
import java.io.*;
import java.time.LocalDate;
import java.util.*;
import Algoritmos.InvertedList;
import Algoritmos.BTree;

public class Actions {

    private long lastPos;
    private int maxId;
    RandomAccessFile file;

    public void openFile() throws IOException {
        File dbFile = new File("TP2/src/steam.db");
        file = new RandomAccessFile(dbFile, "rw");
        System.out.println("Usando o arquivo original: steam.db");

        if (file.length() == 0) {
            file.writeInt(0);
            file.writeLong(12);
            lastPos = 12;
        } else {
            file.seek(0);
            maxId = file.readInt();
            lastPos = file.readLong();
        }
    }

    public void closeFile() throws IOException {
        if (file != null) file.close();
    }

    public void loadData() {
        try (BufferedReader csv = new BufferedReader(new FileReader("TP2/src/steam2.csv"));
             RandomAccessFile write = new RandomAccessFile("TP2/src/steam.db", "rw")) {

            csv.readLine();
            write.writeInt(0);
            write.writeLong(12);

            System.out.println("Carregando dados para o arquivo...");
            String str;
            int lastId = 0;

            while ((str = csv.readLine()) != null) {
                if (str.trim().isEmpty()) continue;

                String[] vet = str.split(",(?=(?:[^\"]\"[^\"]\")[^\"]$)");
                if (vet.length < 6 || vet[0].isEmpty() || vet[1].isEmpty() || vet[2].isEmpty()) continue;

                int appid;
                try {
                    appid = Integer.parseInt(vet[0]);
                } catch (NumberFormatException e) { continue; }

                LocalDate releaseDate;
                try {
                    releaseDate = LocalDate.parse(vet[2]);
                } catch (Exception e) { continue; }

                ArrayList<String> platforms = new ArrayList<>();
                if (!vet[4].isEmpty()) {
                    for (String platform : vet[4].split(";")) platforms.add(platform.trim());
                }

                steam tmp = new steam(appid, vet[1], releaseDate, platforms, vet[3], vet[5].trim());
                byte[] aux = tmp.toByteArray();

                write.writeByte(0);
                write.writeInt(aux.length);
                write.write(aux);
                lastId = tmp.getAppid();
            }

            write.seek(0);
            write.writeInt(lastId);
            write.writeLong(write.getFilePointer());
            System.out.println("Dados carregados com sucesso!");

        } catch (Exception e) {
            System.err.println("Erro ao carregar dados: " + e);
        }
    }

    public boolean isGameValid(byte[] arr, int id) {
        try {
            steam temp = new steam();
            temp.fromByteArray(arr);
            return temp.getAppid() == id;
        } catch (Exception e) {
            return false;
        }
    }

    public steam readGame(int searchId) throws IOException {
        long pos = 12;
        file.seek(pos);
        try {
            while (file.getFilePointer() < file.length()) {
                byte tombstone = file.readByte();
                int tam = file.readInt();
                if (tam <= 0 || tam > file.length() - file.getFilePointer()) return null;
                byte[] tempVet = new byte[tam];
                file.read(tempVet);
                if (tombstone == 0 && isGameValid(tempVet, searchId)) {
                    steam game = new steam();
                    game.fromByteArray(tempVet);
                    return game;
                }
                pos = file.getFilePointer();
                file.seek(pos);
            }
        } catch (Exception e) {
            System.err.println("Erro na função readGame: " + e.getMessage());
        }
        return null;
    }

    public boolean updateGame(int id, steam newGame) {
        long pos = 12;
        try {
            file.seek(pos);
            while (file.getFilePointer() < file.length()) {
                long regPos = file.getFilePointer();
                byte tombstone = file.readByte();
                int tam = file.readInt();
                byte[] arr = new byte[tam];
                file.read(arr);

                if (tombstone == 0 && isGameValid(arr, id)) {
                    byte[] newGameBytes = newGame.toByteArray();
                    if (newGameBytes.length <= tam) {
                        file.seek(regPos + 5);
                        file.write(newGameBytes);
                        return true;
                    } else {
                        file.seek(regPos);
                        file.writeByte(1);
                        return createGame(newGame);
                    }
                }
                pos += 5 + tam;
            }
        } catch (Exception e) {
            System.err.println("Erro na função updateGame: " + e);
        }
        return false;
    }

    public boolean createGame(steam tmp) {
        try {
            if (readGame(tmp.getAppid()) != null) return false;
            byte[] aux = tmp.toByteArray();
            file.seek(file.length());
            file.writeByte(0);
            file.writeInt(aux.length);
            file.write(aux);
            lastPos = file.getFilePointer();
            if (tmp.getAppid() > maxId) maxId = tmp.getAppid();
            file.seek(0);
            file.writeInt(maxId);
            file.writeLong(lastPos);
            return true;
        } catch (IOException e) {
            System.err.println("Erro na função createGame: " + e);
            return false;
        }
    }

    public steam deleteGame(int id) {
        long pos = 12;
        try {
            file.seek(pos);
            while (file.getFilePointer() < file.length()) {
                long regPos = file.getFilePointer();
                byte tombstone = file.readByte();
                int tam = file.readInt();
                byte[] temp = new byte[tam];
                file.read(temp);
                if (tombstone == 0 && isGameValid(temp, id)) {
                    file.seek(regPos);
                    file.writeByte(1);
                    
                    steam aux = new steam();
                    aux.fromByteArray(temp);
                    return aux;
                }
                pos += 5 + tam;
            }
        } catch (Exception e) {
            System.err.println("Erro na função deleteGame: " + e);
        }
        return null;
    }
    public void menuArvoreB(Scanner scanner) throws IOException {
        BTree bTree = new BTree();
        
        // Verifica se o índice já existe, se não, oferece para criá-lo
        File indexFile = new File("TP2/src/btree_index.db");
        if (!indexFile.exists() || indexFile.length() == 0) {
            System.out.println("Índice da Árvore B não encontrado. Deseja criar? (S/N)");
            String resposta = scanner.nextLine();
            if (resposta.equalsIgnoreCase("S")) {
                System.out.println("Criando índice da Árvore B...");
                bTree = new BTree(5); 
                bTree.carregarDados("TP2/src/steam.db");
                System.out.println("Índice criado com sucesso!");
            }
        }
        
        while (true) {
            System.out.println("\n=== MENU ÁRVORE B ===");
            System.out.println("1. Buscar jogo por ID");
            System.out.println("2. Inserir novo jogo no índice");
            System.out.println("3. Atualizar endereço de um jogo");
            System.out.println("4. Remover jogo do índice");
            System.out.println("5. Recriar índice");
            System.out.println("6. Voltar ao Menu Principal");
            System.out.print("Escolha uma opção: ");
            
            int choice = scanner.nextInt();
            scanner.nextLine(); // Limpa o buffer
            
            switch (choice) {
                case 1:
                    buscarJogoPorIdUI(bTree, scanner);
                    break;
                    
                case 2:
                    inserirJogoUI(bTree, scanner);
                    break;
                    
                case 3:
                    atualizarEnderecoUI(bTree, scanner);
                    break;
                    
                case 4:
                    removerJogoUI(bTree, scanner);
                    break;
                    
                case 5:
                    recriarIndiceUI(bTree, scanner);
                    break;
                    
                case 6:
                    bTree.close();
                    return;
                    
                default:
                    System.out.println("Opção inválida.");
            }
        }
    }
    
    private void buscarJogoPorIdUI(BTree bTree, Scanner scanner) {
        System.out.print("Digite o ID do jogo para buscar: ");
        int id = scanner.nextInt();
        scanner.nextLine(); // Limpa o buffer
        
        long endereco = bTree.procurar(id);
        if (endereco == -1) {
            System.out.println("Jogo com ID " + id + " não encontrado no índice.");
        } else {
            try {
                // Posiciona o arquivo no endereço encontrado na árvore
                file.seek(endereco);
                byte tombstone = file.readByte();
                int tam = file.readInt();
                byte[] dados = new byte[tam];
                file.read(dados);
                
                // Verifica se o registro não está excluído
                if (tombstone == 0) {
                    steam game = new steam();
                    game.fromByteArray(dados);
                    System.out.println("\n=== JOGO ENCONTRADO ===");
                    System.out.println(game);
                    System.out.println("Endereço no arquivo: " + endereco);
                } else {
                    System.out.println("Registro encontrado no índice, mas está marcado como excluído.");
                }
            } catch (IOException e) {
                System.err.println("Erro ao ler o jogo do arquivo: " + e.getMessage());
            }
        }
    }
    
    private void inserirJogoUI(BTree bTree, Scanner scanner) throws IOException {
        System.out.println("\nInsira os dados do novo jogo:");
        System.out.print("ID (appid): ");
        int id = scanner.nextInt();
        scanner.nextLine(); // Limpa o buffer
        
        // Verifica se o ID já existe na árvore
        if (bTree.procurar(id) != -1) {
            System.out.println("Jogo com ID " + id + " já existe no índice.");
            return;
        }
        
        System.out.print("Nome: ");
        String nome = scanner.nextLine();
        
        System.out.print("Data de lançamento (YYYY-MM-DD): ");
        LocalDate releaseDate;
        try {
            releaseDate = LocalDate.parse(scanner.nextLine());
        } catch (Exception e) {
            System.out.println("Formato de data inválido. Operação cancelada.");
            return;
        }
        
        System.out.print("Plataformas (separadas por ;): ");
        String plataformasInput = scanner.nextLine();
        ArrayList<String> plataformas = new ArrayList<>();
        if (!plataformasInput.isEmpty()) {
            for (String plataforma : plataformasInput.split(";")) {
                plataformas.add(plataforma.trim());
            }
        }
        
        System.out.print("Gênero: ");
        String genero = scanner.nextLine();
        
        System.out.print("Desenvolvedor: ");
        String dev = scanner.nextLine();
        
        // Criar o objeto steam e inserir no arquivo de dados
        steam novoJogo = new steam(id, nome, releaseDate, plataformas, genero, dev);
        if (createGame(novoJogo)) {
            // Obter a posição onde o jogo foi inserido
            long enderecoJogo = file.length() - novoJogo.toByteArray().length - 5;
            // Inserir na árvore B
            bTree.inserir(id, enderecoJogo);
            System.out.println("Jogo cadastrado com sucesso e indexado na árvore B!");
        } else {
            System.out.println("Erro ao cadastrar o jogo. Operação cancelada.");
        }
    }
    
    private void atualizarEnderecoUI(BTree bTree, Scanner scanner) {
        System.out.print("Digite o ID do jogo para atualizar endereço: ");
        int id = scanner.nextInt();
        scanner.nextLine(); // Limpa o buffer
        
        // Busca o jogo no arquivo diretamente (sem usar a árvore)
        long endereco = -1;
        try {
            long pos = 12; // Pula o cabeçalho
            file.seek(pos);
            while (file.getFilePointer() < file.length()) {
                long posRegistro = file.getFilePointer();
                byte tombstone = file.readByte();
                int tam = file.readInt();
                byte[] dados = new byte[tam];
                file.read(dados);
                
                if (tombstone == 0 && isGameValid(dados, id)) {
                    endereco = posRegistro;
                    break;
                }
            }
        } catch (IOException e) {
            System.err.println("Erro ao buscar jogo: " + e.getMessage());
        }
        
        if (endereco == -1) {
            System.out.println("Jogo com ID " + id + " não encontrado no arquivo de dados.");
            return;
        }
        
        // Atualiza o endereço na árvore B
        if (bTree.atualizar(id, endereco)) {
            System.out.println("Endereço do jogo atualizado com sucesso no índice!");
        } else {
            System.out.println("Jogo não encontrado no índice ou erro ao atualizar.");
        }
    }
    
    private void removerJogoUI(BTree bTree, Scanner scanner) {
        System.out.print("Digite o ID do jogo para remover do índice: ");
        int id = scanner.nextInt();
        scanner.nextLine(); // Limpa o buffer
        
        // Primeiro verifica se o jogo existe no índice
        if (bTree.procurar(id) == -1) {
            System.out.println("Jogo com ID " + id + " não encontrado no índice.");
            return;
        }
        
        // Remove o jogo do índice
        if (bTree.deletar(id)) {
            System.out.println("Jogo removido do índice com sucesso!");
            
            // Pergunta se deseja excluir o registro do arquivo de dados também
            System.out.println("Deseja excluir o registro do arquivo de dados também? (S/N)");
            String resposta = scanner.nextLine();
            if (resposta.equalsIgnoreCase("S")) {
                steam jogoExcluido = deleteGame(id);
                if (jogoExcluido != null) {
                    System.out.println("Registro excluído do arquivo de dados: " + jogoExcluido.getName());
                } else {
                    System.out.println("Erro ao excluir o registro do arquivo de dados.");
                }
            }
        } else {
            System.out.println("Erro ao remover o jogo do índice.");
        }
    }
    
    private void recriarIndiceUI(BTree bTree, Scanner scanner) {
        System.out.println("Tem certeza que deseja recriar o índice da Árvore B? (S/N)");
        System.out.println("Isso irá apagar o índice atual e criar um novo com base no arquivo de dados.");
        String resposta = scanner.nextLine();
        
        if (resposta.equalsIgnoreCase("S")) {
            bTree.close(); // Fecha a árvore atual
            
            // Cria uma nova árvore com ordem 5
            BTree novaArvore = new BTree(5);
            
            System.out.println("Recriando índice da Árvore B...");
            novaArvore.carregarDados("TP2/src/steam.db");
            System.out.println("Índice recriado com sucesso!");
            
            // Substitui a árvore antiga pela nova
            bTree = novaArvore;
        }
    }


    
    public void menuListaInvertida(Scanner scanner) {
        InvertedList invertedList = new InvertedList();
        
        // Verifica e constrói as listas se necessário
        if (new File("src/temp/invertedGenero.db").length() == 0 || 
            new File("src/temp/invertedPlataforma.db").length() == 0) {
            System.out.println("Construindo listas invertidas pela primeira vez...");
            invertedList.construirListas();
        }
    
        while (true) {
            System.out.println("\n=== MENU LISTA INVERTIDA ===");
            System.out.println("1. Buscar por Gênero");
            System.out.println("2. Buscar por Plataforma");
            System.out.println("3. Buscar por Gênero e Plataforma");
            System.out.println("4. Listar Tudo (Debug)");
            System.out.println("5. Voltar ao Menu Principal");
            System.out.print("Escolha uma opção: ");
            
            int choice = scanner.nextInt();
            scanner.nextLine(); // Limpa o buffer
            
            switch (choice) {
                case 1:
                    buscarPorGeneroUI(invertedList, scanner);
                    break;
                    
                case 2:
                    buscarPorPlataformaUI(invertedList, scanner);
                    break;
                    
                case 3:
                    buscarPorGeneroEPlataformaUI(invertedList, scanner);
                    break;
                    
                case 4:
                    invertedList.listarTudo();
                    break;
                    
                case 5:
                    invertedList.close();
                    return;
                    
                default:
                    System.out.println("Opção inválida.");
            }
        }
    }
    
    private void buscarPorGeneroUI(InvertedList invertedList, Scanner scanner) {
        System.out.print("Digite o gênero para buscar: ");
        String genero = scanner.nextLine();
        List<Integer> ids = invertedList.buscarPorGenero(genero);
        if (ids.isEmpty()) {
            System.out.println("Nenhum jogo encontrado para o gênero: " + genero);
        } else {
            System.out.println("\n=== JOGOS ENCONTRADOS ===");
            for (int id : ids) {
                try {
                    steam game = readGame(id);
                    if (game != null) {
                        System.out.println(game);
                        System.out.println("----------------------------");
                    }
                } catch (IOException e) {
                    System.err.println("Erro ao ler jogo com ID " + id + ": " + e.getMessage());
                }
            }
        }
    }

    public byte[] readAllBytesFromDb() throws IOException {
    File file = new File("TP2/src/steam.db");
    byte[] data = new byte[(int) file.length()];
    try (FileInputStream fis = new FileInputStream(file)) {
        fis.read(data);
    }
    return data;
}

    
    private void buscarPorPlataformaUI(InvertedList invertedList, Scanner scanner) {
        System.out.print("Digite a plataforma para buscar: ");
        String plataforma = scanner.nextLine();
        List<Integer> ids = invertedList.buscarPorPlataforma(plataforma);
        if (ids.isEmpty()) {
            System.out.println("Nenhum jogo encontrado para a plataforma: " + plataforma);
        } else {
            System.out.println("\n=== JOGOS ENCONTRADOS ===");
            for (int id : ids) {
                try {
                    steam game = readGame(id);
                    if (game != null) {
                        System.out.println(game);
                        System.out.println("----------------------------");
                    }
                } catch (IOException e) {
                    System.err.println("Erro ao ler jogo com ID " + id + ": " + e.getMessage());
                }
            }
        }
    }

    public byte[] readRawGameData(int searchId) {
    long pos = 12;
    try {
        file.seek(pos);
        while (file.getFilePointer() < file.length()) {
            long regPos = file.getFilePointer();
            byte tombstone = file.readByte();
            int tam = file.readInt();

            if (tam <= 0 || tam > file.length() - file.getFilePointer()) return null;

            byte[] tempVet = new byte[tam];
            file.read(tempVet);

            if (tombstone == 0 && isGameValid(tempVet, searchId)) {
                return tempVet; // 🔁 Retorna os dados crus
            }

            pos = file.getFilePointer();
            file.seek(pos);
        }
    } catch (Exception e) {
        System.err.println("Erro na função readRawGameData: " + e.getMessage());
    }
    return null;
}

    
    private void buscarPorGeneroEPlataformaUI(InvertedList invertedList, Scanner scanner) {
        System.out.print("Digite o gênero: ");
        String genero = scanner.nextLine();
        System.out.print("Digite a plataforma: ");
        String plataforma = scanner.nextLine();
        List<Integer> ids = invertedList.buscarPorGeneroEPlataforma(genero, plataforma);
        if (ids.isEmpty()) {
            System.out.println("Nenhum jogo encontrado para gênero " + genero + " e plataforma " + plataforma);
        } else {
            System.out.println("\n=== JOGOS ENCONTRADOS ===");
            for (int id : ids) {
                try {
                    steam game = readGame(id);
                    if (game != null) {
                        System.out.println(game);
                        System.out.println("----------------------------");
                    }
                } catch (IOException e) {
                    System.err.println("Erro ao ler jogo com ID " + id + ": " + e.getMessage());
                }
            }
        }
    }}