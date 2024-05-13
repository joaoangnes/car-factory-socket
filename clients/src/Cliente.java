package src;

import java.io.FileWriter;
import java.io.BufferedReader;
import java.io.IOException;
import java.net.SocketException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class Cliente extends Thread {
    private int id;
    Map<Integer, String> lojaEnderecos; // Mapa de endereços das lojas para portas
    private Random random;
    private Socket socket;
    private BufferedReader reader;
    private PrintWriter writer;
    private final FileWriter logFile; // Arquivo de log para registrar as ações
    private List<Veiculo> carrosComprados; // Lista para armazenar os carros comprados
    private int posicaoGaragem; // Posição da garagem onde o carro será alocado

    public Cliente(int id, Map<Integer, String> lojaEnderecos) throws IOException {
        this.id = id;
        this.lojaEnderecos = lojaEnderecos;
        this.random = new Random();
        this.logFile = new FileWriter("cliente_" + id + "_log.txt");
        this.carrosComprados = new ArrayList<>();
        this.posicaoGaragem = 0;
    }

    // Método para registrar uma mensagem no arquivo de log
    private void log(String message) throws IOException {
        logFile.write(message + "\n");
        logFile.flush();
    }

    public void connect(String ip, int port) throws IOException {
        socket = new Socket(ip, port);
        reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        writer = new PrintWriter(socket.getOutputStream(), true);
    }

    // Método para comprar um carro
    public synchronized void buyCar() throws IOException {
        // Decide quantos carros comprar de forma aleatória
        int numCarros = random.nextInt(5) + 1; // Compra entre 1 e 5 carros

        for (int i = 0; i < numCarros; i++) {
            try {
                // Converte o conjunto de chaves para uma lista
                List<Integer> portas = new ArrayList<>(lojaEnderecos.keySet());
                // Seleciona um índice aleatório
                int index = random.nextInt(portas.size());
                // Usa o índice para obter uma porta de loja aleatória
                int lojaPorta = portas.get(index);
    
                String lojaEndereco = lojaEnderecos.get(lojaPorta);
                connect(lojaEndereco, lojaPorta);
                //socket = new Socket(lojaEndereco, lojaPorta);
                //reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                // writer = new PrintWriter(socket.getOutputStream(), true);
    
                // Envia a requisição de compra para o servidor
                String request = "buy " + this.id + " " + this.posicaoGaragem;
                writer.println(request);
                // Aguarda a resposta do servidor
                String response = reader.readLine();
    
                while (response == null || response.equals("Não foi possível comprar o veículo")) {
                    // Se não há veículos disponíveis, espera até que um esteja disponível
                    System.out.println("Cliente " + this.id + " aguardando um carro disponivel para compra...");
                    try {
                        Thread.sleep(3000); // Espera um segundo antes de tentar novamente
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    // Tenta reconectar com a loja novamente
                    connect(lojaEndereco, lojaPorta);
                    // Tenta comprar o veículo novamente
                    writer.println(request);
                    response = reader.readLine();
                }
    
                if (response.startsWith("Comando desconhecido: ")) {
                    System.err.println(response);
                } else {
                    Veiculo veiculo = deserializeVeiculo(response);
                    carrosComprados.add(veiculo); // Adiciona o carro comprado à lista
                    log("[GARAGEM]  - Veículo foi adicionado na garagem do cliente na posição " + posicaoGaragem + " (ID: " + veiculo.getId() + ", Cor: " + veiculo.getCor() +
                        ", Tipo: " + veiculo.getTipo() + ", Estação: " + veiculo.getEstacaoId() +
                        ", Funcionário: " + veiculo.getFuncionarioId() + ")");
                    posicaoGaragem++; // Incrementa a posição da garagem
                }
    
                // Fecha a conexão com a loja
                socket.close();
            } catch (SocketException e) {
                System.out.println("Connection reset. Trying to reconnect...");
                i--; // Try to buy the car again
            }
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

    @Override
    public void run() {
        try {
            buyCar();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}