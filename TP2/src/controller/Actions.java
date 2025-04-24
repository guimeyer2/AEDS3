//Lucas Lopes e Guilherme Meyer

package controller;

import Model.steam;
import java.io.*;
import java.time.LocalDate;
import java.util.ArrayList;

public class Actions {

    private long lastPos; // Última posição escrita no arquivo
    private int maxId;// Maior ID registrado no banco de dados
    RandomAccessFile file; // Arquivo de banco de dados

    public void openFile() throws IOException {
        File dbFile = new File("TP2/src/steam.db");
    
        
       
            file = new RandomAccessFile(dbFile, "rw");// Abre o arquivo para leitura e escrita
            System.out.println("Usando o arquivo original: steam.db");
        
        if (file.length() == 0) {// Se o arquivo estiver vazio, inicializa os metadados
            file.writeInt(0);
            file.writeLong(12);
            lastPos = 12;
        } else {
            file.seek(0);
            maxId = file.readInt();// Lê o ID máximo salvo no arquivo
            lastPos = file.readLong();// Lê a última posição escrita
        }
    

    }
    
    // Método para fechar o arquivo
    public void closeFile() throws IOException {
        try {
            file.close();
        } catch (Exception e) {
            System.err.println("Erro ao fechar arquivo .db: " + e);
        }
    }
    // Método para carregar dados de um CSV para o banco de dados
    public void loadData() {
        try (BufferedReader csv = new BufferedReader(new FileReader("TP2/src/steam2.csv"));
             RandomAccessFile write = new RandomAccessFile("TP2/src/steam.db", "rw")) {

            csv.readLine();
            write.writeInt(0); // Reinicializa o maior ID
            write.writeLong(12);// Reinicializa a posição inicial

            System.out.println("Carregando dados para o arquivo...");

            String str;
            int lastId = 0;

            while ((str = csv.readLine()) != null) {// Lê cada linha do CSV
                if (str.trim().isEmpty()) continue;// Pula linhas vazias

                String[] vet = str.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");// Divide corretamente considerando aspas
                if (vet.length < 6) {
                    continue;
                }

                if (vet[0].isEmpty() || vet[1].isEmpty() || vet[2].isEmpty()) {
                    System.err.println("Linha com dados faltando: " + str);
                    continue;
                }

                int appid;
                try {
                    appid = Integer.parseInt(vet[0]);// Converte o AppID para inteiro
                } catch (NumberFormatException e) {
                    System.err.println("Erro ao converter AppID para número: " + vet[0]);
                    continue;
                }

                LocalDate releaseDate = null;
                try {
                    releaseDate = LocalDate.parse(vet[2]);
                } catch (Exception e) {
                    System.err.println("Erro ao parsear data: " + vet[2]);
                    continue;
                }

                ArrayList<String> platforms = new ArrayList<>();
                if (!vet[4].isEmpty()) {
                    for (String platform : vet[4].split(";")) {
                        platforms.add(platform.trim());
                    }
                }

                steam tmp = new steam(appid, vet[1], releaseDate, platforms, vet[3], vet[5].trim());
                byte[] aux = tmp.toByteArray();

                write.writeByte(0);// Marca como não excluído
                write.writeInt(aux.length);// Escreve o tamanho do registro
                write.write(aux);// Escreve os dados do jogo
                lastId = tmp.getAppid();// Atualiza o último ID
            }

            write.seek(0);
            write.writeInt(lastId);// Atualiza o maior ID no início do arquivo
            write.writeLong(write.getFilePointer()); // Atualiza a última posição escrita
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
            System.err.println("Erro na validação do jogo: " + e);
            return false;
        }
    }
    
