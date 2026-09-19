package br.com.cem.fccpd.parteum.controller;

import br.com.cem.fccpd.parteum.dto.RequestsDTO.CriarMonitorRequest;
import br.com.cem.fccpd.parteum.model.Monitor;
import br.com.cem.fccpd.parteum.repository.MonitorRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;

@RestController
@RequestMapping("/monitores")
public class MonitorController {

    private final MonitorRepository monitorRepository;

    public MonitorController(MonitorRepository monitorRepository) {
        this.monitorRepository = monitorRepository;
    }

    @PostMapping
    public ResponseEntity<Monitor> cadastrar(@RequestBody CriarMonitorRequest request) {
        Monitor monitor = new Monitor(null, request.nome(), request.disciplina());
        Monitor salvo = monitorRepository.salvar(monitor);
        return ResponseEntity.status(HttpStatus.CREATED).body(salvo);
    }

    @GetMapping
    public Collection<Monitor> listar() {
        return monitorRepository.listarTodos();
    }
}
