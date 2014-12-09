package prjageda;

import java.util.ArrayList;
import java.util.Collections;
import weka.core.Instance;
import weka.core.Instances;

public class AlGEnArDe {

    //<editor-fold defaultstate="collapsed" desc="1° Definição dos Atributos e método Inicializador da classe">    	
    //Variáveis Públicas Estáticas
    public static final int _quantidade = 100;
    public static final int _profundidade = 4;   //Define-se como Nível = Nível + 1 (para 4 irá gerar até o Nivel 5, sendo 4 Níveis mais a aresta do último nível)
    public static final double _TxCrossover = 0.9;
    public static final int _qtdDecimais = 4;
    public static ArrayList<Arvores> _nodos;
    public static ArrayList<Arvores> _arvores;
    public static MersenneTwister mtw = new MersenneTwister();

    //Variáveis Privadas Estáticas
    private static final int _geracoes = 100;
    private static final int _nroFolds = 3;
    private static final int _qtdFoldsAvaliados = 10;
    private int _qtdOcorr = 0;

    //Método Inicializador da classe
    public AlGEnArDe() {
    }
    //</editor-fold>  

    //<editor-fold defaultstate="collapsed" desc="3° Definição dos Métodos pertinentes a Geração da População">
    //Tradução da Sigla - AlGenArDe - "Al"goritmo "Gen"ético de "Ar"vore de "De"cisão
    public void AlGenArDe(Instances dados) throws Exception {
        try {
            //Efetuar o processamento das Sub-Arvores e suas Aretas (COM TODAS AS INSTÂNCIAS DE DADOS)
            gerarDecisionStumps(dados);

            //Declaração Variáveis e Objetos
            ArrayList<Arvores> resultado = new ArrayList<>();

            //Efetuar a Estratificação dos dados
            dados.stratify(_qtdFoldsAvaliados);

            //Percorrer a quantidade de Folds Avaliados
            for (int i = 0; i < _qtdFoldsAvaliados; i++) {
                //Mensagem de processamento
                System.out.println("Efetuando o processamento do Fold.: " + (i + 1) + "/" + _qtdFoldsAvaliados);

                //Definir respectivamente as "Divisões" dos Folds
                Instances treino = dados.testCV(_qtdFoldsAvaliados, i);    //Pegar APENAS 1 dos FOLDS EXISTENTES (10% DOS DADOS - Ordem do Processamento)
                Instances tempInst = dados.trainCV(_qtdFoldsAvaliados, i); //Pegar O COMPLEMENTO da divisão      (90% DOS DADOS - Dados Complementares) 

                //Definição das instâncias de "Teste e Validação"
                Instances teste = tempInst.testCV(_nroFolds, 0);           //33% dos Dados Complementares
                Instances validacao = tempInst.trainCV(_nroFolds, 0);      //66% dos Dados Complementares (Quantidade Restante)

                //Declaração Variáveis e Objetos E Inicializações
                _arvores = new ArrayList<>();
                int geracaoAtual = 1;
                int profMaxima = _profundidade * 2;

                //Efetuar a Geração da População Inicial, informar a _quantidade de atributos MENOS o atributos classe
                gerarPopulacaoInicial(treino, validacao, _profundidade + 1);

                System.out.println("Melhor Arvore.: " + _arvores.get(0).getNomeAtributo() + " "
                        + _arvores.get(0).getFitness());

                //Efetuar a geração das novas populações
                while (geracaoAtual < _geracoes) {
                    //Atualizar a Geração
                    geracaoAtual++;

                    //Carregar com as árvores
                    _arvores = new Processamento().gerarPopulacaoArvores(dados, true);

                    //Processar Árvores e Eliminar as definições dos Nodos Folhas (Na Geração Inicial NÃO SE FAZ necessário)
                    for (int j = Processamento._qtdElitismo; j < _arvores.size(); j++) {
                        //Processamento dos _nodos folhas
                        eliminarClassificacaoNodosFolhas(_arvores.get(j), 1, profMaxima, true);

                    }

                    //Calcular o Fitness e após Ordenar Crescente (em função do Crossover e da Mutação a profundidade da árvore poderá duplicar)
                    calcularFitnessPopulacao(treino, validacao, profMaxima, Processamento._qtdElitismo);
                    
                    System.out.println("Melhor Arvore.: " + _arvores.get(0).getNomeAtributo() + " "
                            + _arvores.get(0).getFitness());
                }

                //Processamento dos _nodos folhas
                eliminarClassificacaoNodosFolhas(_arvores.get(0), 1, profMaxima, false);

                //Efetuar o "Teste" em cima da melhor Árvore encontrada após o Procesamento
                testarNodosFolhas(teste, _arvores.get(0), profMaxima);

                //Adicionar a melhor Árvore da Geração
                resultado.add(_arvores.get(0));

            }

            //Calcular a Media E Desvio Padrão dos Indivíduos
            calcularMediaEDesvioPadraoArvores(resultado);

        } catch (Exception e) {
            e.printStackTrace();

        } finally {
            //Liberação dos Objetos
            _nodos = null;
            _arvores = null;
            _qtdOcorr = 0;

        }

    }