    public steam readGame(int searchId) throws IOException {
        long pos = 12; // Define a posição inicial para a leitura no arquivo
        file.seek(pos); // Move o ponteiro do arquivo para essa posição
    
        try {
            while (file.getFilePointer() < file.length()) { // Enquanto não atingir o final do arquivo
                byte tombstone = file.readByte(); // Lê o byte que indica se o registro está ativo ou excluído
                
                // Verifica se há pelo menos 4 bytes disponíveis para leitura do tamanho
                if (file.getFilePointer() + 4 > file.length()) {
                    System.err.println("❌ Erro: Tentando ler um tamanho inválido! Registro pode estar corrompido.");
                    return null;
                }
    
                int tam = file.readInt(); // Lê o tamanho do registro
    
                // Verifica se o tamanho do registro é válido
                if (tam <= 0 || tam > file.length() - file.getFilePointer()) {
                    System.err.println("❌ Erro: tamanho do registro inválido! Algo está corrompido.");
                    return null;
                }
    
                byte[] tempVet = new byte[tam]; // Cria um array de bytes para armazenar os dados do registro
                file.read(tempVet); // Lê os bytes do registro
    
                // Verifica se o registro não foi excluído e se corresponde ao ID buscado
                if (tombstone == 0 && isGameValid(tempVet, searchId)) {
                    steam game = new steam(); // Cria uma instância da classe steam
                    game.fromByteArray(tempVet); // Converte os bytes lidos para um objeto steam
                    return game; // Retorna o jogo encontrado
                }
    
                pos = file.getFilePointer(); // Atualiza a posição do ponteiro corretamente
                file.seek(pos); // Move o ponteiro do arquivo para continuar a leitura
            }
        } catch (Exception e) {
            System.err.println("Erro na função readGame: " + e.getMessage()); // Captura e exibe possíveis erros
        }
    
        return null; // Retorna null caso o jogo não seja encontrado
    }
    
    

    
    
    
    public boolean updateGame(int id, steam newGame) {
        long pos = 12;
    
        try {
            file.seek(pos);
    
            while (file.getFilePointer() < file.length()) {
                long regPos = file.getFilePointer(); // Guarda a posição do registro atual
                byte tombstone = file.readByte();// Lê o marcador de exclusão
                int tam = file.readInt(); // Lê o tamanho do registro
                byte[] arr = new byte[tam];
                file.read(arr);// Lê os dados do jogo
    
                if (tombstone == 0 && isGameValid(arr, id)) {// Verifica se o jogo é válido e corresponde ao ID
                    byte[] newGameBytes = newGame.toByteArray();
    
                    if (newGameBytes.length <= tam) {
                        file.seek(regPos + 5);// Pula o marcador e o tamanho
                        file.write(newGameBytes);// Escreve os novos dados
                        return true;
                    } else {// Se o novo jogo for maior que o espaço disponível
                        file.seek(regPos);// Marca o registro antigo como deletado
                        file.writeByte(1);
                        return createGame(newGame); // Cria um novo registro no final do arquivo
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
            if (readGame(tmp.getAppid()) != null) {
                System.err.println("Erro: Já existe um jogo com o AppID " + tmp.getAppid());
                return false;
            }
    
            byte[] aux = tmp.toByteArray();
    
            // Ir para o final do arquivo
            file.seek(file.length());
    
            
    
            file.writeByte(0);// Marca o registro como válido
            file.writeInt(aux.length);// Escreve o tamanho do registro
            file.write(aux);// Escreve os dados do jogo
    
            lastPos = file.getFilePointer(); // Atualiza a posição final corretamente
    
            if (tmp.getAppid() > maxId) {
                maxId = tmp.getAppid();
            }
    
            // Atualiza os metadados corretamente
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
        steam aux = new steam();
        long pos = 12;

        try {
            file.seek(pos);

            while (file.getFilePointer() < file.length()) {
                long regPos = file.getFilePointer();
                byte tombstone = file.readByte();//Lapide que marca se esta deletado
                int tam = file.readInt();
                byte[] temp = new byte[tam];
                file.read(temp);

                if (tombstone == 0 && isGameValid(temp, id)) {
                    file.seek(regPos);
                    file.writeByte(1); // Marca o jogo como deletado
                    aux.fromByteArray(temp);
                    return aux;
                }

                pos += 5 + tam;//reajusta o tamanho
            }
        } catch (Exception e) {
            System.err.println("Erro na função deleteGame: " + e);
        }
        return null;
    }
}