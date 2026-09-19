package br.com.cem.fccpd.parteum.controller;

import br.com.cem.fccpd.parteum.dto.RequestsDTO.CriarSlotRequest;
import br.com.cem.fccpd.parteum.model.Monitor;
import br.com.cem.fccpd.parteum.model.Slot;
import br.com.cem.fccpd.parteum.repository.MonitorRepository;
import br.com.cem.fccpd.parteum.repository.SlotRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/monitores/{monitorId}/slots")
public class SlotController {

    private final SlotRepository slotRepository;
    private final MonitorRepository monitorRepository;

    public SlotController(SlotRepository slotRepository, MonitorRepository monitorRepository) {
        this.slotRepository = slotRepository;
        this.monitorRepository = monitorRepository;
    }

    @PostMapping
    public ResponseEntity<Slot> cadastrar(@PathVariable Long monitorId,
                                          @RequestBody CriarSlotRequest request) {
        Monitor monitor = monitorRepository.buscarPorId(monitorId);
        if (monitor == null) {
            return ResponseEntity.notFound().build();
        }

        Slot slot = new Slot(null, monitorId, request.inicio(), request.fim());
        Slot salvo = slotRepository.salvar(slot);
        return ResponseEntity.status(HttpStatus.CREATED).body(salvo);
    }

    @GetMapping
    public Collection<Slot> listarDoMonitor(@PathVariable Long monitorId) {
        List<Slot> todos = slotRepository.listarTodos().stream()
                .filter(slot -> slot.getMonitorId().equals(monitorId))
                .collect(Collectors.toList());
        return todos;
    }
}
