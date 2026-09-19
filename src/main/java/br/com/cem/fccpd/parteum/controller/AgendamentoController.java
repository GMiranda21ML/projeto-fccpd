package br.com.cem.fccpd.parteum.controller;

import br.com.cem.fccpd.parteum.dto.RequestsDTO.AgendarRequest;
import br.com.cem.fccpd.parteum.exception.SlotIndisponivelException;
import br.com.cem.fccpd.parteum.model.Agendamento;
import br.com.cem.fccpd.parteum.repository.AgendamentoRepository;
import br.com.cem.fccpd.parteum.service.AgendamentoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;

@RestController
public class AgendamentoController {

    private final AgendamentoService agendamentoService;
    private final AgendamentoRepository agendamentoRepository;

    public AgendamentoController(AgendamentoService agendamentoService,
                                 AgendamentoRepository agendamentoRepository) {
        this.agendamentoService = agendamentoService;
        this.agendamentoRepository = agendamentoRepository;
    }

    @PostMapping("/slots/{slotId}/agendar")
    public ResponseEntity<?> agendar(@PathVariable Long slotId,
                                     @RequestBody AgendarRequest request) {
        try {
            Agendamento agendamento = agendamentoService.agendar(slotId, request.alunoId());
            return ResponseEntity.status(HttpStatus.CREATED).body(agendamento);
        } catch (SlotIndisponivelException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }

    @GetMapping("/agendamentos")
    public Collection<Agendamento> listar() {
        return agendamentoRepository.listarTodos();
    }
}