    //Efetuar a Geração da População Inicial
    private void gerarPopulacaoInicial(Instances treino, Instances validacao, int profMaxima) {
        try {
            //Declaração Variáveis e Objetos
            ArrayList<Arvores> arvore;

            //Percorrer a _quantidade de árvores informado
            for (int i = 0; i < _quantidade; i++) {
                //Inicialização Objetos (DeepCopy dos Decisions Stumps)
                arvore = (ArrayList<Arvores>) ObjectUtil.deepCopyList(_nodos);

                //Selecionar o nodo raiz (sorteado aleatóriamente)
                Arvores arv = arvore.get(mtw.nextInt(arvore.size() - 1));

                //Geração da árvore ATÉ a _profundidade estabelecida (Desconsiderando o nodo do 1° Nível)
                gerarPopulacaoArvores(arvore.size() - 1, 2, arv);

                //Adicionar a Árvore Gerada
                _arvores.add(arv);

            }

            //Calcular o Fitness das árvores(Treinamento e Validação) E após Ordenar Crescentemente 
            calcularFitnessPopulacao(treino, validacao, profMaxima, 0);

        } catch (Exception e) {
            e.printStackTrace();

        }

    }

    //Efetuar a Geração da População de Árvores de Decisão
    public void gerarPopulacaoArvores(int nroAtributos, int prof, Arvores arvore) {
        try {
            //Condição de Parada - Se o grau de _profundidade for máximo
            if (prof <= _profundidade) {
                //percorrer todas as arestas do árvore
                for (int i = 0; i < arvore.getArestas().size(); i++) {
                    //Gerar as Sub-Árvores com 50% de probabilidade                
                    if (mtw.nextBoolean()) {
                        //Tratamento dos _nodos (Geração das Sub-Árvores e Atributos)
                        ArrayList<Arvores> arvTemp = (ArrayList<Arvores>) ObjectUtil.deepCopyList(_nodos);

                        // 1°) Sortear um Nodo(Árvore) Qualquer Aleatóriamente p/ Inserção                   
                        // 2°) Inserir na aresta a Árvore Selecionada Aleatóriamente(No Atributo Nodo)
                        arvore.SetNodo(arvore.getArestas(i), arvTemp.get(mtw.nextInt(arvTemp.size())));

                        //Chamada Recursiva para Geração da árvore atualizando o nivel de _profundidade
                        gerarPopulacaoArvores(nroAtributos, prof + 1, arvore.getArvoreApartirAresta(i));

                    }

                }

            }

        } catch (Exception e) {
            e.printStackTrace();

        }

    }

