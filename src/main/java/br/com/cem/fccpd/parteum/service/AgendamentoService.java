package br.com.cem.fccpd.parteum.service;

import br.com.cem.fccpd.parteum.exception.SlotIndisponivelException;
import br.com.cem.fccpd.parteum.model.Agendamento;
import br.com.cem.fccpd.parteum.model.Slot;
import br.com.cem.fccpd.parteum.model.StatusSlot;
import br.com.cem.fccpd.parteum.repository.AgendamentoRepository;
import br.com.cem.fccpd.parteum.repository.SlotRepository;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

@Service
public class AgendamentoService {

    private final SlotRepository slotRepository;
    private final AgendamentoRepository agendamentoRepository;
    private final Map<Long, ReentrantLock> locksPorSlot = new ConcurrentHashMap<>();

    public AgendamentoService(SlotRepository slotRepository,
                              AgendamentoRepository agendamentoRepository) {
        this.slotRepository = slotRepository;
        this.agendamentoRepository = agendamentoRepository;
    }

    public Agendamento agendar(Long slotId, Long alunoId) {
        ReentrantLock lock = obterLockDoSlot(slotId);

        lock.lock();
        try {
            Slot slot = slotRepository.buscarPorId(slotId);

            if (slot == null) {
                throw new SlotIndisponivelException("Slot " + slotId + " não existe.");
            }

            if (slot.getStatus() != StatusSlot.DISPONIVEL) {
                throw new SlotIndisponivelException(
                        "Slot " + slotId + " já está reservado.");
            }

            slot.setStatus(StatusSlot.RESERVADO);
            slotRepository.salvar(slot);
            Agendamento agendamento = new Agendamento(null, slotId, alunoId);
            return agendamentoRepository.salvar(agendamento);

        } finally {
            lock.unlock();
        }
    }

    private ReentrantLock obterLockDoSlot(Long slotId) {
        return locksPorSlot.computeIfAbsent(slotId, id -> new ReentrantLock());
    }
}
