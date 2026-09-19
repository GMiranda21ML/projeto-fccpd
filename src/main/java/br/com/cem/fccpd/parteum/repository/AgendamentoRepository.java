package br.com.cem.fccpd.parteum.repository;

import br.com.cem.fccpd.parteum.model.Agendamento;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class AgendamentoRepository {

    private final Map<Long, Agendamento> agendamentos = new ConcurrentHashMap<>();
    private final AtomicLong proximoId = new AtomicLong(1);

    public Agendamento salvar(Agendamento agendamento) {
        if (agendamento.getId() == null) {
            agendamento.setId(proximoId.getAndIncrement());
        }
        agendamentos.put(agendamento.getId(), agendamento);
        return agendamento;
    }

    public Collection<Agendamento> listarTodos() {
        return agendamentos.values();
    }
}