    //Processamento dos _nodos - Definição das árvores e seus _nodos (Numéricos - Bifurcadas / Nominais - Quantidade de arestas definida pela _quantidade de classes)
    public void gerarDecisionStumps(Instances dados) {
        //Inicialização do Objeto
        _nodos = new ArrayList<>();

        //Processamento: PARA CADA COLUNA PERCORRE TODAS AS LINHAS
        //--------------------------------------------------------------------------------------------------------------------------------------------------------------------------
        //Percorrer TODOS os atributos (colunas) existentes e para cada atributo percorre todas as instâncias  por exemplo: Atributo 0, Atributo 1, Atributo 2,...Atributo N-1
        for (int i = 0; i < dados.numAttributes() - 1; i++) {
            //1° Passo     - Processar todos os Atributos (Binários e Nominais)
            //2° Parâmetro - Nome do atributo
            //3° Parâmetro - Instâncias e a posição do Atributo
            _nodos.add(new Arvores(dados.instance(0).attribute(i).name(), new Processamento().processarInstanciasDados(dados, i)));

        }

    }
    //</editor-fold> 

    //<editor-fold defaultstate="collapsed" desc="4° Definição dos Métodos e Funções Destinadas a Avaliação da População">    
    //Definição do Cálculo do Fitness 
    // 1° Passo - Efetua-se a classificação das árvores (definição da classe majoritária) com as instâncias de teste
    // 2° Passo - Efetua-se a Validação da árvore pelas instâncias de treinamento
    // 3° Passo - Cálculo do Fitness da árvore 
    private void calcularFitnessPopulacao(Instances treino, Instances validacao, int profMaxima, int posInicial) {
        try {
            //Efetuar o treinamento - Definir quais classes pertencem os _nodos folhas
            treinarNodosFolhas(treino, profMaxima, posInicial);

            //Execução da Validação para atualizar a _quantidade de ocorrência a partir da base montada e Calcular o Fitness
            validarNodosFolhas(validacao, profMaxima, posInicial);

            //Ordenar a população EM ORDEM CRESCENTE pelo valor do Fitness, por exemplo.: 0.2, 0.3, 0.4,...1.0
            Collections.sort(_arvores);

        } catch (Exception e) {
            e.printStackTrace();

        }

    }

    //Efetuar o treinamento dos _nodos folhas - Atribuição das classes e suas quantidades, a classe que possuir maior _quantidade será a classe dominante
    private void treinarNodosFolhas(Instances treino, int profMaxima, int posInicial) throws NullPointerException {
        try {
            //Declaração Variáveis e Objetos
            Processamento proc = new Processamento();

            //Percorrer todas as árvores existentes para atribuição das classes e quantidades dos _nodos folhas
            for (int i = posInicial; i < _arvores.size(); i++) {
                //1° Passo - Percorre a função recursivamente para chegar a todos os _nodos folhas e atribuir a(s) propriedades encontradas
                //2° Passo - Executa-se as instância de treino p/ calcular o fitness da árvore(s)
                for (int j = 0; j < treino.numInstances(); j++) {
                    //Atualizar(Calcular) a _quantidade de ocorrências dos atributos na árvore
                    proc.definirClasseNodosFolhas(_arvores.get(i), treino.instance(j), 1, profMaxima);

                }

                //Definir a classe majoritária da aresta
                proc.atribuirClasseNodosFolhas(_arvores.get(i), 1, profMaxima);

                //Eliminar as arestas que não possuem classificação após a execução do "Treino", ou seja, efetua a PODA dos nodos que possuem apenas 1 aresta válida
                podarArestasSemClassificacao(_arvores.get(i), 1, profMaxima);

                //Eliminar os Nodos(Atributos) que não possuem arestas após a execução do "Treino"
                podarNodosSemArestas(_arvores.get(i), 1, profMaxima);

            }

        } catch (Exception e) {
            e.printStackTrace();

        }

    }

