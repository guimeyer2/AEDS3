package Algoritmos;


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.RandomAccessFile;
import java.util.Arrays;
import java.util.Comparator;

/**
 * Classe que implementa o algoritmo da Árvore B em memória secundária
 * para indexação de jogos da Steam
 */
public class BTree {

    /**
     * Classe que representa um registro de índice
     */
    public class Indice {
        public int id;            // ID do jogo (appid)
        public long endereco;     // Posição do registro no arquivo de dados

        public Indice() {
            this.id = -1;
            this.endereco = -1;
        }

        public Indice(int id, long endereco) {
            this.id = id;
            this.endereco = endereco;
        }
    }

    /**
     * Classe que implementa a estrutura da Página da Árvore B
     */
    public class Pagina {
        public int numElementos;
        public Indice[] chaves;
        public long[] filhos;
        public boolean folha = true;

        public Pagina(int ordem) {
            chaves = new Indice[ordem - 1];
            filhos = new long[ordem];
            criarPagina(ordem);
        }

        /**
         * Método que preenche a página com valores "nulos" (-1), para ids e endereços
         */
        public void criarPagina(int ordem) {
            numElementos = 0;

            for (int i = 0; i < (ordem - 1); i++) {
                chaves[i] = new Indice();
            }
            for (int i = 0; i < ordem; i++) {
                filhos[i] = -1;
            }

            folha = true;
        }

