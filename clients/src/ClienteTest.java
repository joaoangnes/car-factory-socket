package src;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.io.IOException;
import java.util.Random;

public class ClienteTest {
  private static int NUMBER_OF_CLIENTS = 20;

  public static void main(String[] args) {
    Map<Integer, String> lojaEnderecos = new HashMap<>();
    lojaEnderecos.put(8001, "localhost" ); // Substitua por endereços reais das lojas e portas correspondentes
    lojaEnderecos.put(8002,"localhost");
    lojaEnderecos.put(8003,"localhost");
    // Cria um pool de threads com 20 threads
    ExecutorService executorService = Executors.newFixedThreadPool(NUMBER_OF_CLIENTS);

    // Loop para criar NUMBER_OF_CLIENTS clientes
    for (int i = 1; i <= NUMBER_OF_CLIENTS; i++) {
      // Cria uma variável final para usar no lambda
      int finalI = i;

      // Executa uma nova tarefa no pool de threads
      executorService.execute(() -> {
        try {
          // Cria um novo cliente com o ID finalI
          Cliente cliente = new Cliente(finalI, lojaEnderecos);

          // Loop infinito para o cliente continuar comprando veículos
          while (true) {
            Thread.sleep(new Random().nextInt(2000));
            cliente.buyCar(); // O cliente tenta comprar um carro
            Thread.sleep(2000); // Espera 2 segundos antes da próxima compra
          }
        } catch (IOException | InterruptedException e) {
          // Imprime a pilha de chamadas se ocorrer uma exceção
          e.printStackTrace();
        }
      });
    }

    // Desliga o executorService após a conclusão de todas as tarefas
    // executorService.shutdown();
  }
}