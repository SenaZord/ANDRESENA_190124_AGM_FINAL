package com.mygdx.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.ScreenUtils;

import java.util.Random;

public class MyGdxGame extends ApplicationAdapter {

	private SpriteBatch batch;

	private Texture[] passaros;
	private Texture fundo;
	private Texture canoTopo;
	private Texture canoBaixo;
	private Texture gameOver;
	private	Texture logoAngryBirds;
	private Texture texturaMoedaOuro;
	private Texture texturaMoedaPrata;

	private int estadoJogo = 0;
	private int pontos = 0;
	private int pontuacaoMaxima = 0;

	private int gravidade = 2;
	private float variacao = 0;
	private float posicaoInicialVerticalPassaro = 0;
	private float posicaoCanoHorizontal;
	private float posicaoCanoVertical;
	private float posicaoHorizontalPassaro = 0;
	private float espacoEntreCanos;
	private float larguraDispositivo;
	private float alturaDispositivo;

	private boolean passouCano = false;
	private Random random;

	BitmapFont textoPontuacao;
	BitmapFont textoReiniciar;
	BitmapFont textoMelhorPontuacao;

	Preferences preferencias;

	private ShapeRenderer shapeRenderer;
	private Circle circuloPassaro;
	private Rectangle retanguloCanoCima;
	private Rectangle retanguloCanoBaixo;

	private Coin moedaAtual;
	private Coin moedaOuro;
	private Coin moedaPrata;
	private Circle circuloMoeda;
	private float posicaoMoedaVertical;
	private float posicaoMoedaHorizontal;
	private boolean pegouMoeda;

	Sound somVoando;
	Sound somColisao;
	Sound somPontuacao;
	Sound somMoeda;

	@Override
	public void create () {
		inicializaTextura();
		inicializaObjetos();
		criaMoeda();

	}

	private void inicializaTextura() {
		passaros = new Texture[3];

		passaros[0] = new Texture("AngryBird1.png");
		passaros[1] = new Texture("AngryBird2.png");
		passaros[2] = new Texture("AngryBird3.png");

		fundo = new Texture("fundo.png");

		canoBaixo = new Texture("cano_baixo_maior.png");
		canoTopo = new Texture("cano_topo_maior.png");
		gameOver = new Texture("game_over.png");
		logoAngryBirds = new Texture("Logo.png");
	}

	private void criaMoeda() {
		posicaoMoedaHorizontal= larguraDispositivo;
		posicaoMoedaVertical = random.nextInt((int) 200)+600;

		texturaMoedaOuro = new Texture("Moeda_Dourada.png");
		moedaOuro = new Coin(texturaMoedaOuro,10);

		texturaMoedaOuro = new Texture("Moeda_Prata.png");
		moedaPrata = new Coin(texturaMoedaOuro,5);

		moedaAtual = moedaOuro;
	}

	private void inicializaObjetos() {
		random = new Random();
		batch = new SpriteBatch();
		larguraDispositivo = Gdx.graphics.getWidth();
		alturaDispositivo = Gdx.graphics.getHeight();
		posicaoInicialVerticalPassaro = alturaDispositivo / 2;
		posicaoCanoHorizontal = larguraDispositivo;
		espacoEntreCanos = 350;

		textoPontuacao = new BitmapFont();
		textoPontuacao.setColor(Color.WHITE);
		textoPontuacao.getData().setScale(10);

		textoReiniciar = new BitmapFont();
		textoReiniciar.setColor(Color.GREEN);
		textoReiniciar.getData().setScale(3);

		textoMelhorPontuacao = new BitmapFont();
		textoMelhorPontuacao.setColor(Color.RED);
		textoMelhorPontuacao.getData().setScale(3);

		shapeRenderer = new ShapeRenderer();
		circuloPassaro = new Circle();
		retanguloCanoCima = new Rectangle();
		retanguloCanoBaixo = new Rectangle();
		circuloMoeda = new Circle();

		somVoando = Gdx.audio.newSound(Gdx.files.internal("som_asa.wav"));
		somColisao = Gdx.audio.newSound(Gdx.files.internal("som_batida.wav"));
		somPontuacao = Gdx.audio.newSound(Gdx.files.internal("som_pontos.wav"));
		somMoeda = Gdx.audio.newSound(Gdx.files.internal("MoedaSom.wav"));

		//pegando as Preferencias e declarando para cada um
		preferencias = Gdx.app.getPreferences("flappyBird");
		pontuacaoMaxima = preferencias.getInteger("pontuacaoMaxima", 0);
	}



	@Override
	public void render () {
		verificaEstadoJogo();
		validaPontos();
		desenhaTexturas();
		detectaColisao();
	}