        /**
         * Método que transforma o objeto Pagina em um vetor de bytes para ser inserido no arquivo
         */
        public byte[] toByteArray() {
            try {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                DataOutputStream dos = new DataOutputStream(baos);

                dos.writeInt(numElementos);

                for (int i = 0; i < filhos.length; i++) {
                    dos.writeLong(filhos[i]);
                }

                for (int i = 0; i < chaves.length; i++) {
                    dos.writeInt(chaves[i].id);
                    dos.writeLong(chaves[i].endereco);
                }

                dos.writeBoolean(folha);

                return baos.toByteArray();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        /**
         * Método que transforma um vetor de bytes em um objeto Pagina, para que seja manipulado
         */
        public void fromByteArray(byte[] array) {
            try {
                ByteArrayInputStream bais = new ByteArrayInputStream(array);
                DataInputStream dis = new DataInputStream(bais);

                this.numElementos = dis.readInt();

                for (int i = 0; i < filhos.length; i++) {
                    this.filhos[i] = dis.readLong();
                }

                for (int i = 0; i < chaves.length; i++) {
                    if (chaves[i] == null) {
                        chaves[i] = new Indice();
                    }
                    this.chaves[i].id = dis.readInt();
                    this.chaves[i].endereco = dis.readLong();
                }

                this.folha = dis.readBoolean();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        /**
         * Método que ordena os registros da página
         */
        public void ordenarChaves() {
            Indice[] validos = new Indice[numElementos];
            int cont = 0;

            for (int i = 0; i < chaves.length; i++) {
                if (chaves[i] != null && chaves[i].id >= 0) {
                    validos[cont] = chaves[i];
                    cont++;
                }
            }

            Arrays.sort(validos, 0, cont, Comparator.comparingInt(r -> r.id));

            for (int i = 0; i < chaves.length; i++) {
                if (i < cont) {
                    chaves[i] = validos[i];
                } else {
                    chaves[i] = new Indice();
                }
            }
        }
    }

    /**
     * Classe para auxiliar o split de uma página da Árvore B
     */
    private class SplitResult {
        public Indice indice;
        public long enderecoPaginaNova;

        public SplitResult(Indice subir, long enderecoPaginaNova) {
            this.indice = subir;
            this.enderecoPaginaNova = enderecoPaginaNova;
        }
    }

    private int ordem;
    private long raiz;
    private static final String arquivo = "TP2/src/btree_index.db";
    private RandomAccessFile file;

    /**
     * Construtor usado quando a árvore já está construída
     */
    public BTree() {
        try {
            File arqIndice = new File(arquivo);
            file = new RandomAccessFile(arqIndice, "rw");

            if (file.length() > 0) {
                file.seek(0);
                this.raiz = file.readLong();
                this.ordem = file.readInt();
            } else {
                this.ordem = 5; // Ordem padrão
                this.raiz = -1;
                file.writeLong(raiz);
                file.writeInt(ordem);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Construtor usado quando a árvore será construída
     */
    public BTree(int ordem) {
        this.ordem = ordem;
        this.raiz = -1;

        try {
            File arqIndice = new File(arquivo);
            file = new RandomAccessFile(arqIndice, "rw");

            file.setLength(0);
            file.seek(0);
            file.writeLong(raiz);
            file.writeInt(ordem);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Fecha o arquivo de índice
     */
    public void close() {
        try {
            if (file != null) {
                file.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Método que insere a página no arquivo, no endereço informado
     */
    private void inserirPagina(Pagina pagina, long endereco) {
        try {
            file.seek(endereco);

            byte[] array = pagina.toByteArray();

            if (array != null) {
                file.writeInt(array.length);
                file.write(array);
            } else {
                throw new Exception("Erro ao transformar em array de bytes!");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Método que vai até o endereço informado, lê uma página inteira, cria o objeto e o retorna
     */
    private Pagina lerPagina(long endereco) {
        try {
            file.seek(endereco);

            int tam = file.readInt();
            byte[] array = new byte[tam];
            file.readFully(array);

            Pagina nova = new Pagina(this.ordem);
            nova.fromByteArray(array);

            return nova;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Método que insere um índice na árvore B
     */
    public void inserir(int id, long endereco) {
        Indice indice = new Indice(id, endereco);
        
        try {
            if (raiz == -1) {
                // Árvore vazia, cria a raiz
                long enderecoNovaPagina = file.length();
                file.seek(enderecoNovaPagina);

                Pagina pagina = new Pagina(this.ordem);
                pagina.chaves[0] = indice;
                pagina.numElementos = 1;

                inserirPagina(pagina, enderecoNovaPagina);

                file.seek(0);
                file.writeLong(enderecoNovaPagina);
                this.raiz = enderecoNovaPagina;
            } else {
                inserir(indice, raiz, null, -1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Método que insere um índice recursivamente
     */
    private void inserir(Indice indice, long endereco, Pagina pai, long enderecoPai) {
        try {
            Pagina pagina = lerPagina(endereco);

            if (pagina.folha) {
                if (pagina.numElementos < (ordem - 1)) {
                    // Há espaço na página
                    pagina.chaves[pagina.numElementos] = indice;
                    pagina.numElementos++;
                    pagina.ordenarChaves();

                    inserirPagina(pagina, endereco);
                } else {
                    // Página cheia, precisa fazer split
                    SplitResult splitado = splitPagina(pagina, indice, endereco);
                    
                    if (splitado != null) {
                        inserirEndereco(pai, enderecoPai, splitado.enderecoPaginaNova, splitado.indice);
                    }
                }
            } else {
                // Página não é folha, procura o filho correto para inserir
                long e = procurarEndereco(indice, pagina);
                inserir(indice, e, pagina, endereco);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Método auxiliar para o split, que recebe o elemento que irá subir e o coloca na página pai
     */
    private SplitResult inserirEndereco(Pagina pai, long enderecoPai, long endereco, Indice aSubir) {
        try {
            if (pai.numElementos < (ordem - 1)) {
                // Há espaço no pai
                int pos = 0;
                while (pos < pai.numElementos && pai.chaves[pos].id < aSubir.id) {
                    pos++;
                }

                // Desloca chaves para inserir a nova
                for (int j = pai.numElementos; j > pos; j--) {
                    pai.chaves[j] = pai.chaves[j - 1];
                }
                pai.chaves[pos] = aSubir;

                // Desloca filhos para inserir o novo filho
                for (int j = pai.numElementos + 1; j > pos; j--) {
                    pai.filhos[j] = pai.filhos[j - 1];
                }
                pai.filhos[pos + 1] = endereco;

                pai.numElementos++;
                inserirPagina(pai, enderecoPai);

                return null;
            } else {
                // Pai também está cheio, precisa fazer split
                return splitPagina(pai, aSubir, enderecoPai);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Método que procura o endereço da próxima página para inserir o registro
     */
    private long procurarEndereco(Indice indice, Pagina pagina) {
        for (int i = 0; i < pagina.numElementos; i++) {
            if (indice.id < pagina.chaves[i].id) {
                return pagina.filhos[i];
            }
        }
        return pagina.filhos[pagina.numElementos];
    }

    /**
     * Método split
     */
    private SplitResult splitPagina(Pagina cheia, Indice novo, long enderecoPaginaCheia) {
        try {
            Indice[] indices = new Indice[ordem];

            // Coloca todos os elementos em um array (elementos antigos + novo)
            for (int i = 0; i < (ordem - 1); i++) {
                indices[i] = cheia.chaves[i];
            }
            indices[ordem - 1] = novo;

            // Ordena o array
            Arrays.sort(indices, Comparator.comparingInt(r -> r.id));

            // Cria duas novas páginas
            Pagina novaEsq = new Pagina(this.ordem);
            Pagina novaDir = new Pagina(this.ordem);

            // Distribui os elementos entre as páginas
            for (int i = 0; i < (ordem / 2); i++) {
                novaEsq.chaves[i] = indices[i];
            }
            Indice vaiSubir = indices[ordem / 2];
            for (int i = ((ordem / 2) + 1), j = 0; i < indices.length; i++, j++) {
                novaDir.chaves[j] = indices[i];
            }

            novaEsq.numElementos = (ordem / 2);
            novaDir.numElementos = (ordem / 2);

            novaEsq.folha = cheia.folha;
            novaDir.folha = cheia.folha;

            // Distribui os filhos se não for folha
            if (!cheia.folha) {
                for (int i = 0; i <= ordem / 2; i++) {
                    novaEsq.filhos[i] = cheia.filhos[i];
                }

                for (int i = (ordem / 2) + 1, j = 0; i < ordem; i++, j++) {
                    novaDir.filhos[j] = cheia.filhos[i];
                }
            }

            // Insere as novas páginas no arquivo
            inserirPagina(novaEsq, enderecoPaginaCheia);

            long enderecoNovo = file.length();
            inserirPagina(novaDir, enderecoNovo);

            // Se a página que sofreu split for a raiz, cria uma nova raiz
            if (enderecoPaginaCheia == raiz) {
                Pagina raizNova = new Pagina(this.ordem);
                raizNova.chaves[0] = vaiSubir;
                raizNova.filhos[0] = enderecoPaginaCheia;
                raizNova.filhos[1] = enderecoNovo;
                raizNova.numElementos = 1;
                raizNova.folha = false;

                long enderecoRaizNova = file.length();
                inserirPagina(raizNova, enderecoRaizNova);
                
                file.seek(0);
                file.writeLong(enderecoRaizNova);
                this.raiz = enderecoRaizNova;
            } else {
                return new SplitResult(vaiSubir, enderecoNovo);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Procura um registro pelo ID na árvore
     * @return Endereço do registro no arquivo de dados ou -1 se não encontrado
     */
    public long procurar(int id) {
        if (raiz == -1) {
            return -1;
        }
        return procurar(id, raiz);
    }

    /**
     * Método de procura recursivo
     */
    private long procurar(int id, long endereco) {
        Pagina pagina = lerPagina(endereco);
        if (pagina == null) return -1;

        for (int i = 0; i < pagina.numElementos; i++) {
            if (id == pagina.chaves[i].id) {
                // Encontrou o índice
                return pagina.chaves[i].endereco;
            } else if (id < pagina.chaves[i].id) {
                // Se o id procurado é menor, vai para o filho à esquerda
                if (pagina.filhos[i] == -1) {
                    return -1; // Não encontrado
                }
                return procurar(id, pagina.filhos[i]);
            }
        }

        // Se chegou aqui, o id é maior que todos os da página, vai para o último filho
        if (pagina.filhos[pagina.numElementos] == -1) {
            return -1; // Não encontrado
        }
        return procurar(id, pagina.filhos[pagina.numElementos]);
    }

    /**
     * Atualiza o endereço de um registro na árvore
     */
    public boolean atualizar(int id, long enderecoNovo) {
        if (raiz == -1) {
            return false;
        }
        return atualizar(id, raiz, enderecoNovo);
    }

    /**
     * Método de atualização recursivo
     */
    private boolean atualizar(int id, long endereco, long enderecoNovo) {
        Pagina pagina = lerPagina(endereco);
        if (pagina == null) return false;

        for (int i = 0; i < pagina.numElementos; i++) {
            if (id == pagina.chaves[i].id) {
                // Encontrou o índice, atualiza
                pagina.chaves[i].endereco = enderecoNovo;
                inserirPagina(pagina, endereco);
                return true;
            } else if (id < pagina.chaves[i].id) {
                // Se o id procurado é menor, vai para o filho à esquerda
                if (pagina.filhos[i] == -1) {
                    return false; // Não encontrado
                }
                return atualizar(id, pagina.filhos[i], enderecoNovo);
            }
        }

        // Se chegou aqui, o id é maior que todos os da página, vai para o último filho
        if (pagina.filhos[pagina.numElementos] == -1) {
            return false; // Não encontrado
        }
        return atualizar(id, pagina.filhos[pagina.numElementos], enderecoNovo);
    }

    /**
     * Remove um índice da árvore (marca como inválido)
     */
    public boolean deletar(int id) {
        if (raiz == -1) {
            return false;
        }
        return deletar(id, raiz);
    }

    /**
     * Método de remoção recursivo
     */
    private boolean deletar(int id, long endereco) {
        Pagina pagina = lerPagina(endereco);
        if (pagina == null) return false;

        for (int i = 0; i < pagina.numElementos; i++) {
            if (id == pagina.chaves[i].id) {
                // Encontrou o índice, marca como inválido
                pagina.chaves[i].id = -1;
                pagina.chaves[i].endereco = -1;
                // Reordena a página para manter consistência
                pagina.ordenarChaves();
                // Atualiza o número de elementos válidos
                int novosElementos = 0;
                for (int j = 0; j < pagina.numElementos; j++) {
                    if (pagina.chaves[j].id >= 0) {
                        novosElementos++;
                    }
                }
                pagina.numElementos = novosElementos;
                
                inserirPagina(pagina, endereco);
                return true;
            } else if (id < pagina.chaves[i].id) {
                // Se o id procurado é menor, vai para o filho à esquerda
                if (pagina.filhos[i] == -1) {
                    return false; // Não encontrado
                }
                return deletar(id, pagina.filhos[i]);
            }
        }

        // Se chegou aqui, o id é maior que todos os da página, vai para o último filho
        if (pagina.filhos[pagina.numElementos] == -1) {
            return false; // Não encontrado
        }
        return deletar(id, pagina.filhos[pagina.numElementos]);
    }
    
    /**
     * Método para carregar todos os registros de um arquivo de dados para a árvore B
     */
    public void carregarDados(String arquivoDados) {
        try {
            File dbFile = new File(arquivoDados);
            if (!dbFile.exists()) {
                System.out.println("Arquivo de dados não encontrado!");
                return;
            }
            
            RandomAccessFile dataFile = new RandomAccessFile(dbFile, "r");
            
            // Pula o cabeçalho do arquivo de dados (max ID e lastPos)
            dataFile.seek(12);
            
            long pos = 12;
            int contadorRegistros = 0;
            
            while (pos < dataFile.length()) {
                long posicaoRegistro = pos;
                byte tombstone = dataFile.readByte();
                int tam = dataFile.readInt();
                
                if (tombstone == 0 && tam > 0) {
                    byte[] dados = new byte[tam];
                    dataFile.readFully(dados);
                    
                    // Extrai o ID do registro
                    ByteArrayInputStream bis = new ByteArrayInputStream(dados);
                    DataInputStream dis = new DataInputStream(bis);
                    int id = dis.readInt();
                    dis.close();
                    
                    // Insere na árvore B
                    inserir(id, posicaoRegistro);
                    contadorRegistros++;
                } else {
                    // Pula o registro se estiver marcado como excluído
                    if (tam > 0) {
                        dataFile.skipBytes(tam);
                    }
                }
                
                pos = dataFile.getFilePointer();
            }
            
            dataFile.close();
            System.out.println("Dados carregados para a árvore B: " + contadorRegistros + " registros indexados.");
            
        } catch (Exception e) {
            System.err.println("Erro ao carregar dados para a árvore B: " + e.getMessage());
            e.printStackTrace();
        }
    }
}