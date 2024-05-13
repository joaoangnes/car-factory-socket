package src;

import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.Semaphore;

// Classe que representa a esteira da fábrica
class EsteiraFabrica implements Runnable{
  // Buffer para armazenar os veículos na esteira
  private Veiculo[] buffer;
  // Tamanho da esteira
  private int tamanho;
  // Índices de entrada e saída para o buffer circular
  public int entrada, saida;

  // Semáforos para controlar o acesso ao buffer
  private Semaphore mutex; // garante acesso exclusivo ao buffer
  private Semaphore vazio; // controla o número de espaços vazios no buffer
  private Semaphore cheio; // controla o número de espaços ocupados no buffer

  // Arquivo de log para registrar as ações
  private final FileWriter logFile;

  // Construtor da esteira
  public EsteiraFabrica(int tamanho, FileWriter logFile) {
      this.tamanho = tamanho;
      this.buffer = new Veiculo[tamanho];
      this.entrada = 0;
      this.saida = 0;
      this.mutex = new Semaphore(1);
      this.vazio = new Semaphore(tamanho);
      this.cheio = new Semaphore(0);
      this.logFile = logFile;
  }

  // Método executado quando a thread da esteira é iniciada
  @Override
  public void run() {
      // try {
      //     while (true) {
      //         cheio.acquire(); // aguarda até que haja um veículo na esteira
      //         Veiculo veiculo = retirarVeiculoDaEsteira(); // retira o veículo da esteira
      //         vazio.release(); // libera um espaço vazio na esteira
      //     }
      // } catch (InterruptedException  e) {
      //     Thread.currentThread().interrupt();
      // }
  }

  // Método para adicionar um veículo na esteira
  public void estocarVeiculo(Veiculo veiculo) throws InterruptedException {
      vazio.acquire(); // aguarda até que haja um espaço vazio na esteira
      mutex.acquire(); // garante acesso exclusivo ao buffer
      this.buffer[this.entrada] = veiculo; // adiciona o veículo na esteira
      try {
        log("[ESTEIRA]  - Veículo foi adicionado na esteira" + " na posição " + this.entrada + " (ID: " + veiculo.getId() + ", Cor: " + veiculo.getCor() +
        ", Tipo: " + veiculo.getTipo() + ", Estação: " + veiculo.getEstacaoId() +
        ", Funcionário: " + veiculo.getFuncionarioId() + ")");
      } catch (IOException e) {
        e.printStackTrace();
      }
      this.entrada = (this.entrada + 1) % this.tamanho; // atualiza o índice de entrada
      mutex.release(); // libera o acesso ao buffer
      cheio.release(); // indica que há um espaço ocupado na esteira
  }

  // Método para retirar um veículo da esteira
  public Veiculo retirarVeiculoDaEsteira() throws InterruptedException {
      cheio.acquire(); // aguarda até que haja um veículo na esteira
      Veiculo veiculo = this.buffer[this.saida]; // retira o veículo da esteira
      mutex.acquire(); // garante acesso exclusivo ao buffer
      Thread.sleep(1000); // Sleep para simular o tempo de retirada do veículo
      this.saida = (this.saida + 1) % this.tamanho; // atualiza o índice de saída
      try {
          log("[VENDA]    - Veículo retirado da esteira " + "(ID: " + veiculo.getId() + ", Cor: " + veiculo.getCor() +
          ", Tipo: " + veiculo.getTipo() + ", Estação: " + veiculo.getEstacaoId() +
          ", Funcionário: " + veiculo.getFuncionarioId() + ")");
      } catch (IOException e) {
          e.printStackTrace();
      }
      mutex.release(); // libera o acesso ao buffer
      vazio.release(); // libera um espaço vazio na esteira
      return veiculo;
  }

  // Método para obter o número de posições restantes na esteira
  public int getPosicoesRestantes() {
      return vazio.availablePermits();
  }

  // Método para registrar uma mensagem no arquivo de log
  private void log(String message) throws IOException {
    logFile.write(message + "\n");
    logFile.flush();
  }

  public Veiculo[] getBuffer() {
    return buffer;
  }

  public int getTamanho() {
    return tamanho;
  }

  public int getEntrada() {
    return entrada;
  }

  public int getSaida() {
    return saida;
  }

  public Semaphore getMutex() {
    return mutex;
  }

  public Semaphore getVazio() {
    return vazio;
  }

  public Semaphore getCheio() {
    return cheio;
  }

  public FileWriter getLogFile() {
    return logFile;
  }
}