package br.com.cem.fccpd.parteum.model;

import java.time.LocalDateTime;

public class Agendamento {
    private Long id;
    private Long slotId;
    private Long alunoId;
    private LocalDateTime dataHoraCriacao;

    public Agendamento() {}

    public Agendamento(Long id, Long slotId, Long alunoId) {
        this.id = id;
        this.slotId = slotId;
        this.alunoId = alunoId;
        this.dataHoraCriacao = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getSlotId() {
        return slotId;
    }

    public void setSlotId(Long slotId) {
        this.slotId = slotId;
    }

    public Long getAlunoId() {
        return alunoId;
    }

    public void setAlunoId(Long alunoId) {
        this.alunoId = alunoId;
    }

    public LocalDateTime getDataHoraCriacao() {
        return dataHoraCriacao;
    }

    public void setDataHoraCriacao(LocalDateTime dataHoraCriacao) {
        this.dataHoraCriacao = dataHoraCriacao;
    }
}