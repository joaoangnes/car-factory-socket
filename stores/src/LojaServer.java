package src;

import java.net.*;
import java.io.*;

public class LojaServer {
    private int port; // Porta do servidor da loja
    private Loja loja;

    public LojaServer(Loja loja, int port) {
        this.loja = loja;
        this.port = port;
    }

    public void start() throws IOException {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Servidor da Loja iniciado na porta " + port);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                // System.out.println("New clients connected");
                new LojaClientHandler(clientSocket, loja).start();
            }
        }
    }
}

class LojaClientHandler extends Thread {
    private Socket clientSocket;
    private Loja loja;

    public LojaClientHandler(Socket socket, Loja loja) {
        this.clientSocket = socket;
        this.loja = loja;
    }

    // Método para serializar um veículo
    private String serializeVeiculo(Veiculo veiculo) {
        return veiculo.getId() + "," + veiculo.getCor() + "," + veiculo.getTipo() + "," 
            + veiculo.getEstacaoId() + "," + veiculo.getFuncionarioId();
    }

    @Override
    public void run() {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            PrintWriter writer = new PrintWriter(clientSocket.getOutputStream(), true);
            
            String request;
            while ((request = reader.readLine()) != null) {
                Veiculo veiculo = null;
                String[] parts = request.split(" ");
                if (parts[0].equals("buy")) {
                    if (parts.length < 3) {
                        writer.println("Formato de solicitação inválido. Esperado: 'buy <clientId> <posicaoGaragem>'");
                        continue;
                    }
                    try {
                        int clientId = Integer.parseInt(parts[1]); // Lê o ID do cliente da requisição
                        int posicaoGaragem = Integer.parseInt(parts[2]); // Lê a posição da garagem do cliente
                        System.out.println("Cliente " + clientId + " está comprando um carro da loja "+ this.loja.getId() + ", armazenado na posição da garagem: "+ posicaoGaragem); // Log do ID do cliente
                        synchronized (loja) {
                            try {
                                veiculo = loja.realizarVendaVeiculo();
                                if (veiculo != null) {
                                    loja.registrarVenda(veiculo, clientId, posicaoGaragem);
                                }
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    } catch (NumberFormatException e) {
                        writer.println("ID do cliente e posição da garagem devem ser números inteiros");
                    }

                    if (veiculo != null) {
                        String veiculoStr = serializeVeiculo(veiculo);
                        writer.println(veiculoStr);
                    } else {
                        writer.println("Não foi possível comprar o veículo");
                        break;
                    }
                } else {
                    writer.println("Comando desconhecido: " + request);
                }
            }
    
            clientSocket.close();
        } catch (IOException e) {
            System.out.println("Erro no cliente: " + e.getMessage());
        }
    }
}