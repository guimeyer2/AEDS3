package controller;

import controller.Actions;
import Model.steam;
import Algoritmos.HashExtensivel;

import java.io.RandomAccessFile;

public class HashCrud extends Actions {

  private int hashPtrGlobal;
  private int hashContBuckets;

  HashExtensivel hash = new HashExtensivel();

  // * Getters e setters
  public int getHashPtrGlobal() {
    return hashPtrGlobal;
  }

  public void setHashPtrGlobal(int hashPtrGlobal) {
    this.hashPtrGlobal = hashPtrGlobal;
  }

  public int getHashContBuckets() {
    return hashContBuckets;
  }

  public void setHashContBuckets(int hashContBuckets) {
    this.hashContBuckets = hashContBuckets;
  }

  // * Load dados do DB no Hash indexado
  public void loadDataToHash() {
    try (RandomAccessFile file = new RandomAccessFile("./TP2/src/steam.db", "rw")) {
      long ultimaPos = file.length(); // ← mais seguro
      file.seek(13); // ignora cabeçalho, se existir
  
      while (file.getFilePointer() < ultimaPos) {
        long pos = file.getFilePointer(); // posição do registro
  
        int tam = file.readInt(); // tamanho do registro
        System.out.println("tamanho do registro " + tam);
  
        if (tam <= 0 || tam > 10000) {
          System.err.println("Tamanho inválido: " + tam + " em pos " + pos);
          break;
        }
  
        byte[] arr = new byte[tam];
        file.readFully(arr); // ← aqui está a segurança
  
        steam aux = new steam();
        try {
          aux.fromByteArray(arr);
          System.out.println("objeto adicionado: id " + aux.getAppid());
  
          // insere no hash
          hash.createInHash(aux.getAppid(), pos);
          System.out.println("inserido no hash\n");
        } catch (Exception e) {
          System.err.println("Erro ao desserializar: " + e.getMessage());
          // continua lendo os próximos
        }
      }
  
    } catch (Exception e) {
      System.err.println("Erro ao carregar dados para o Hash: " + e);
    }
  }
  

  // * Buscar registro usando hash indexado
  public steam readHash(int id) {
    try {
      long pos = hash.searchHash(id);

      if (pos != -1) {
        file.seek(pos + 1); // posiciona ptr no end. correto, pulando a lápide
        int tam = file.readInt(); // tamanho do registro
        byte[] arr = new byte[tam];
        file.read(arr);

        steam aux = new steam();

        System.out.println("O game encontrado foi: ");
        aux.toString();

        aux.fromByteArray(arr);

        return aux;
      } else {
        // jogo não encontrado
        return null;
      }
    } catch (Exception e) {
      System.err.println("Erro ao buscar registro no Hash: " + e);
    }

    return null;
  }

  public steam readHashh (int id) {
    try {
      steam aux = new steam();
      aux = this.readGame(id);
      return aux;
    } catch (Exception e) {
      System.err.println("Erro ao buscar registro no Hash: " + e);
      return null;
    }
  }

  // * Deletar registro no Hash indexado
  public steam deleteHash(int id) {
    try {
      long pos = hash.searchHash(id);

      if (pos != -1) {
        file.seek(pos); // posiciona ptr no end. correto, pulando a lápide
        file.writeByte(-1); // marca como deletado
        hash.deleteInHash(id, pos);
        System.out.println("Registro deletado com sucesso!");
        return null;
      } else {
        System.out.println("Registro não encontrado!");
        return null;
      }
    } catch (Exception e) {
      System.err.println("Erro ao deletar registro no Hash: " + e);
      return null;
    }
  }

  public steam deleteHashh(int id) {
    try {
      steam aux = this.deleteGame(id);
      return aux;
    } catch (Exception e) {
      System.err.println("Erro ao deletar registro no Hash: " + e);
      return null;
    }
  }

  // * Cria registro no Hash indexado
  public boolean createHash(steam tmp) {
    try {
      file.seek(file.length()); // Posiciona o ponteiro no final do arquivo
      long pos = file.getFilePointer();

      byte[] arr = tmp.toByteArray(); // Converte o game para vetor de bytes
      file.writeInt(arr.length); // Escreve o tamanho do registro

      file.write(arr); // Escreve o registro

      hash.createInHash(tmp.getAppid(), pos); // Insere no hash
      return true; // Return true if the hash creation is successful
    } catch (Exception e) {
      System.err.println("Erro ao criar registro no Hash: " + e);
      return false; // Return false if there is an error in hash creation
    }
  }

  public boolean createHashh(steam tmp) {
    try {
      boolean aux = this.createGame(tmp);
      return aux;
    } catch (Exception e) {
      System.err.println("Erro ao criar registro no Hash: " + e);
      return false;
    }
  }


  public void getHashInfo() {
    hash.readCbAndPg();
    setHashPtrGlobal(hash.getPtrGlobal());
    setHashContBuckets(hash.getContBuckets());
  }

  public long searchGameHash(int id) {
    return hash.searchHash(id);
  }
}