package src;

// Classe que representa um veículo produzido na fábrica
class Veiculo {
  private final int id;
  private final String cor;
  private final String tipo;
  private final int estacaoId;
  private final int funcionarioId;

  // Construtor do veículo
  public Veiculo(int id, String cor, String tipo, int estacaoId, int funcionarioId) {
      this.id = id;
      this.cor = cor;
      this.tipo = tipo;
      this.estacaoId = estacaoId;
      this.funcionarioId = funcionarioId;
  }

  // Getters para as propriedades do veículo
  public int getId() {
      return id;
  }

  public String getCor() {
      return cor;
  }

  public String getTipo() {
      return tipo;
  }

  public int getEstacaoId() {
      return estacaoId;
  }

  public int getFuncionarioId() {
      return funcionarioId;
  }
}