package Algoritmos;

import java.io.RandomAccessFile;

public class HashExtensivel {

  // Ponteiros
  private int ptrGlobal; 
  private int contBuckets; 
  private long endBucket;
  // Buckets
  private int ptrLocal;
  private int contReg; 
  private int maxReg = 2972;
  // Registros
  private int tamReg = 12; 
  private int idReg; 
  private long endReg; 

  // * Arquivos
  public RandomAccessFile hashIndex;
  public RandomAccessFile hashBuckets;

  // * Ponteiros
  public int getPtrGlobal() {
    return ptrGlobal;
  }

  public void setPtrGlobal(int ptrGlobal) {
    this.ptrGlobal = ptrGlobal;
  }

  public int getContBuckets() {
    return contBuckets;
  }

  public void setContBuckets(int contBuckets) {
    this.contBuckets = contBuckets;
  }

  public long getEndBucket() {
    return endBucket;
  }

  public void setEndBucket(long endBucket) {
    this.endBucket = endBucket;
  }

  // * Buckets
  public int getPtrLocal() {
    return ptrLocal;
  }

  public void setPtrLocal(int ptrLocal) {
    this.ptrLocal = ptrLocal;
  }

  public int getContReg() {
    return contReg;
  }

  public void setContReg(int contReg) {
    this.contReg = contReg;
  }

  public int getMaxReg() {
    return maxReg;
  }

  // * Registros
  public int getTamReg() {
    return tamReg;
  }

  public int getIdReg() {
    return idReg;
  }

  public void setIdReg(int idReg) {
    this.idReg = idReg;
  }

  public long getEndReg() {
    return endReg;
  }

  public void setEndReg(long endReg) {
    this.endReg = endReg;
  }

  // * Construtor
  public HashExtensivel() {
    // Ponteiros
    this.ptrGlobal = 1;
    this.contBuckets = 0;
    this.endBucket = -1;
    // Buckets
    this.ptrLocal = 1;
    this.contReg = 0;
    this.maxReg = 2972;
    // Registros
    this.tamReg = 12;
    this.idReg = -1;
    this.endReg = -1;

    try {
      this.hashIndex = new RandomAccessFile("./TP2/src/hashIndex.db", "rw"); // cria o arquivo de indice
      this.hashBuckets = new RandomAccessFile("./TP2/src/hashBuckets.db", "rw"); // cria o arquivo de buckets

      if (hashBuckets.length() == 0 && hashIndex.length() == 0) { 
        // ponteiros no inicio do file
        hashIndex.seek(0);
        hashBuckets.seek(0);

        hashIndex.writeInt(1); 
        hashIndex.writeInt(2); 

        for (int i = 0; i < 2; i++) {
          hashIndex.writeLong(hashBuckets.getFilePointer()); 
          hashBuckets.writeInt(1); 
          hashBuckets.writeInt(0); 
          for (int j = 0; j < maxReg; j++) { 
            hashBuckets.writeInt(-1); 
            hashBuckets.writeLong(-1); 
          }
        }
        setContBuckets(2);
      }
    } catch (Exception e) {
      System.out.println();
      System.err.println(
        "Erro ao criar os arquivos de buckets: " + e.getMessage()
      );
      e.printStackTrace();
    }
  }

  
  public void readCbAndPg() {
    try {
      hashIndex.seek(0); 

      // leitura da contagem de buckets
      setPtrGlobal(hashIndex.readInt());
      setContBuckets(hashIndex.readInt());
    } catch (Exception e) {
      System.out.println();
      System.err.println(
        "Erro ao ler ptrGlobal e contBuckets! " + e.getMessage()
      );
      e.printStackTrace();
    }
  }

  public long funcaoHash(int id, int p) {
    int qualBucket = id % ((int) Math.pow(2, p));
    long posBucket = -1; // posicao do bucket no arquivo

    try {
      hashIndex.seek(8); // posiciona o ponteiro no inicio do arquivo, pulando o ptrGlobal e a qtd de buckets
      hashIndex.skipBytes(qualBucket * 8); // pula para o endereco do bucket certo
      posBucket = hashIndex.readLong(); // le o endBucket
    } catch (Exception e) {
      System.out.println();
      System.err.println("Erro no cálculo da função hash! " + e.getMessage());
      e.printStackTrace();
    }

    return posBucket;
  }

