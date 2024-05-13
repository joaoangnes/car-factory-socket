package src;

// Classe para gerar IDs únicos
class IdGenerator {
    private static int nextId = 1;

    public static synchronized int nextId() {
        return nextId++;
    }
}