    //Validação das folhas - Cálculo do Fitness, PARA cada árvore percorre-se TODAS as instâncias de Validação e efetua-se o cálculo das quantidades das Classes p/ cada Nodo Folha
    private void validarNodosFolhas(Instances validacao, int profMaxima, int posInicial) {
        try {
            //Percorrer todas as árores existentes e Atualiza a _quantidade de ocorrências
            for (int i = posInicial; i < _arvores.size(); i++) {
                //Zerar a quantidade de Ocorrências
                this._qtdOcorr = 0;

                //1° Passo - Percorre a função recursivamente para chegar a todos os _nodos folhas e atribuir a(s) propriedades encontradas
                //2° Passo - Executa-se as instância de avaliação p/ calcular o fitness da árvore(s)
                for (int j = 0; j < validacao.numInstances(); j++) {
                    //Atualizar(Calcular) a _quantidade de ocorrências dos atributos na árvore
                    validarCalculoFitnessArvore(_arvores.get(i), validacao.instance(j), 1, profMaxima);

                }

                //Atualizar a Quantidade de ocorrências e o Valor do Fitness Inicial
                _arvores.get(i).setQtdOcorrencias(this._qtdOcorr);
                _arvores.get(i).setFitness(1);

                //Se possuir Ocorrências Efetua o Cálculo
                if (this._qtdOcorr > 0) {
                    //Declaração Variáveis e Objetos
                    double vlrFitness = new Processamento().arredondarValor(1 - ((double) this._qtdOcorr / validacao.numInstances()), _qtdDecimais, 1);

                    //Cálcular Valor do Fitness
                    _arvores.get(i).setFitness(vlrFitness == 0 ? 1 : vlrFitness);

                }

            }

        } catch (Exception e) {
            e.printStackTrace();

        }

    }

    //Cálculo das quantidades (POR CLASSE) de cada um dos _nodos folhas
    public void validarCalculoFitnessArvore(Arvores arvore, Instance dados, int prof, int profMaxima) {
        //Se o árvore for nula
        if (arvore == null) {
            return;

        }

        //Se as arestas forem nulas
        if (arvore.getArestas().isEmpty()) {
            return;

        }

        //Condição de Parada - Se o grau de _profundidade for máximo
        if (prof <= (profMaxima + 1)) {
            //Declaração Variáveis e Objetos
            int posicao = 0;

            //OBSERVAÇÃO.: O "for" é devido as classes do WEKA não permitirem de que apartir da instância selecionada PEGAR um atributo em específico, a pesquisa é feita
            //             somente pelo índice do atributo e NÃO PELO NOME DO MESMO
            //------------------------------------------------------------------------------------------------------------------------------------------------------------------
            //Percorrer todos os atributos da instância selecionada
            for (int k = 0; k < dados.numAttributes() - 1; k++) {
                //Encontrou o mesmo atributos que o pesquisado
                if (dados.attribute(k).name().equals(arvore.getNomeAtributo())) {
                    posicao = k;
                    break;

                }

            }

            //Se o atributo for Numérico
            if (dados.attribute(posicao).isNumeric()) {
                //Declaração Variáveis e Objetos
                Processamento prc = new Processamento();
                double valorAresta = Double.valueOf(arvore.getArestas(0).getAtributo().split(" ")[1]);

                //Se valor posição 0 FOR MENOR IGUAL ao valor atributo selecionado (Então posição igual a 0 SENAO 1)
                int pos = prc.arredondarValor(dados.value(posicao), _qtdDecimais, 1) <= prc.arredondarValor(valorAresta, _qtdDecimais, 1) ? 0 : 1;

                //Se a árvore não for nula
                if (arvore.getArestas(pos) != null) {
                    //Se for um nodo folha
                    if (arvore.getArestas(pos).getNodo() == null) {
                        //Se o valor da aresta for igual ao valor do atributo selecionada da instância processada, atualiza a _quantidade de OCORRÊNCIAS do Nodo
                        if (arvore.getArestas(pos).getClasseDominante().equals(dados.classAttribute().value((int) dados.classValue()))) {
                            //Atualizar a _quantidade (Somar 1 na _quantidade atual)
                            this._qtdOcorr += 1;

                        }

                    } else {
                        //Chamada recursiva da função passando como parâmetros a aresta selecionada
                        validarCalculoFitnessArvore(arvore.getArestas(pos).getNodo(), dados, prof + 1, profMaxima);

                    }

                }

            } else {
                //Percorrer todas as arestas
                for (Atributos aresta : arvore.getArestas()) {
                    //Se for um nodo folha
                    if (aresta.getNodo() == null) {
                        //Se o valor da aresta for igual ao valor do atributo selecionada da instância processada, atualiza a quantidade de OCORRÊNCIAS do Nodo
                        if (dados.classAttribute().value((int) dados.classValue()).equals(aresta.getClasseDominante())) {
                            //Atualizar a _quantidade (Somar 1 na _quantidade atual)
                            this._qtdOcorr += 1;

                        }

                    } else {
                        //Chamada recursiva da função passando como parâmetros a aresta selecionada
                        validarCalculoFitnessArvore(aresta.getNodo(), dados, prof + 1, profMaxima);

                    }

                }

            }

        }

    }