  // busca por um registro no hash
  public long searchHash(int id) {
    long posBucket = funcaoHash(id, getPtrGlobal());

    try {
      hashBuckets.seek(posBucket + 4); // posiciona o ponteiro no inicio do bucket certo, pulando o ptrLocal
      int registrosBucket = hashBuckets.readInt(); // le a contagem de registros do bucket
      setContReg(registrosBucket);

      // percorre o bucket sequencialmente
      for (int i = 0; i < getContReg(); i++) {
        // se o id do registro == ao id procurado
        if (hashBuckets.readInt() == id) {
          setEndReg(hashBuckets.readLong()); // le o endReg
          return getEndReg();
        } else {
          hashBuckets.skipBytes(8); // pula para o proximo registro
        }
      }
    } catch (Exception e) {
      System.out.println("Erro ao procurar registro: " + e.getMessage());
      e.printStackTrace();
    }
    return -1;
  }

  // apaga registro do hash
  public boolean deleteInHash(int id, long endBucket) {
    try {
      hashBuckets.seek(endBucket + 4); // posiciona ptr no bucket certo, pulando o ptrLocal
      setContReg(hashBuckets.readInt()); // le a contagem de registros do bucket

      for (int i = 0; i < 440; i++) { // percorre o bucket inteiro
        if (hashBuckets.readInt() == id) { // se o id do registro for igual ao id do registro a excluir
          hashBuckets.seek(hashBuckets.getFilePointer() - 4); // volta 4 bytes (idReg)
          hashBuckets.writeInt(-1); // escreve -1 no idReg
          hashBuckets.writeLong(-1); // escreve -1 no endReg
          hashBuckets.seek(endBucket + 4); // posiciona o ponteiro no inicio do bucket certo, pulando o ptrLocal
          hashBuckets.writeInt(getContReg() - 1); // atualiza a qtd de registros do bucket
          return true;
        } else {
          hashBuckets.skipBytes(8); // pula para o proximo registro
        }
      }
      return false;
    } catch (Exception e) {
      System.out.println();
      System.out.println("Erro ao excluir registro: " + e.getMessage());
      e.printStackTrace();
      return false;
    }
  }

  // insere registro no Hash
  public long createInHash(int id, long endReg) {
    long endBucketCheio = -1;
    long aux = -1; // aux para valores tmp

    try {
      // se os arqs ja existirem
      if (hashBuckets.length() != 0 && hashIndex.length() != 0) {
        hashIndex.seek(0); // posiciona o ptr no inicio do arquivo
        setPtrGlobal(hashIndex.readInt()); // le o ptrGlobal

        // função hash
        int qualBucket = id % ((int) Math.pow(2, getPtrGlobal()));

        hashIndex.skipBytes(4); // pula o contBuckets
        hashIndex.skipBytes(qualBucket * 8); // endereço do bucket correto
        setEndBucket(hashIndex.readLong()); // leitura do endBucket

        hashBuckets.seek(getEndBucket()); // posiciona o ptr no bucket certo
        setPtrLocal(hashBuckets.readInt()); // le o ptrLocal
        setContReg(hashBuckets.readInt()); // le a contagem de registros

        // * Caso 1: bucket possui espaco livre
        if (getContReg() < maxReg) {
          hashBuckets.seek(getEndBucket() + 8); // posiciona o ptr no inicio do bucket, pulando o ptrLocal e a contReg

          // percorre o bucket
          for (int i = 0; i < maxReg; i++) {
            // se o bucket estiver vazio
            if (hashBuckets.readInt() == -1) {
              hashBuckets.seek(hashBuckets.getFilePointer() - 4); // volta 4 bytes (idReg)
              hashBuckets.writeInt(id); // escreve o idReg
              hashBuckets.writeLong(endReg); // escreve o endReg
              hashBuckets.seek(getEndBucket() + 4); // posiciona o ponteiro no inicio do bucket, pulando o ptrLocal
              hashBuckets.writeInt(getContReg() + 1); // atualiza a contagem de registros
              i = maxReg;
            } else {
              hashBuckets.skipBytes(8); // pula para o proximo registro
            }
          }

          return getEndBucket();
        }
        // * Caso 2: bucket sem espaço e ptrLocal < ptrGlobal
        else if ((getContReg() == maxReg) && (getPtrLocal() < getPtrGlobal())) {
          // atualiza o ptrLocal
          hashBuckets.seek(getEndBucket()); // posiciona o ponteiro no inicio do bucket
          setPtrLocal(getPtrGlobal()); // atualiza o ptrLocal
          hashBuckets.writeInt(getPtrLocal()); // atualiza o ptrLocal no arquivo

          // cria bucket novo
          hashBuckets.seek(hashBuckets.length()); // posiciona o ponteiro no final do arquivo
          endBucketCheio = getEndBucket(); // armazena o endBucket do bucket cheio
          criaBucket(); // cria um novo bucket

          // atualiza o ptr do novo bucket
          hashIndex.seek(8); // posiciona o ponteiro no inicio do arquivo, pulando o ptrGlobal e o contBuckets
          for (int i = 0; i < (hashIndex.length() - 8); i++) {
            // se endBucket == endBucket do bucket que foi dividido
            if (hashIndex.readLong() == endBucketCheio) {
              i = (int) (hashIndex.length() - 8); // sai do loop
            }
          }
          while (hashIndex.getFilePointer() < hashIndex.length()) { // percorre o arquivo indexado
            if (hashIndex.readLong() == endBucketCheio) { // se o endBucket == endBucket do bucket que foi dividido
              hashIndex.seek(hashIndex.getFilePointer() - 8); // volta o ponteiro para o endBucket
              hashIndex.writeLong(getEndBucket()); // move o ponteiro para o novo endBucket
              break;
            }
          }

          // redistribuir os registros do bucket cheio
          aux = endBucketCheio + 8;
          for (int i = 0; i < 440; i++) { // percorre o bucket
            hashBuckets.seek(aux);
            if (hashBuckets.readInt() != -1) {
              hashBuckets.seek(hashBuckets.getFilePointer() - 4); // volta 4 bytes (idReg)
              setIdReg(hashBuckets.readInt()); // le o idReg
              setEndReg(hashBuckets.readLong()); // le o endReg
              aux = hashBuckets.getFilePointer();
              deleteInHash(getIdReg(), endBucketCheio); // exclui o registro do bucket cheio
              createInHash(getIdReg(), getEndReg());
            } else {
              aux += 12; // pula para o proximo registro
            }
          }

          // insere o novo registro
          createInHash(id, endReg);
        }
        // * Caso 3: bucket sem espaco e ptrLocal == ptrGlobal
        else if (
          (getContReg() == maxReg) && (getPtrLocal() == getPtrGlobal())
        ) {
          aumentaProfundidade();
          createInHash(id, endReg);
          return getEndBucket();
        }
      }
      return -1;
    } catch (Exception e) {
      System.out.println();
      System.out.println("Erro ao createInHash: " + e.getMessage());
      e.getStackTrace();
      return -1;
    }
  }

