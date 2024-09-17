import kotlin.random.Random

const val TAMANHO = 10
const val NUM_PORTA_AVIOES = 10
const val NUM_CRUZADOR = 1
const val NUM_REBOCADORES = 2
const val MAX_TENTATIVAS = 15

enum class TipoNavio(val pontuacao: Int, val simbolo: Char) {
    PORTA_AVIOES(5, 'P'),
    CRUZADOR(15, 'C'),
    REBOCADOR(10, 'R'),
    VAZIO(0, '.'),
    ERRO(0, 'O')
}

data class Posicao(val linha: Int, val coluna: Int)

fun main() {
    do {
        val tabuleiro = Array(TAMANHO) { Array(TAMANHO) { TipoNavio.VAZIO } }
        val localizacoesNavios = mutableListOf<Posicao>()
        val acertos = mutableSetOf<Posicao>()
        val erros = mutableSetOf<Posicao>()
        val detalhesErros = mutableMapOf<Posicao, Char>()
        var pontuacao = 0
        var tentativas = 0

        posicionarNavios(tabuleiro, TipoNavio.PORTA_AVIOES, NUM_PORTA_AVIOES, localizacoesNavios)
        posicionarNavios(tabuleiro, TipoNavio.CRUZADOR, NUM_CRUZADOR, localizacoesNavios)
        posicionarNavios(tabuleiro, TipoNavio.REBOCADOR, NUM_REBOCADORES, localizacoesNavios)

        while (tentativas < MAX_TENTATIVAS) {
            exibirTabuleiro(tabuleiro, acertos, erros, detalhesErros)

            println("Digite as coordenadas (linha coluna) separadas por espaço: ")
            val entrada = readLine() ?: ""
            val (linha, coluna) = entrada.split(" ").map { it.toIntOrNull() ?: -1 }

            if (linha !in 0 until TAMANHO || coluna !in 0 until TAMANHO) {
                println("Coordenadas inválidas. Tente novamente.")
                continue
            }

            if (acertos.contains(Posicao(linha, coluna)) || erros.contains(Posicao(linha, coluna))) {
                println("Posição já atingida. Tente novamente.")
                continue
            }

            tentativas++
            when (val navio = tabuleiro[linha][coluna]) {
                TipoNavio.PORTA_AVIOES -> {
                    pontuacao += TipoNavio.PORTA_AVIOES.pontuacao
                    acertos.add(Posicao(linha, coluna))
                    println("Acertou um Porta Aviões! Pontuação: $pontuacao")
                }
                TipoNavio.CRUZADOR -> {
                    pontuacao += TipoNavio.CRUZADOR.pontuacao
                    acertos.add(Posicao(linha, coluna))
                    println("Acertou um Cruzador! Pontuação: $pontuacao")
                }
                TipoNavio.REBOCADOR -> {
                    pontuacao += TipoNavio.REBOCADOR.pontuacao
                    acertos.add(Posicao(linha, coluna))
                    println("Acertou um Rebocador! Pontuação: $pontuacao")
                }
                TipoNavio.VAZIO -> {
                    val distanciaErro = calcularDistanciaErro(tabuleiro, linha, coluna)
                    erros.add(Posicao(linha, coluna))
                    detalhesErros[Posicao(linha, coluna)] = distanciaErro
                    println("Errou. Distância do erro: $distanciaErro")
                }

                TipoNavio.ERRO -> TODO()
            }
        }

        exibirTabuleiro(tabuleiro, acertos, erros, detalhesErros, verdadeiro = true)
        println("Fim de jogo! Sua pontuação final é $pontuacao")

        println("Deseja jogar novamente? (sim/não): ")
    } while (readLine()?.trim()?.equals("sim", ignoreCase = true) == true)
}

fun posicionarNavios(tabuleiro: Array<Array<TipoNavio>>, tipo: TipoNavio, quantidade: Int, localizacoes: MutableList<Posicao>) {
    var posicionado = 0
    while (posicionado < quantidade) {
        val linha = Random.nextInt(TAMANHO)
        val coluna = Random.nextInt(TAMANHO)
        val pos = Posicao(linha, coluna)
        if (tabuleiro[linha][coluna] == TipoNavio.VAZIO) {
            tabuleiro[linha][coluna] = tipo
            localizacoes.add(pos)
            posicionado++
        }
    }
}

fun calcularDistanciaErro(tabuleiro: Array<Array<TipoNavio>>, linha: Int, coluna: Int): Char {
    val direcoes = listOf(
        Posicao(1, 0), Posicao(-1, 0), Posicao(0, 1), Posicao(0, -1)
    )

    for (distancia in 1..3) {
        for (direcao in direcoes) {
            val novaLinha = linha + direcao.linha * distancia
            val novaColuna = coluna + direcao.coluna * distancia
            if (novaLinha in 0 until TAMANHO && novaColuna in 0 until TAMANHO) {
                if (tabuleiro[novaLinha][novaColuna] != TipoNavio.VAZIO) {
                    return distancia.toString()[0]
                }
            }
        }
    }

    return 'M'
}

fun exibirTabuleiro(
    tabuleiro: Array<Array<TipoNavio>>,
    acertos: Set<Posicao>,
    erros: Set<Posicao>,
    detalhesErros: Map<Posicao, Char> = mapOf(),
    verdadeiro: Boolean = false
) {
    val RESET = "\u001B[0m"
    val RED = "\u001B[31m"
    val GREEN = "\u001B[32m"
    val BLUE = "\u001B[34m"
    val GRAY = "\u001B[37m"

    println("    ${0.until(TAMANHO).joinToString("   ")}")
    println("  ${"-".repeat(TAMANHO * 4 + 1)}")

    for (linha in tabuleiro.indices) {
        print("${linha.toString().padStart(2)}| ")
        for (coluna in tabuleiro[linha].indices) {
            val pos = Posicao(linha, coluna)
            val simbolo = when {
                pos in acertos -> when (tabuleiro[linha][coluna]) {
                    TipoNavio.PORTA_AVIOES -> "$RED${tabuleiro[linha][coluna].simbolo}$RESET"
                    TipoNavio.CRUZADOR -> "$RED${tabuleiro[linha][coluna].simbolo}$RESET"
                    TipoNavio.REBOCADOR -> "$RED${tabuleiro[linha][coluna].simbolo}$RESET"
                    else -> ""
                }

                pos in erros -> "$GREEN${detalhesErros[pos]}$RESET"

                verdadeiro && tabuleiro[linha][coluna] != TipoNavio.VAZIO -> when (tabuleiro[linha][coluna]) {
                    TipoNavio.PORTA_AVIOES -> "$BLUE${tabuleiro[linha][coluna].simbolo}$RESET"
                    TipoNavio.CRUZADOR -> "$BLUE${tabuleiro[linha][coluna].simbolo}$RESET"
                    TipoNavio.REBOCADOR -> "$BLUE${tabuleiro[linha][coluna].simbolo}$RESET"
                    else -> "$GRAY${tabuleiro[linha][coluna].simbolo}$RESET"
                }

                else -> " "
            }
            print("$simbolo | ")
        }
        println()
        println("  ${"-".repeat(TAMANHO * 4 + 1)}")
    }
}