package br.com.cem.fccpd.parteum.dto;

import java.time.LocalDateTime;

public class RequestsDTO {

    public record CriarMonitorRequest(String nome, String disciplina) {}

    public record CriarSlotRequest(LocalDateTime inicio, LocalDateTime fim) {}

    public record AgendarRequest(Long alunoId) {}
}