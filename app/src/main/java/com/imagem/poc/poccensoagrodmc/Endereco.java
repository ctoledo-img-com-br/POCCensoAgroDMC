package com.imagem.poc.poccensoagrodmc;

import com.esri.core.geometry.Point;

/**
 * Created by ctoledo on 17/03/2016.
 */
public class Endereco {
    private String nome;
    private Point point;

    public Endereco(String nome, Point point) {
        this.nome = nome;
        this.point = point;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public Point getPoint() {
        return point;
    }

    public void setPoint(Point point) {
        this.point = point;
    }

}
