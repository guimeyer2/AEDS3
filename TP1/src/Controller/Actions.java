package controller;

import java.io.*;

public class Actions {

  private long lastPos;
  private int maxId; // Último jogo do arquivo, antes do fim
  private int gamesCount;
  static final int inicialYear = 1900; // Ano inicial para a conversão da data
  RandomAccessFile file;

  // Abertura do arquivo .db
  public void openFile() throws IOException {
    file = new RandomAccessFile("./TP01/out/games.db", "rw");
    lastPos = file.readLong(); // Guarda a da ultima posição do arquivo
    maxId = 59431;
    gamesCount = maxId;
  }

  // Fechamento do arquivo .db
  public void closeFile() throws IOException {
    try {
      file.close();
    } catch (Exception e) {
      System.err.println("Erro ao fechar arquivo .db: " + e);
    }
  }

  // Carrega dados do .csv para o .db
  public void loadData() {
    RandomAccessFile csv, write;

    try {
      csv = new RandomAccessFile("./TP01/db/games.csv", "r");
      write = new RandomAccessFile("./TP01/out/games.db", "rw");
      
      write.writeLong(10); // Reserva um espaço no início do arquivo para inserir a posição do final do arquivo

      csv.readLine(); // Pular cabeçalho do CSV
      String str;

      System.out.println("Carregando dados para o arquivo binário...");

      while ((str = csv.readLine()) != null) {
        String[] vet = str.split(","); // Separando em vetor a string (assumindo que o CSV usa vírgulas como separadores)

        // Criando o objeto Games a partir dos dados do CSV
        Games tmp = new Games(
          false, // Lápide (inicialmente false)
          Integer.parseInt(vet[0]), // appid
          vet[1], // name
          Integer.parseInt(vet[2]), // release_date (ou outro campo de data)
          Float.parseFloat(vet[3]), // price ou qualquer outro dado numérico
          vet[4], // developer
          vet[5], // publisher
          vet[6], // platforms
          vet[7], // required_age
          vet[8] // categories, ou outro campo
        );

        byte[] aux = tmp.byteParse(); // Convertendo o objeto para bytes

        write.writeInt(aux.length); // Tamanho do registro antes de cada vetor
        write.write(aux); // Inserindo o vetor de dados de byte no arquivo binário

        lastPos = write.getFilePointer(); // Atualizando a última posição

      }

      write.seek(0); // Posicionando no início do arquivo
      write.writeLong(lastPos); // Escrevendo a última posição no início

      write.close();
      csv.close();

      System.out.println("Dados carregados com sucesso!\n");
    } catch (Exception e) {
      System.err.println("Erro ao carregar dados: " + e);
    }
  }

  // Transformar string data em horas, a partir de 1900
  public static int dateToHours(String data) {
    String[] tmp = data.split("/");
    int year, month, day;

    year = (Integer.parseInt(tmp[2]) - inicialYear) * 8760; // Convertendo anos para horas
    month = (Integer.parseInt(tmp[1])) * 730; // Convertendo meses para horas
    day = Integer.parseInt(tmp[0]) * 24; // Convertendo dias para horas

    return year + month + day;
  }

  // Checa se o game é válido ou não, quanto a lápide e ao ID
  public boolean isGameValid(byte[] arr, int id) {
    boolean resp = false;
    ByteArrayInputStream by = new ByteArrayInputStream(arr);
    DataInputStream dis = new DataInputStream(by);

    try {
      if (!dis.readBoolean() && dis.readInt() == id) {
        resp = true;
      }
    } catch (Exception e) {
      System.err.println("Erro na checagem de validade do Game: " + e);
    }

    return resp;
  }

  // Outros métodos de CRUD...
}
