package br.com.cem.fccpd.parteum.repository;

import br.com.cem.fccpd.parteum.model.Slot;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class SlotRepository {

    private final Map<Long, Slot> slots = new ConcurrentHashMap<>();
    private final AtomicLong proximoId = new AtomicLong(1);

    public Slot salvar(Slot slot) {
        if (slot.getId() == null) {
            slot.setId(proximoId.getAndIncrement());
        }
        slots.put(slot.getId(), slot);
        return slot;
    }

    public Slot buscarPorId(Long id) {
        return slots.get(id);
    }

    public Collection<Slot> listarTodos() {
        return slots.values();
    }
}
