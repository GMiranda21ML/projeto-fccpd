package br.com.cem.fccpd.parteum.repository;

import br.com.cem.fccpd.parteum.model.Monitor;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class MonitorRepository {

    private final Map<Long, Monitor> monitores = new ConcurrentHashMap<>();
    private final AtomicLong proximoId = new AtomicLong(1);

    public Monitor salvar(Monitor monitor) {
        if (monitor.getId() == null) {
            monitor.setId(proximoId.getAndIncrement());
        }
        monitores.put(monitor.getId(), monitor);
        return monitor;
    }

    public Monitor buscarPorId(Long id) {
        return monitores.get(id);
    }

    public Collection<Monitor> listarTodos() {
        return monitores.values();
    }
}
