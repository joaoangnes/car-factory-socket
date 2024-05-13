package src;

import java.io.*;
import java.net.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FabricaServer {
    private static final int PORT = 8000;
    private static Fabrica fabrica;

    public static void main(String[] args) throws IOException {
        fabrica = new Fabrica();
        fabrica.iniciarProducao();

        ExecutorService executorService = Executors.newFixedThreadPool(3);
        ServerSocket serverSocket = new ServerSocket(PORT);

        System.out.println("Server is listening on port " + PORT);

        while (true) {
            Socket socket = serverSocket.accept();
            System.out.println("New store connected");

            FabricaHandler fabricaHandler = new FabricaHandler(socket, fabrica);
            executorService.execute(fabricaHandler);
        }
    }
}

class FabricaHandler implements Runnable {
    private Socket socket;
    private Fabrica fabrica;

    public FabricaHandler(Socket socket, Fabrica fabrica) {
        this.socket = socket;
        this.fabrica = fabrica;
    }

    // Método para serializar um veículo
    private String serializeVeiculo(Veiculo veiculo) {
        return veiculo.getId() + "," + veiculo.getCor() + "," + veiculo.getTipo() + "," 
               + veiculo.getEstacaoId() + "," + veiculo.getFuncionarioId();
    }

    @Override
    public void run() {
        try {
            InputStream input = socket.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(input));

            OutputStream output = socket.getOutputStream();
            PrintWriter writer = new PrintWriter(output, true);

            String idLojaStr;
            while ((idLojaStr = reader.readLine()) != null) {
                int idLoja = Integer.parseInt(idLojaStr);
                
                while (true) {
                    Veiculo veiculo = null;
                    int posicaoEsteira = Integer.parseInt(reader.readLine());

                    synchronized (fabrica) {
                        try {
                            veiculo = fabrica.realizarVendaVeiculo();
                            if (veiculo != null) {
                                fabrica.registrarVenda(veiculo, idLoja, posicaoEsteira);
                            }
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                    if (veiculo != null) {
                        String veiculoStr = serializeVeiculo(veiculo);
                        writer.println(veiculoStr);
                    } else {
                        writer.println("Não foi possível comprar o veículo");
                        break;
                    }
                }
            }
            socket.close();
        } catch (IOException e) {
            System.out.println("Erro no cliente: " + e.getMessage());
        }
    }
}