    //Efetuar a Validação do _nodos folhas p/ o Cálculo do Fitness, resumindo PARA cada árvore percorre-se TODAS as instâncias de Validação e efetua-se o calculo das 
    //quantidades das Classes p/ cada nodo folha
    private void testarNodosFolhas(Instances teste, Arvores arvore, int profMaxima) {
        try {
            //Zerar a _quantidade de Ocorrências
            this._qtdOcorr = 0;

            //1° Passo - Percorre a função recursivamente para chegar a todos os _nodos folhas e atribuir a(s) propriedades encontradas
            //2° Passo - Executa-se as instância de avaliação p/ calcular o fitness da árvore(s)
            for (int j = 0; j < teste.numInstances(); j++) {
                //Atualizar(Calcular) a _quantidade de ocorrências dos atributos na árvore
                validarCalculoFitnessArvore(arvore, teste.instance(j), 1, profMaxima);

            }

            //Atualizar a Quantidade de ocorrências e o Valor do Fitness Inicial
            arvore.setQtdOcorrencias(this._qtdOcorr);
            arvore.setFitness(1);

            //Se possuir Ocorrências Efetua o Cálculo
            if (this._qtdOcorr > 0) {
                //Declaração Variáveis e Objetos
                double vlrFitness = new Processamento().arredondarValor(1 - ((double) this._qtdOcorr / teste.numInstances()), _qtdDecimais, 1);

                //Cálcular Valor do Fitness
                arvore.setFitness(vlrFitness == 0 ? 1 : vlrFitness);

            }

        } catch (Exception e) {
            e.printStackTrace();

        }

    }

