package src;

import java.io.FileWriter;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Loja {
    private static final String SERVER_IP = "localhost"; // IP do servidor da fábrica
    private static final int SERVER_PORT = 8000; // Porta do servidor da fábrica

      
    private int id;
    private Socket socket;
    private BufferedReader reader;
    private PrintWriter writer;
    private final FileWriter logFile; // Arquivo de log para registrar as ações
    private final FileWriter vendaLogFile; // Arquivo de log para registrar as vendas
    private final EsteiraLoja esteira; // Esteira onde os veículos produzidos são colocados

    // Constantes para definir o tamanho da esteira
    public static final int TAMANHO_ESTEIRA = 500; // Capacidade da esteira

    // Construtor da loja
    public Loja(int id) throws IOException {
        this.id = id;
        this.logFile = new FileWriter("loja_" + id + "_log.txt");
        this.vendaLogFile = new FileWriter("vendas_loja_" + id + ".txt");
        this.esteira = new EsteiraLoja(TAMANHO_ESTEIRA, logFile);
    }

    // Método para conectar com o servidor da fábrica para efetuar as compras
    public void connect() throws IOException {
        socket = new Socket(SERVER_IP, SERVER_PORT);
        reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        writer = new PrintWriter(socket.getOutputStream(), true);

        // Envia o ID da loja para o servidor
        writer.println(id);
    }

    // Método para comprar um carro
    public synchronized void buyCar() throws IOException {
        // Envia a posição da esteira para o servidor
        writer.println(esteira.getEntrada());

        // Aguarda a resposta do servidor
        String response = reader.readLine();
        while (response == null || response.equals("Não foi possível comprar o veículo")) {
            System.out.println("Aguardando a produção de um carro...");
            try {
                Thread.sleep(1000); // Espera um segundo antes de tentar novamente
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            writer.println(esteira.getEntrada()); // Tenta comprar um carro novamente
            response = reader.readLine();
        }
        
        Veiculo veiculo = deserializeVeiculo(response);
        // System.out.println("[Loja " + this.id + "] Carro comprado: " + veiculo.getId() + " | Posição na esteira: " + esteira.getEntrada());
        // Caso tenha conseguido comprar o veículo, tenta estocá-lo na esteira
        try {
            esteira.estocarVeiculo(veiculo);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
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
    public void registrarVenda(Veiculo veiculo, int idCliente, int posicaoGaragem) {
      try { 
        this.vendaLogFile.write(String.format("[VENDA] - Venda realizada: ID do Cliente: %d, Posição da garagem: %d, Informações do veículo: %s\n", idCliente, posicaoGaragem, "(ID: " + veiculo.getId() + ", Cor: " + veiculo.getCor() +
                                                                                                                                                                                      ", Tipo: " + veiculo.getTipo() + ", Estação: " + veiculo.getEstacaoId() +
                                                                                                                                                                                      ", Funcionário: " + veiculo.getFuncionarioId() + ")"));
        // this.vendaLogFile.write("--------------------------------------------------\n");
        this.vendaLogFile.flush();
      } catch (IOException e) {
        System.out.println("Erro ao registrar a venda: " + e.getMessage());
      }
    }

    // Método para serializar um veículo
    private Veiculo deserializeVeiculo(String str) {
      String[] parts = str.split(",");
      int id = Integer.parseInt(parts[0]);
      String cor = parts[1];
      String tipo = parts[2];
      int estacaoId = Integer.parseInt(parts[3]);
      int funcionarioId = Integer.parseInt(parts[4]);
      return new Veiculo(id, cor, tipo, estacaoId, funcionarioId);
    }

    public static String getServerIp() {
      return SERVER_IP;
    }

    public static int getServerPort() {
      return SERVER_PORT;
    }

    public int getId() {
      return id;
    }

    public Socket getSocket() {
      return socket;
    }

    public BufferedReader getReader() {
      return reader;
    }

    public PrintWriter getWriter() {
      return writer;
    }

    public FileWriter getLogFile() {
      return logFile;
    }

    public FileWriter getVendaLogFile() {
      return vendaLogFile;
    }

    public EsteiraLoja getEsteira() {
      return esteira;
    }

    public static int getTamanhoEsteira() {
      return TAMANHO_ESTEIRA;
    }
}