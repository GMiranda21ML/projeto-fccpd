package br.com.cem.fccpd.parteum.teste;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Teste de carga "fora do Spring": dispara N requisições HTTP reais,
 * simultâneas, contra a mesma rota de agendamento, e confere que
 * exatamente UMA delas é aceita (double booking evitado).
 *
 * Como rodar:
 *   1. Suba a aplicação Spring Boot normalmente (porta 8080).
 *   2. Rode esta classe separadamente (outro terminal / outra Run
 *      Configuration na IDE). Ela mesma cria o monitor e o slot antes
 *      de disparar a corrida.
 *
 * Por que Virtual Threads aqui: cada requisição HTTP fica bloqueada
 * esperando resposta de rede (I/O). Isso é exatamente o cenário onde
 * Virtual Threads brilham (Aula 4) — conseguimos disparar centenas de
 * chamadas "simultâneas" sem o custo de centenas de threads de SO.
 */
public class TesteConcorrenciaAgendamento {

    private static final String BASE_URL = "http://localhost:8080";
    private static final int QUANTIDADE_ALUNOS = 50;

    private static final HttpClient httpClient = HttpClient.newHttpClient();

    // Fila de log thread-safe: cada "aluno" registra aqui o instante em
    // que enviou o pedido e o instante em que recebeu a resposta. Como
    // várias virtual threads escrevem ao mesmo tempo, imprimir direto
    // no console (System.out) causaria linhas embaralhadas — por isso
    // guardamos tudo aqui e só imprimimos, em ordem, no final.
    private static final ConcurrentLinkedQueue<LogEntry> logs = new ConcurrentLinkedQueue<>();

    private record LogEntry(long instanteNanos, long alunoId, String evento) {
    }

    public static void main(String[] args) throws Exception {
        System.out.println("== Preparando cenário ==");
        long monitorId = criarMonitor();
        long slotId = criarSlot(monitorId);
        System.out.println("Monitor id=" + monitorId + " | Slot id=" + slotId);

        System.out.println("\n== Disparando " + QUANTIDADE_ALUNOS
                + " tentativas de agendamento SIMULTÂNEAS para o mesmo slot ==");
        List<Integer> statusCodes = dispararAgendamentosConcorrentes(slotId, QUANTIDADE_ALUNOS);

        long sucessos = statusCodes.stream().filter(s -> s == 201).count();
        long conflitos = statusCodes.stream().filter(s -> s == 409).count();
        long outros = statusCodes.size() - sucessos - conflitos;

        System.out.println("\n== Resultado ==");
        System.out.println("201 Created (conseguiu agendar): " + sucessos);
        System.out.println("409 Conflict (chegou tarde):      " + conflitos);
        System.out.println("Outros códigos inesperados:       " + outros);

        if (sucessos == 1 && conflitos == QUANTIDADE_ALUNOS - 1) {
            System.out.println("\n✅ SUCESSO: exatamente 1 agendamento confirmado, sem double booking.");
        } else {
            System.out.println("\n❌ FALHA: esperado exatamente 1 sucesso — a proteção de concorrência falhou!");
        }
    }

    /**
     * Dispara as requisições concorrentemente usando Virtual Threads.
     * Um CyclicBarrier garante que todas as threads fiquem "prontas para
     * disparar" antes de qualquer uma enviar a requisição — isso
     * maximiza a chance real de colisão no mesmo instante, em vez de
     * enviarem uma de cada vez em sequência.
     */
    private static List<Integer> dispararAgendamentosConcorrentes(long slotId, int quantidade)
            throws InterruptedException {

        CyclicBarrier largada = new CyclicBarrier(quantidade);
        AtomicInteger contadorAlunos = new AtomicInteger(1);
        long instanteReferencia = System.nanoTime(); // "t=0" para os logs

        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            List<Future<Integer>> futuros = IntStream.range(0, quantidade)
                    .mapToObj(i -> executor.submit(() -> {
                        long alunoId = contadorAlunos.getAndIncrement();
                        largada.await(); // espera todo mundo estar pronto

                        logs.add(new LogEntry(
                                System.nanoTime() - instanteReferencia,
                                alunoId,
                                "ENVIOU o pedido de agendamento"));

                        int status = enviarPedidoDeAgendamento(slotId, alunoId);

                        logs.add(new LogEntry(
                                System.nanoTime() - instanteReferencia,
                                alunoId,
                                "RECEBEU resposta HTTP " + status
                                        + (status == 201 ? " (conseguiu!)" : " (negado)")));

                        return status;
                    }))
                    .collect(Collectors.toList());

            List<Integer> resultados = futuros.stream()
                    .map(TesteConcorrenciaAgendamento::obterResultado)
                    .collect(Collectors.toList());

            imprimirLogEmOrdem();
            return resultados;
        }
    }

    /**
     * Imprime, em ordem cronológica real (por nanoTime), cada pedido
     * enviado e cada resposta recebida. É essa saída que mostra "os
     * alunos pedindo ao mesmo tempo" na prática.
     */
    private static void imprimirLogEmOrdem() {
        System.out.println("\n== Linha do tempo das requisições (ordem real de chegada) ==");
        logs.stream()
                .sorted(Comparator.comparingLong(LogEntry::instanteNanos))
                .forEach(log -> {
                    double milissegundos = log.instanteNanos() / 1_000_000.0;
                    System.out.printf(
                            "[t=%8.3f ms] Aluno %-3d -> %s%n",
                            milissegundos, log.alunoId(), log.evento());
                });
        System.out.println();
    }

    private static Integer obterResultado(Future<Integer> futuro) {
        try {
            return futuro.get();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static int enviarPedidoDeAgendamento(long slotId, long alunoId) throws Exception {
        String corpo = "{\"alunoId\": " + alunoId + "}";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/slots/" + slotId + "/agendar"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(corpo))
                .timeout(Duration.ofSeconds(5))
                .build();

        HttpResponse<String> response =
                httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        return response.statusCode();
    }

    // ---- Métodos auxiliares para preparar o cenário (monitor + slot) ----

    private static long criarMonitor() throws Exception {
        String corpo = "{\"nome\": \"Monitor de Teste\", \"disciplina\": \"Cálculo I\"}";
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/monitores"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(corpo))
                .build();

        HttpResponse<String> response =
                httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        return extrairId(response.body());
    }

    private static long criarSlot(long monitorId) throws Exception {
        LocalDateTime inicio = LocalDateTime.now().plusDays(1).withHour(14).withMinute(0);
        LocalDateTime fim = inicio.plusHours(1);

        String corpo = String.format(
                "{\"inicio\": \"%s\", \"fim\": \"%s\"}", inicio, fim);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/monitores/" + monitorId + "/slots"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(corpo))
                .build();

        HttpResponse<String> response =
                httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        return extrairId(response.body());
    }

    /**
     * Extração bem simples do campo "id" do JSON de resposta, só para
     * não precisar adicionar uma dependência de parser JSON neste
     * utilitário de teste. Funciona porque sabemos exatamente o
     * formato retornado pelos nossos próprios controllers.
     */
    private static long extrairId(String json) {
        String chave = "\"id\":";
        int inicio = json.indexOf(chave) + chave.length();
        int fim = json.indexOf(',', inicio);
        if (fim == -1) {
            fim = json.indexOf('}', inicio);
        }
        return Long.parseLong(json.substring(inicio, fim).trim());
    }
}