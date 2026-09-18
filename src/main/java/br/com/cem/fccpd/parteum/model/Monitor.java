package br.com.cem.fccpd.parteum.model;

public class Monitor {
    private Long id;
    private String nome;
    private String disciplina;

    public Monitor() {}

    public Monitor(Long id, String nome, String disciplina) {
        this.id = id;
        this.nome = nome;
        this.disciplina = disciplina;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getDisciplina() {
        return disciplina;
    }

    public void setDisciplina(String disciplina) {
        this.disciplina = disciplina;
    }
}