    /**
     * Eliminar a Classificação dos _nodos Folhas
     *
     * @param arv - Árvore a ser processada
     * @param prof - Profundidade Atual
     * @param profMaxima - Profundidade Máxima da árvore
     * @param bEliminar - True/False - Eliminar a classificação dos nodos folhas da Árvore
     */
    private void eliminarClassificacaoNodosFolhas(Arvores arv, int prof, int profMaxima, boolean bEliminar) {
        try {
            //Condição de Parada - Se o grau de _profundidade for máximo
            if (prof <= profMaxima) {
                //Se o árvore for nula
                if (arv == null) {
                    return;

                }

                //Se as arestas forem nulas
                if (arv.getArestas().isEmpty()) {
                    return;

                }
                //Atualizar os Atributos da Árvore
                arv.setQtdOcorrencias(0);
                arv.setFitness(0);

                //Se possuir arestas
                if (!arv.getArestas().isEmpty()) {
                    //Percorrer todas as arestas
                    for (int i = 0; i < arv.getArestas().size(); i++) {
                        //Se a aresta não for nula
                        if (arv.getArestas(i) != null) {
                            //Se deve eliminar as definições das classes e Atributos determinantes
                            if (bEliminar) {
                                //Atribuições da aresta
                                arv.getArestas(i).setClasses(null);
                                arv.getArestas(i).setClasseDominante("");

                            }

                            //Se a aresta não for nula
                            if (arv.getArestas(i) != null) {
                                //Se o nodo não for nulo (Chama Recursivamente o próximo nível) até chegar em um nodo Folha
                                if (arv.getArestas(i).getNodo() != null) {
                                    //Chamada Recursiva da Função Avaliando a posição Atual
                                    eliminarClassificacaoNodosFolhas(arv.getArestas(i).getNodo(), prof + 1, profMaxima, bEliminar);

                                }

                            }

                        }

                    }

                }

            }

        } catch (Exception e) {
            e.printStackTrace();

        }

    }

    //Efetuar o Cálculo da Média e Desvio padrão do Processamento
    private void calcularMediaEDesvioPadraoArvores(ArrayList<Arvores> dados) {
        //Declaração Variáveis e Objetos
        int execucao = 0;
        double vlrMedia = 0d;
        double vlrDesvioPadrao = 0d;

        //Imprimir Linha em Branco
        System.out.println("\n");

        //Imprimir as melhores Árvores obtidas
        for (Arvores item : dados) {
            //Somatório dos Fitness
            vlrMedia += new Processamento().arredondarValor(item.getFitness(), 4, 1);

        }

        //Calcular o valor da Média (Somatório dos Fitness / Quantidade de itens)
        vlrMedia = new Processamento().arredondarValor(vlrMedia / dados.size(), 4, 1);

        //Calcular o Desvio Padrão - Que é a raiz Quadrada da Variância. 
        for (Arvores item : dados) {
            //Calcular o Desvio
            double vlrDesvio = new Processamento().arredondarValor(item.getFitness() - vlrMedia, 4, 1);

            //Imprimir as Melhores Árrvores Processadas
            System.out.println("Execução.: " + execucao + " - Árvore.: " + item.getNomeAtributo()
                    + " - Fitness.: " + item.getFitness() + " - Valor Média.: " + vlrMedia
                    + " - Desvio.: " + vlrDesvio
                    + " - Quadrado Desvio.: " + new Processamento().arredondarValor(Math.pow(vlrDesvio, 2), 4, 0));

            //Diferença do 
            vlrDesvioPadrao += Math.pow(vlrDesvio, 2);
            execucao += 1;

        }

        //Imprimir o Desvio Padrão
        System.out.println("\nValor Desvio Padrão Encontrado.: " + new Processamento().arredondarValor(Math.sqrt(vlrDesvioPadrao / dados.size()), 4, 1)
                + " - Valor Média.: " + vlrMedia + "\n");

    }