	private void desenhaTexturas() {
		batch.begin();


		batch.draw(fundo, 0, 0, larguraDispositivo, alturaDispositivo);
		batch.draw(passaros[(int) variacao], 50 + posicaoHorizontalPassaro, posicaoInicialVerticalPassaro);
		batch.draw(canoBaixo, posicaoCanoHorizontal, alturaDispositivo / 2 - canoBaixo.getHeight() - espacoEntreCanos / 2 + posicaoCanoVertical);
		batch.draw(canoTopo,posicaoCanoHorizontal, alturaDispositivo / 2 + espacoEntreCanos / 2 + posicaoCanoVertical);
		textoPontuacao.draw(batch, String.valueOf(pontos), larguraDispositivo / 2, alturaDispositivo - 100);

		if(estadoJogo == 2)
		{
			batch.draw(gameOver, larguraDispositivo / 2 - gameOver.getWidth() / 2, alturaDispositivo / 2);
			textoReiniciar.draw(batch, "TOQUE NA TELA PARA REINICIAR!", larguraDispositivo / 2 - 200, alturaDispositivo / 2 - gameOver.getHeight() / 2);
			textoMelhorPontuacao.draw(batch, "SUA MELHOR PONTUAÇÃO É : " + pontuacaoMaxima +  "PONTOS!", larguraDispositivo / 2 - 300, alturaDispositivo / 2 - gameOver.getHeight() * 2);
		}else if(estadoJogo==0)
		{

			batch.draw(logoAngryBirds,posicaoMoedaHorizontal/2, alturaDispositivo / 2,500,500);
		}else if(!pegouMoeda){
			batch.draw(moedaAtual.texturaCoin,posicaoMoedaHorizontal,posicaoMoedaVertical,100,100);
		}

		batch.end();
	}

	private void verificaEstadoJogo() {

		boolean toqueTela = Gdx.input.justTouched();
		if(estadoJogo == 0)
		{

			if (toqueTela)
			{
				gravidade = -15;
				estadoJogo = 1;
				somVoando.play();
			}

		}
		else if (estadoJogo == 1)
		{
			if (toqueTela)
			{
				gravidade = -15;
				somVoando.play();
			}
			posicaoCanoHorizontal -= Gdx.graphics.getDeltaTime() * 200;
			posicaoMoedaHorizontal -= Gdx.graphics.getDeltaTime()*200;
			if(posicaoCanoHorizontal < -canoBaixo.getHeight())
			{
				posicaoCanoHorizontal = larguraDispositivo;
				posicaoCanoVertical = random.nextInt(400) - 200;
				passouCano = false;
			}
			if(posicaoMoedaHorizontal < -moedaAtual.texturaCoin.getHeight() || pegouMoeda)
			{
				posicaoMoedaHorizontal= larguraDispositivo;
				posicaoMoedaVertical = random.nextInt((int) 200)+600;
				int randomMoeda = random.nextInt(5);
				if(randomMoeda==1){
					moedaAtual = moedaOuro;
				}
				else{
					moedaAtual = moedaPrata;
				}
				pegouMoeda = false;
			}

			if (posicaoInicialVerticalPassaro > 0 || toqueTela)
				posicaoInicialVerticalPassaro = posicaoInicialVerticalPassaro - gravidade;

			gravidade++;
		}
		else if(estadoJogo == 2)
		{
			if(pontos > pontuacaoMaxima)
			{
				pontuacaoMaxima = pontos;
				preferencias.putInteger("pontuacaoMaxima", pontuacaoMaxima);
			}

			posicaoHorizontalPassaro -= Gdx.graphics.getDeltaTime() * 500;

			if(toqueTela)
			{
				estadoJogo = 0;
				pontos = 0;
				gravidade = 0;
				posicaoHorizontalPassaro = 0;
				posicaoInicialVerticalPassaro = alturaDispositivo /2;
				posicaoCanoHorizontal = larguraDispositivo;
			}
		}
	}

	private void validaPontos() {

		if(posicaoCanoHorizontal < 50 - passaros[0].getWidth())
		{
			if(!passouCano)
			{
				pontos++;
				passouCano = true;
				somPontuacao.play();
			}
		}
		variacao += Gdx.graphics.getDeltaTime() * 10;
		if (variacao > 3)
			variacao = 0;
	}

	private void detectaColisao() {

		circuloPassaro.set(50 + passaros[0].getWidth() / 2,
				posicaoInicialVerticalPassaro + passaros[0].getHeight() / 2, passaros[0].getWidth() / 2);
		retanguloCanoBaixo.set(posicaoCanoHorizontal,
				alturaDispositivo / 2 - canoBaixo.getHeight() - espacoEntreCanos / 2 + posicaoCanoVertical,
				canoBaixo.getWidth(), canoBaixo.getHeight());

		retanguloCanoCima.set(posicaoCanoHorizontal,
				alturaDispositivo / 2 + espacoEntreCanos + espacoEntreCanos / 2 + posicaoCanoVertical,
				canoTopo.getWidth(), canoTopo.getHeight());

		circuloMoeda.set(posicaoMoedaHorizontal*2,posicaoMoedaVertical + moedaAtual.texturaCoin.getHeight()/2,moedaAtual.texturaCoin.getWidth()/3);

		boolean bateuCanoCima = Intersector.overlaps(circuloPassaro, retanguloCanoCima);
		boolean bateuCanoBaixo = Intersector.overlaps(circuloPassaro, retanguloCanoBaixo);
		boolean bateuMoeda = Intersector.overlaps(circuloPassaro,circuloMoeda);


		if(bateuCanoBaixo || bateuCanoCima)
		{

			if(estadoJogo == 1)
			{
				somColisao.play();
				estadoJogo = 2;
			}
		}

		if(bateuMoeda && estadoJogo ==1 )
		{
			pontos+=moedaAtual.pontos;
			pegouMoeda = true;
			somMoeda.play();
		}
	}

	@Override
	public void dispose () {

	}
}