  // aumenta a profundidade do hash
  public void aumentaProfundidade() {
    try {
      int qtdPointersAntigo = ((int) Math.pow(2, getPtrGlobal())); // armazena a cont de ponteiros antes do aumento do ptrGlobal
      setPtrGlobal(getPtrGlobal() + 1); // atualiza o ptrGlobal

      hashIndex.seek(0); // posiciona o ptr no inicio do arquivo
      hashIndex.writeInt(getPtrGlobal()); // escreve o novo ptrGlobal
      hashIndex.skipBytes(4); // pula os 4 bytes da qtd de buckets

      for (int i = 1; i <= qtdPointersAntigo; i++) {
        setEndBucket(hashIndex.readLong()); // le o endBucket
        hashIndex.seek(hashIndex.length()); // posiciona o ponteiro no final do arquivo
        hashIndex.writeLong(getEndBucket()); // escreve o endBucket do novo pointer
        hashIndex.seek((i * 8) + 8); // posiciona o ponteiro depois do pointer lido, pulando ptrGlobal e contBuckets
      }
    } catch (Exception e) {
      System.out.println();
      System.out.println(
        "Erro ao aumentar a profundidade do Hash! " + e.getMessage()
      );
      e.printStackTrace();
    }
  }

  // cria um novo bucket
  public void criaBucket() {
    try {
      hashBuckets.seek(hashBuckets.length()); // posiciona o ponteiro no final do arquivo
      setEndBucket(hashBuckets.getFilePointer()); // armazena o novo endBucket

      hashBuckets.writeInt(getPtrGlobal()); // escreve o ptrLocal
      hashBuckets.writeInt(0); // escreve a cont de registros (bucket inicia vazio)

      for (int i = 0; i < maxReg; i++) { // registros vazios
        hashBuckets.writeInt(-1); // escreve o idReg
        hashBuckets.writeLong(-1); // escreve o endReg
      }

      setContBuckets(getContBuckets() + 1); // atualiza a cont de buckets escritos no arquivo
      hashIndex.seek(4); // posiciona o ptr no inicio do arquivo, pulando o ptrGlobal
      hashIndex.writeInt(getContBuckets()); // escreve a cont de buckets
    } catch (Exception e) {
      System.out.println();
      System.out.println("Erro ao criar novo bucket: " + e.getMessage());
      e.printStackTrace();
    }
  }
}