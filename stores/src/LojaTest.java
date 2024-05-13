package src;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.io.IOException;
import java.util.Random;

public class LojaTest {
    
    public static void main(String[] args) {
        // Cria um pool de threads com 3 threads
        ExecutorService executorService = Executors.newFixedThreadPool(3);

        // Loop para criar 3 lojas
        for (int i = 1; i <= 3; i++) {
            // Cria uma variável final para usar no lambda
            int finalI = i;

            // Executa uma nova tarefa no pool de threads
            executorService.execute(() -> {
                try {
                    // Cria uma nova loja com o ID finalI
                    Loja loja = new Loja(finalI);
                    // Cria um novo servidor para a loja
                    LojaServer lojaServer = new LojaServer(loja, 8000 + finalI);
                    // Inicia o servidor em uma nova thread
                    new Thread(() -> {
                        try {
                            lojaServer.start();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }).start();
                    // Aguarda um pouco para o servidor iniciar
                    Thread.sleep(2000);
                    loja.connect(); // Conecta a loja ao servidor
                    // Loop infinito para comprar carros
                    while (true) {
                        // A loja tenta comprar um carro
                        loja.buyCar();
                        // A thread dorme por um tempo aleatório de até 1 segundo
                        Thread.sleep(new Random().nextInt(1000));
                    }
                } catch (IOException | InterruptedException e) {
                    // Imprime a pilha de chamadas se ocorrer uma exceção
                    e.printStackTrace();
                }
            });
        }
    }
}