    //Eliminar as arestas nulas da árvoe após a classificação dos nodos(Após a aplicação das Instâncias de "Treino")
    private void podarArestasSemClassificacao(Arvores arv, int prof, int profMaxima) {
        try {
            //Condição de Parada - Se o grau de _profundidade for máximo
            if (prof <= profMaxima) {
                //Se o árvore for nula
                if (arv == null) {
                    return;

                }

                //Se as arestas forem nulas
                if (arv.getArestas().isEmpty()) {
                    return;

                }

                //Se possuir arestas
                if (!arv.getArestas().isEmpty()) {
                    //Declaração Variáveis e Objetos
                    int qArestas = arv.getArestas().isEmpty() ? 0 : arv.getArestas().size();
                    int pos = 0, i = 0;

                    //Percorrer todas as arestas
                    for (i = 0; i < qArestas; i++) {
                        //Se a aresta não for nula
                        if (arv.getArestas(i) != null) {
                            //Percorrer todas as arestas
                            while (pos < qArestas) {
                                //Se a aresta da "posição" não for nula
                                if (arv.getArestas(pos) != null) {
                                    //Se o nodo for nulo elimina a aresta e verifica as demais
                                    if ((arv.getArestas(pos).getNodo() == null) && (arv.getArestas(pos).getClasseDominante().isEmpty())) {
                                        //Excluir o nodo atual E Atualizar a posição e a quantidade
                                        arv.removerAresta(pos);
                                        qArestas = pos = i = 0;

                                        //Se não possuir arestas
                                        if (!arv.getArestas().isEmpty()) {
                                            qArestas = arv.getArestas().size();

                                        } else {
                                            //Setar o Fitness para 1 (máximo possível)
                                            arv.setFitness(1.0d);

                                            //Sair fora
                                            break;

                                        }

                                    } else {
                                        //Atualizar a posição
                                        pos += 1;

                                    }

                                } else {
                                    //Atualizar a posição
                                    pos += 1;

                                }

                            }

                            //Se possuir arestas válidas
                            if (!arv.getArestas().isEmpty()) {
                                //Se possuir a aresta da posição atual válida
                                if (arv.getArestas(i) != null) {
                                    //Se o nodo não for nulo (Chama Recursivamente o próximo nível) até chegar em um nodo Folha
                                    if (arv.getArestas(i).getNodo() != null) {
                                        //Chamada Recursiva da Função Avaliando a posição Atual
                                        podarArestasSemClassificacao(arv.getArestas(i).getNodo(), prof + 1, profMaxima);

                                    }

                                }

                            }

                        }

                    }

                }

            }

        } catch (Exception e) {
            e.printStackTrace();

        }

    }

