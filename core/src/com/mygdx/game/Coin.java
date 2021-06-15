package com.mygdx.game;

import com.badlogic.gdx.graphics.Texture;
//classe de controla das moedas//
public class Coin {
    public Texture texturaCoin;
    public int pontos;

    public Coin(Texture texture, int pontos)
    {
        this.texturaCoin = texture;
        this.pontos = pontos;
    }
}
