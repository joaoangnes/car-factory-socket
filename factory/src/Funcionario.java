package src;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

// Classe que representa um funcionário da fábrica
class Funcionario implements Runnable {
  // Variáveis para gerenciar o funcionário, a esteira e o estoque de peças
  private final int estacaoId; // ID da estação de trabalho do funcionário
  private final int funcionarioId; // ID do funcionário
  private final EsteiraFabrica esteira; // Esteira onde o funcionário coloca os veículos produzidos
  private final Semaphore estoquePecas; // Semáforo para controlar o acesso ao estoque de peças
  private final Random random; // Gerador de números aleatórios
  private final FileWriter logFile; // Arquivo de log para registrar as ações
  private final Semaphore ferramentaEsquerda; // Semáforo para controlar o acesso à ferramenta da esquerda
  private final Semaphore ferramentaDireita; // Semáforo para controlar o acesso à ferramenta da direita

  // Construtor do funcionário
  public Funcionario(int estacaoId, int funcionarioId, EsteiraFabrica esteira, Semaphore estoquePecas, Semaphore ferramentaEsquerda, Semaphore ferramentaDireita, Random random, FileWriter logFile) {
    // Inicialização das variáveis
    this.estacaoId = estacaoId;
    this.funcionarioId = funcionarioId;
    this.esteira = esteira;
    this.estoquePecas = estoquePecas;
    this.random = random;
    this.logFile = logFile;
    this.ferramentaEsquerda = ferramentaEsquerda;
    this.ferramentaDireita = ferramentaDireita;
  }

  // Método que define o que o funcionário faz quando sua thread é iniciada
  @Override
public void run() {
  try {
    while (true) {
      if (funcionarioId < 5) {
        ferramentaEsquerda.acquire();
        ferramentaDireita.acquire();
      } else {
        ferramentaDireita.acquire();
        ferramentaEsquerda.acquire();
      }

      // Simular o trabalho
      Thread.sleep((long) (Math.random() * 1000));
      Veiculo veiculo = produzirVeiculo();

      // Liberar as ferramentas
      ferramentaEsquerda.release();
      ferramentaDireita.release();

      // Produzir um veículo e colocá-lo na esteira
      log("[PRODUÇÃO] - Veículo produzido (ID: " + veiculo.getId() + ", Cor: " + veiculo.getCor() +
      ", Tipo: " + veiculo.getTipo() + ", Estação: " + veiculo.getEstacaoId() +
      ", Funcionário: " + veiculo.getFuncionarioId() + ") - Posições restantes: " + esteira.getPosicoesRestantes());
      esteira.estocarVeiculo(veiculo);
    }
  } catch (InterruptedException | IOException e) {
    Thread.currentThread().interrupt();
  }
}

  // Método para produzir um veículo
  private Veiculo produzirVeiculo() throws InterruptedException, IOException {
    // Gerar um ID único para o veículo
    int idVeiculo = IdGenerator.nextId();
    // Gerar uma cor aleatória para o veículo
    int red = random.nextInt(256);
    int green = random.nextInt(256);
    int blue = random.nextInt(256);
    String cor = String.format("#%02x%02x%02x", red, green, blue);
    // Escolher um tipo aleatório para o veículo
    String[] tipos = {"SUV", "Sedan"};
    String tipo = tipos[random.nextInt(tipos.length)];
    // Solicitar uma peça do estoque
    solicitarPeca();
    // Retornar o veículo produzido
    return new Veiculo(idVeiculo, cor, tipo, estacaoId, funcionarioId);
  }

  // Método para solicitar uma peça do estoque
  private void solicitarPeca() throws InterruptedException, IOException {
    // Tentar adquirir uma peça do estoque
    if (estoquePecas.tryAcquire(1, 200, TimeUnit.MILLISECONDS)) {
      // Se conseguiu, não faz nada
    } else if (estoquePecas.availablePermits() == 0) {
      // Se o estoque está vazio, registra no log e interrompe a produção
      log("[FUNCIONARIO] - Funcionário " + funcionarioId + " - Estação " + estacaoId + " não pode solicitar uma peça porque o estoque está vazio");
      System.out.println("Estoque de peças vazio! Encerrando a produção...");
      throw new InterruptedException();
    } else {
      // Se o tempo limite foi atingido, registra no log
      log("[FUNCIONARIO] - Funcionário " + funcionarioId + " - Estação " + estacaoId + " não pode solicitar uma peça porque o tempo limite foi atingido");
    }
  }

  // Método para registrar uma mensagem no log
  private void log(String message) throws IOException {
    logFile.write(message + "\n");
    logFile.flush();
  }
}