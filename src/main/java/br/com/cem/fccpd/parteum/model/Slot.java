package br.com.cem.fccpd.parteum.model;

import java.time.LocalDateTime;

public class Slot {
    private Long id;
    private Long monitorId;
    private LocalDateTime inicio;
    private LocalDateTime fim;
    private StatusSlot status;

    public Slot() {}

    public Slot(Long id, Long monitorId, LocalDateTime inicio, LocalDateTime fim) {
        this.id = id;
        this.monitorId = monitorId;
        this.inicio = inicio;
        this.fim = fim;
        this.status = StatusSlot.DISPONIVEL;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getMonitorId() {
        return monitorId;
    }

    public void setMonitorId(Long monitorId) {
        this.monitorId = monitorId;
    }

    public LocalDateTime getInicio() {
        return inicio;
    }

    public void setInicio(LocalDateTime inicio) {
        this.inicio = inicio;
    }

    public LocalDateTime getFim() {
        return fim;
    }

    public void setFim(LocalDateTime fim) {
        this.fim = fim;
    }

    public StatusSlot getStatus() {
        return status;
    }

    public void setStatus(StatusSlot status) {
        this.status = status;
    }
}
