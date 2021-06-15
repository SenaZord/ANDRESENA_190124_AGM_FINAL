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

	//Variaveis de sprite
	private SpriteBatch batch;
	private Texture[] passaros;
	private Texture fundo;
	private Texture canoTopo;
	private Texture canoBaixo;
	private Texture gameOver;
	private	Texture logoAngryBirds;
	private Texture texturaMoedaOuro;
	private Texture texturaMoedaPrata;

	//Variaveis de estado do jogo e controle de pontos//
	private int estadoJogo = 0;
	private int pontos = 0;
	private int pontuacaoMaxima = 0;

	//Variaves de posicionamento, gravidade e variações entre posições//
	private int gravidade = 2;
	private float variacao = 0;
	private float posicaoInicialVerticalPassaro = 0;
	private float posicaoCanoHorizontal;
	private float posicaoCanoVertical;
	private float posicaoHorizontalPassaro = 0;
	private float espacoEntreCanos;
	private float larguraDispositivo;
	private float alturaDispositivo;

	//Variavel aleatoria e confirmaçõa de passar ao cano//
	private boolean passouCano = false;
	private Random random;

	//variaveis de formatação de texto//
	BitmapFont textoPontuacao;
	BitmapFont textoReiniciar;
	BitmapFont textoMelhorPontuacao;

	Preferences preferencias;

	//variaveis de colisão//
	private ShapeRenderer shapeRenderer;
	private Circle circuloPassaro;
	private Rectangle retanguloCanoCima;
	private Rectangle retanguloCanoBaixo;

	//variaveis referentes a moeda//
	private Coin moedaAtual;
	private Coin moedaOuro;
	private Coin moedaPrata;
	private Circle circuloMoeda;
	private float posicaoMoedaVertical;
	private float posicaoMoedaHorizontal;
	private boolean pegouMoeda;

	//variaveis de som//
	Sound somVoando;
	Sound somColisao;
	Sound somPontuacao;
	Sound somMoeda;

	//Criando os elementos do jogo//
	@Override
	public void create () {

		//Inicializando as texturas//
		inicializaTextura();

		//Inicializando os objetos//
		inicializaObjetos();

		//Criando moedas//
		criaMoeda();

	}

	private void inicializaTextura() {

		passaros = new Texture[3];

		//Array pegando sprite para animação do personagem//
		passaros[0] = new Texture("AngryBird1.png");
		passaros[1] = new Texture("AngryBird2.png");
		passaros[2] = new Texture("AngryBird3.png");

		//Imagem de fundo na tela//
		fundo = new Texture("fundo.png");

		//setando imagens dos canos, logo e Game over//
		canoBaixo = new Texture("cano_baixo_maior.png");
		canoTopo = new Texture("cano_topo_maior.png");
		gameOver = new Texture("game_over.png");
		logoAngryBirds = new Texture("Logo.png");
	}

	private void criaMoeda() {
		//Pegando posição da moeda com relação ao dispositivo//
		posicaoMoedaHorizontal= larguraDispositivo;
		posicaoMoedaVertical = random.nextInt((int) 200)+600;

		//Setando texturas das moedas e seus valores em pontos//
		texturaMoedaOuro = new Texture("Moeda_Dourada.png");
		moedaOuro = new Coin(texturaMoedaOuro,10);

		texturaMoedaPrata = new Texture("Moeda_Prata.png");
		moedaPrata = new Coin(texturaMoedaPrata,5);

		moedaAtual = moedaOuro;
	}

	private void inicializaObjetos() {
		//Pegando os espaços entre os canos e a ordem em que virão//
		random = new Random();
		batch = new SpriteBatch();
		larguraDispositivo = Gdx.graphics.getWidth();
		alturaDispositivo = Gdx.graphics.getHeight();
		posicaoInicialVerticalPassaro = alturaDispositivo / 2;
		posicaoCanoHorizontal = larguraDispositivo;
		espacoEntreCanos = 350;

		//Configurando Textos de pontuação//
		textoPontuacao = new BitmapFont();
		textoPontuacao.setColor(Color.WHITE);
		textoPontuacao.getData().setScale(10);

		//Configurando Texto Reiniciar//
		textoReiniciar = new BitmapFont();
		textoReiniciar.setColor(Color.GREEN);
		textoReiniciar.getData().setScale(3);

		//Configurando texto melhorPontuação//
		textoMelhorPontuacao = new BitmapFont();
		textoMelhorPontuacao.setColor(Color.RED);
		textoMelhorPontuacao.getData().setScale(3);

		//Setando formatos//
		shapeRenderer = new ShapeRenderer();
		circuloPassaro = new Circle();
		retanguloCanoCima = new Rectangle();
		retanguloCanoBaixo = new Rectangle();
		circuloMoeda = new Circle();

		//Setando sons//
		somVoando = Gdx.audio.newSound(Gdx.files.internal("som_asa.wav"));
		somColisao = Gdx.audio.newSound(Gdx.files.internal("som_batida.wav"));
		somPontuacao = Gdx.audio.newSound(Gdx.files.internal("som_pontos.wav"));
		somMoeda = Gdx.audio.newSound(Gdx.files.internal("MoedaSom.wav"));

		//Pegando preferencias para setar o nome e pontuação//
		preferencias = Gdx.app.getPreferences("flappyBird");
		pontuacaoMaxima = preferencias.getInteger("pontuacaoMaxima", 0);
	}



	@Override
	public void render () {

		//Checa estados do jogo//
		verificaEstadoJogo();

		//Validando pontos//
		validaPontos();

		//Desenha as texturas em cena//
		desenhaTexturas();

		//Detecta as colisões no jogo//
		detectaColisao();
	}

	private void desenhaTexturas() {

		//Iniciando//
		batch.begin();

		//Definindo o posicionamento dos elementos em cena//
		batch.draw(fundo, 0, 0, larguraDispositivo, alturaDispositivo);
		batch.draw(passaros[(int) variacao], 50 + posicaoHorizontalPassaro, posicaoInicialVerticalPassaro);
		batch.draw(canoBaixo, posicaoCanoHorizontal, alturaDispositivo / 2 - canoBaixo.getHeight() - espacoEntreCanos / 2 + posicaoCanoVertical);
		batch.draw(canoTopo,posicaoCanoHorizontal, alturaDispositivo / 2 + espacoEntreCanos / 2 + posicaoCanoVertical);
		textoPontuacao.draw(batch, String.valueOf(pontos), larguraDispositivo / 2, alturaDispositivo - 100);

		//Definindo situação para estadoJogo//
		if(estadoJogo == 2)
		{
			//Se estiver em estado 2, ele carregará as abaixo//
			batch.draw(gameOver, larguraDispositivo / 2 - gameOver.getWidth() / 2, alturaDispositivo / 2);
			textoReiniciar.draw(batch, "TOQUE NA TELA PARA REINICIAR!", larguraDispositivo / 2 - 200, alturaDispositivo / 2 - gameOver.getHeight() / 2);
			textoMelhorPontuacao.draw(batch, "SUA MELHOR PONTUAÇÃO É : " + pontuacaoMaxima +  "PONTOS!", larguraDispositivo / 2 - 300, alturaDispositivo / 2 - gameOver.getHeight() * 2);

			//Se estiver estado 0, o jogo roda normalmente//
		}else if(estadoJogo==0)
		{

			batch.draw(logoAngryBirds,posicaoMoedaHorizontal/2, alturaDispositivo / 2,500,500);

			//Pegando moedas, altera-se o saldo//
		}else if(!pegouMoeda){
			batch.draw(moedaAtual.texturaCoin,posicaoMoedaHorizontal,posicaoMoedaVertical,100,100);
		}

		//Finalizando//
		batch.end();
	}

	private void verificaEstadoJogo() {

		//Ao tocar n tela, o jogo entra em estado 0, aplicando as regras dentro do If//
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

		// Ao colidir com cano, toca o som ao contato e entra no estado 1 exibindo tela de gameOver e exibindo melhor pontuação//
		else if (estadoJogo == 1)
		{
			if (toqueTela)
			{
				gravidade = -15;
				somVoando.play();
			}

			//pegando posição do spawn dos canos//
			posicaoCanoHorizontal -= Gdx.graphics.getDeltaTime() * 200;
			posicaoMoedaHorizontal -= Gdx.graphics.getDeltaTime()*200;
			if(posicaoCanoHorizontal < -canoBaixo.getHeight())
			{
				posicaoCanoHorizontal = larguraDispositivo;
				posicaoCanoVertical = random.nextInt(400) - 200;
				passouCano = false;
			}

			//pegando posição do spawn das moedas//
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

			//Adicionado regra de gravidade ao tocar na tela//
			if (posicaoInicialVerticalPassaro > 0 || toqueTela)
				posicaoInicialVerticalPassaro = posicaoInicialVerticalPassaro - gravidade;

			gravidade++;
		}

		//Mostrando pontuação maxima quando jogo muda para estado 2//
		else if(estadoJogo == 2)
		{
			if(pontos > pontuacaoMaxima)
			{
				pontuacaoMaxima = pontos;
				preferencias.putInteger("pontuacaoMaxima", pontuacaoMaxima);
			}

			posicaoHorizontalPassaro -= Gdx.graphics.getDeltaTime() * 500;

			//Ao tocar na tela, o jogo reinicia do estado 2 para o estado 0//
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

	//
	private void validaPontos() {

		//aplicando regra de pontuação e som ao passar entre os canos//
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

		//definindo formatos de colisores para as sprites//
		circuloPassaro.set(50 + passaros[0].getWidth() / 2,
				posicaoInicialVerticalPassaro + passaros[0].getHeight() / 2, passaros[0].getWidth() / 2);
		retanguloCanoBaixo.set(posicaoCanoHorizontal,
				alturaDispositivo / 2 - canoBaixo.getHeight() - espacoEntreCanos / 2 + posicaoCanoVertical,
				canoBaixo.getWidth(), canoBaixo.getHeight());

		retanguloCanoCima.set(posicaoCanoHorizontal,
				alturaDispositivo / 2 + espacoEntreCanos + espacoEntreCanos / 2 + posicaoCanoVertical,
				canoTopo.getWidth(), canoTopo.getHeight());

		circuloMoeda.set(posicaoMoedaHorizontal*2,posicaoMoedaVertical + moedaAtual.texturaCoin.getHeight()/2,moedaAtual.texturaCoin.getWidth()/3);

		//Checando contato com os canos e com a moeda//
		boolean bateuCanoCima = Intersector.overlaps(circuloPassaro, retanguloCanoCima);
		boolean bateuCanoBaixo = Intersector.overlaps(circuloPassaro, retanguloCanoBaixo);
		boolean bateuMoeda = Intersector.overlaps(circuloPassaro,circuloMoeda);

		//Mudando estado do jogo ao colidir com 1 dos canos em tela//
		if(bateuCanoBaixo || bateuCanoCima)
		{

			if(estadoJogo == 1)
			{
				somColisao.play();
				estadoJogo = 2;
			}
		}

		//Aplicando pontos e som ao colidir com moeda//
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