    //Podar os nodos que não possuem arestas
    private void podarNodosSemArestas(Arvores arv, int prof, int profMaxima) {
        try {
            //Condição de Parada - Se o grau de _profundidade for máximo
            if (prof <= profMaxima) {
                //Se o árvore for nula
                if (arv == null) {
                    return;

                }

                //Se possuir arestas
                if (!arv.getArestas().isEmpty()) {
                    //Declaração Variáveis e Objetos
                    int qArestas = arv.getArestas().size();
                    int i = 0;

                    //Se possuir apenas 1 Arestas
                    if (qArestas == 1) {
                        //Se a única aresta possuir uma Sub-Árvore Válida
                        if (arv.getArestas(0).getNodo() != null) {
                            //Declaração Variáveis e Objetos
                            Arvores arvTemp = arv.getArestas(0).getNodo();

                            //Excluir o nodo atual setando o próximo nodo como mandante ("Absorver" a árvore atual)
                            arv.removerAresta(0);

                            //Se possuir arestas válidas
                            if (!arvTemp.getArestas().isEmpty()) {
                                //Setar os atributos da árvore
                                arv.setArvore(arvTemp);

                            }

                        }

                    } else {
                        //Percorrer todas as arestas
                        for (i = 0; i < qArestas; i++) {
                            //Se a aresta não for nula
                            if (arv.getArestas(i) != null) {
                                //Se possuir nodo válido
                                if (arv.getArestas(i).getNodo() != null) {
                                    //Se a Arestas SELECIONADA tiver uma Sub-Árvore válida, mas a mesma não possui nenhuma aresta, elimina o nodo
                                    if (arv.getArestas(i).getNodo().getArestas().isEmpty()) {
                                        //Remover a aresta Selecionada cujo o nodo existe mas as arestas deste nodo são inválidas
                                        arv.removerAresta(i);

                                        //Atribuições
                                        qArestas = arv.getArestas().isEmpty() ? 0 : arv.getArestas().size();

                                        //Atribuições(Devido a Remoção da Árvore)
                                        i = 0;

                                        //Se possuir arestas
                                        if (qArestas == 0) {
                                            //Sair Fora
                                            break;

                                        } else if (qArestas == 1) { //Se possuir apenas 1 aresta
                                            //Se o nodo da Sub-Árvore não for nulo "Absorve" a Árvore Atual
                                            if (arv.getArestas(0).getNodo() != null) {
                                                //Declaração Variáveis e Objetos
                                                Arvores arvTemp = arv.getArestas(0).getNodo();

                                                //Excluir o nodo atual setando o próximo nodo como mandante ("Absorver" a árvore atual)
                                                arv.removerAresta(0);

                                                //Se possuir arestas válidas
                                                if (!arvTemp.getArestas().isEmpty()) {
                                                    //Setar os atributos da árvore
                                                    arv.setArvore(arvTemp);

                                                }

                                            }

                                        }

                                    }

                                    //Se possuir arestas válidas
                                    if (!arv.getArestas().isEmpty()) {
                                        //Se a aresta informada for válida
                                        if (arv.getArestas(i) != null) {
                                            //Se o nodo da aresta for válido
                                            if (arv.getArestas(i).getNodo() != null) {
                                                //Se o nodo possuir arestas
                                                if (!arv.getArestas(i).getNodo().getArestas().isEmpty()) {
                                                    //Se a quantidade de arestas do nodo for apenas 1 aresta
                                                    if (arv.getArestas(i).getNodo().getArestas().size() == 1) {
                                                        //Se a posição da aresta for 0 processa SENÃO existe aresta válida anterior
                                                        if (i == 0) {
                                                            //Se o nodo não for nulo
                                                            if (arv.getArestas(i).getNodo().getArestas(i).getNodo() != null) {
                                                                //Declaração Variáveis e Objetos
                                                                Arvores arvTemp = arv.getArestas(i).getNodo().getArestas(i).getNodo();

                                                                //Excluir o nodo atual setando o próximo nodo como mandante ("Absorver" a árvore atual)
                                                                arv.removerAresta(i);

                                                                //Se possuir arestas válidas
                                                                if (!arvTemp.getArestas().isEmpty()) {
                                                                    //Setar os atributos da árvore
                                                                    arv.setArvore(arvTemp);

                                                                }

                                                            }

                                                        }

                                                    }

                                                }

                                            }

                                        }

                                    }

                                    //Se possuir arestas
                                    if (!arv.getArestas().isEmpty()) {
                                        //Se possuir apenas 1 aresta
                                        if (arv.getArestas().size() == 1) {
                                            //Se o nodo da Sub-Árvore não for nulo "Absorve" a Árvore Atual
                                            if (arv.getArestas(0).getNodo() != null) {
                                                //Declaração Variáveis e Objetos
                                                Arvores arvTemp = arv.getArestas(0).getNodo();

                                                //Excluir o nodo atual setando o próximo nodo como mandante ("Absorver" a árvore atual)
                                                arv.removerAresta(0);

                                                //Se possuir arestas válidas
                                                if (!arvTemp.getArestas().isEmpty()) {
                                                    //Setar os atributos da árvore
                                                    arv.setArvore(arvTemp);

                                                }

                                            }

                                        }

                                    }

                                }

                            }

                            //Se possuir arestas válidas
                            if (!arv.getArestas().isEmpty()) {
                                //Se a aresta da posição for válida
                                if (arv.getArestas(i) != null) {
                                    //Se o nodo não for nulo (Chama Recursivamente o próximo nível) até chegar em um nodo Folha
                                    if (arv.getArestas(i).getNodo() != null) {
                                        //Chamada Recursiva da Função Avaliando a posição Atual
                                        podarNodosSemArestas(arv.getArestas(i).getNodo(), prof + 1, profMaxima);

                                    }

                                }

                            }

                        }

                    }

                }

            }

        } catch (Exception e) {
            e.printStackTrace();

        }
        //</editor-fold> 

    }

}
