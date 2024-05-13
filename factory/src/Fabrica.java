package src;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;
import java.util.concurrent.Semaphore;

// Classe principal que representa a fábrica
public class Fabrica {
    // Variáveis para gerenciar as estações, a esteira e o estoque de peças
    // private final Thread[] estacoes; // Array de threads, cada uma representando uma estação de trabalho
    private final Semaphore estoquePecas; // Semáforo para controlar o acesso ao estoque de peças
    private final Random random; // Gerador de números aleatórios
    private final FileWriter logFile; // Arquivo de log para registrar as ações
    private final FileWriter vendaLogFile; // Arquivo de log para registrar as vendas

    private final Semaphore[] ferramentas;
    private final EsteiraFabrica esteira; // Esteira onde os veículos produzidos são colocados

    // Constantes para definir o número de estações, funcionários por estação, tamanho da esteira e estoque inicial de peças
    public static final int NUM_ESTACOES = 4; // Número de estações de trabalho na fábrica
    public static final int FUNCIONARIOS_POR_ESTACAO = 5; // Número de funcionários por estação de trabalho
    public static final int TAMANHO_ESTEIRA = 40; // Capacidade da esteira
    public static final int ESTOQUE_INICIAL_PECAS = 500; // Número inicial de peças no estoque

    // Construtor da fábrica
    public Fabrica() throws IOException {
        // Inicialização das variáveis
        // this.estacoes = new Thread[NUM_ESTACOES];
        this.logFile = new FileWriter("fabrica_log.txt");
        this.vendaLogFile = new FileWriter("venda_fabrica.txt");
        this.esteira = new EsteiraFabrica(TAMANHO_ESTEIRA, logFile);
        this.estoquePecas = new Semaphore(ESTOQUE_INICIAL_PECAS);
        this.random = new Random();

        this.ferramentas = new Semaphore[FUNCIONARIOS_POR_ESTACAO];
    }

    // Método para iniciar a produção na fábrica
    public void iniciarProducao() {
        // Criação de threads para cada funcionário em cada estação
        for (int i = 0; i < NUM_ESTACOES; i++) {
            for (int f = 0; f < FUNCIONARIOS_POR_ESTACAO; f++) {
                ferramentas[f] = new Semaphore(1);
            }
            for (int j = 0; j < FUNCIONARIOS_POR_ESTACAO; j++) {
                // Criação de um novo funcionário e sua thread
                Funcionario funcionario = new Funcionario(i + 1, j + 1, esteira, estoquePecas, ferramentas[j], ferramentas[(j + 1) % FUNCIONARIOS_POR_ESTACAO], random, logFile);
                Thread funcionarioThread = new Thread(funcionario, "Funcionário " + (j + 1) + " - Estação " + (i + 1));
                // Início da thread do funcionário
                funcionarioThread.start();
            }
        }
        // Criação da thread da esteira
        Thread esteiraThread = new Thread(this.esteira, "Esteira");
        // Início da thread da esteira
        esteiraThread.start();
    }

    // Método para realizar a venda de um veículo
    public Veiculo realizarVendaVeiculo() throws InterruptedException {
        // Verifica se há veículos na esteira
        if (esteira.getCheio().availablePermits() > 0) {
            // Retira um veículo da esteira
            return esteira.retirarVeiculoDaEsteira();
        } else {
            return null;
        }
    }
    // Método para registrar a venda de um veículo
    public void registrarVenda(Veiculo veiculo, int idLoja, int posicaoEsteira) {
        try { 
            this.vendaLogFile.write(String.format("[VENDA] - Venda realizada: ID da Loja: %d, Posição na esteira: %d, Informações do veículo: %s\n", idLoja, posicaoEsteira, "(ID: " + veiculo.getId() + ", Cor: " + veiculo.getCor() +
                                                                                                                                                                                    ", Tipo: " + veiculo.getTipo() + ", Estação: " + veiculo.getEstacaoId() +
                                                                                                                                                                                    ", Funcionário: " + veiculo.getFuncionarioId() + ")"));
            // this.vendaLogFile.write("--------------------------------------------------\n");
            this.vendaLogFile.flush();
        } catch (IOException e) {
            System.out.println("Erro ao registrar a venda: " + e.getMessage());
        }
    }

    // Método principal para iniciar a fábrica
    public static void main(String[] args) throws IOException {
        // Criação de uma nova fábrica
        Fabrica fabrica = new Fabrica();
        // Início da produção na fábrica
        fabrica.iniciarProducao();
    }

    public Semaphore getEstoquePecas() {
        return estoquePecas;
    }

    public Random getRandom() {
        return random;
    }

    public FileWriter getLogFile() {
        return logFile;
    }

    public EsteiraFabrica getEsteira() {
        return esteira;
    }

    public static int getNumEstacoes() {
        return NUM_ESTACOES;
    }

    public static int getFuncionariosPorEstacao() {
        return FUNCIONARIOS_POR_ESTACAO;
    }

    public static int getTamanhoEsteira() {
        return TAMANHO_ESTEIRA;
    }

    public static int getEstoqueInicialPecas() {
        return ESTOQUE_INICIAL_PECAS;
    }
}