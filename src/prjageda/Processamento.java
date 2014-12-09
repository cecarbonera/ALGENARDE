package prjageda;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;

public class Processamento {

    //<editor-fold defaultstate="collapsed" desc="Declaração Atributos e Método(s) Construtor(es) da Classe">    
    //Declaração de objetos privados
    private static final double _percMutacao = 0.05; //5% de Possibilidade de Mutação
    private String _caminhoDados;

    //Declaração de objetos públicos
    public static final int _qtdElitismo = 2;

    public String getCaminhoDados() {
        return _caminhoDados;
    }

    public void setCaminhoDados(String caminho) {
        this._caminhoDados = caminho;
    }

    public Processamento(String local) {
        //Inicialização dos atributos
        this._caminhoDados = local;

    }

    public Processamento() {

    }
    //</editor-fold>        

    //<editor-fold defaultstate="collapsed" desc="Métodos de Processamento Diversos">        
    //Leitura do Arquivo
    public Instances lerArquivoDados() {
        //Declaração Variáveis e Objetos
        Instances dados = null;

        try {
            //Inicialização da Leitura
            dados = new DataSource(_caminhoDados).getDataSet();

            //Setar o atribArv1 classe
            if (dados.classIndex() == -1) {
                dados.setClassIndex(dados.numAttributes() - 1);

            }

        } catch (Exception e) {
            //Se ocorreu alguma exceção
            e.printStackTrace();

        }

        //Definição do Retorno
        return dados;

    }

    //Processamento das instâncias lidas da base de dados
    public ArrayList<Atributos> processarInstanciasDados(Instances dados, int posicao) {
        //Declaração Variáveis e Objetos
        ArrayList<Atributos> registros = new ArrayList<>();

        try {
            //1 - Avaliar se o Atributo é Numérico ou Nominal 
            //  - 1.1 - Se Numérico a árvore terá 2 arestas (Árvore será bifurcada) 
            //  - 1.2 - Se Categórico terá o número de arestas em função da _quantidade de atributos encontrados no dataset
            if (dados.attribute(posicao).isNumeric()) {
                //Declaração Variáveis e Objetos
                String indiceGini = String.valueOf(arredondarValor(calcularIndiceGini(dados, posicao), AlGEnArDe._qtdDecimais, 1));

                //Para atribArv1 numéricos, SEMPRE será bifurcada, assim: 
                //Aresta 0 - Sempre será MENOR OU IGUAL a Média Calculada
                //Aresta 1 - Sempre será MAIOR que a Média Calculada
                registros.add(new Atributos("<= " + indiceGini, null, "", null));
                registros.add(new Atributos("> " + indiceGini, null, "", null));

            } else {
                //Percorrer a Quantidade de Atributos existentes e adicionando os mesmos
                for (int i = 0; i < dados.attribute(posicao).numValues(); i++) {
                    //Adicionar as Arestas
                    registros.add(new Atributos(dados.attribute(posicao).value(i), null, "", null));

                }

            }
        } catch (Exception e) {
            e.printStackTrace();

        }

        //Definir o retorno
        return registros;

    }

    //Efetuar o processamento Recursivo da Árvore, lendo cada instância da base de dados e percorrer toda a árvore
    public ArrayList<Arvores> gerarPopulacaoArvores(Instances dados, boolean elitismo) throws Exception {
        //Declaração Variáveis e Objetos
        ArrayList<Arvores> populacao = new ArrayList<>();

        try {
            //Se tiver elitismo, adicionar (mantém) a melhor árvore da geração atual(ordenada) para a próxima geração
            if (elitismo) {
                //Adicionar a Quantidade de Indivíduos obtidos por Elitismo
                for (int i = 0; i < _qtdElitismo; i++) {
                    //Adicionar as árvores obtidas por Elitismo(Quantidade definida p/ Elitismo)
                    populacao.add(ObjectUtil.deepCopy(AlGEnArDe._arvores.get(i)));

                }

            }

            //Efetua a geração da nova população equanto a população for menor que a população inicialmente estabelecida
            while (populacao.size() < AlGEnArDe._quantidade) {
                //------------------------ Remover ---------------------------------------------------------------------------------------
                //Adicionar as Árvores Pais (Seleção por Torneio)
                Arvores arv1 = selecionarArvoresPorTorneio(AlGEnArDe._arvores);
                Arvores arv2 = selecionarArvoresPorTorneio(AlGEnArDe._arvores);

                //SE Valor Gerado <= _TxCrossover, realiza o Crossover entre os pais SENÃO mantém os pais selecionados através de Torneio p/ a próxima geração            
                if (AlGEnArDe.mtw.nextDouble() <= AlGEnArDe._TxCrossover) {
                    //Efetuar o Crossover E Adicionar as Árvores Filhas              
                    populacao.addAll(efetuarCrossoverArvores(arv1, arv2));

                } else {
                    //Apenas Adicionar as Árvores Filhas
                    populacao.add(arv1);
                    populacao.add(arv2);

                }

            }

            //Efetuar Mutação das Árvores (se selecionado pelo critério do %), EXCETO p/ as Árvores obtidas por Elitismo
            for (int i = _qtdElitismo; i < populacao.size(); i++) {
                //Se for MENOR OU IGUAL ao Limite Superior (Valor < Limite Superior)
                if (arredondarValor(AlGEnArDe.mtw.nextDouble(), 2, 1) < Processamento._percMutacao) {
                    //Declaração Variáveis e Objetos
                    String atributo = BuscarAtributosArvore(populacao.get(i));

                    //Caso a árvore possuir mais do que o nodo raiz, retornará um atribArv1, caso contrário nem processa
                    if (!atributo.isEmpty()) {
                        //Efetuar a Mutação da Árvore - "E"xpansão ou "R"etração de Nodos e Limpar o Objeto
                        efetuarMutacaoArvores(populacao.get(i), 1, AlGEnArDe.mtw.nextBoolean() ? "E" : "R", atributo);

                    }

                }

            }

        } catch (Exception e) {
            e.printStackTrace();

        }

        //Definição do retorno da função
        return populacao;

    }

    //Efetuar a seleção por Torneio das Árvores - Seleciona-se as Árvores Aleatóriamente Ordenando-os Crescente
    private Arvores selecionarArvoresPorTorneio(List<Arvores> arvores) throws Exception {
        //Adicionar 2 árvores Selecionadas Aleatóriamente
        Arvores arvore1 = arvores.get(AlGEnArDe.mtw.nextInt(arvores.size() - 1));
        Arvores arvore2 = arvores.get(AlGEnArDe.mtw.nextInt(arvores.size() - 1));

        //Retornar a melhor arvore (melhor fitness)
        return ObjectUtil.deepCopy((arvore1.getFitness() < arvore1.getFitness() ? arvore1 : arvore2));

    }
    //</editor-fold>  

    //<editor-fold defaultstate="collapsed" desc="Funções Destinadas ao Crossover">
    //Efetuar o Crossover nas Árvores informadas
    private ArrayList<Arvores> efetuarCrossoverArvores(Arvores arvore1, Arvores arvore2) {
        //Declaração Variáveis e Objetos
        ArrayList<Arvores> retorno = new ArrayList<>();

        try {
            //Declaração Variáveis e Objetos e Inicializações
            String atribArv1 = BuscarAtributosArvore(arvore1);
            String atribArv2 = BuscarAtributosArvore(arvore2);
            Arvores arvTemp1 = null, arvTemp2 = null;

            //Se o atribArv1 for válido processa SENÃO abandona o processamento
            if (!atribArv1.isEmpty()) {
                //Buscar um dos Atributos Selecionados Aleatóriamente e Remove a Sub-Árvore da 1° Árvore Transformando em um Nodo "Folha" na posição 
                arvTemp1 = PesquisarPosicaoArvoreOrigem(arvore1, atribArv1, 1);

            }

            //Se o atribArv1 for válido processa SENÃO abandona o processamento
            if (!atribArv2.isEmpty()) {
                //Buscar um dos Atributos Selecionados Aleatóriamente e Remove a Sub-Árvore da 1° Árvore Transformando em um Nodo "Folha" na posição 
                arvTemp2 = PesquisarPosicaoArvoreOrigem(arvore2, atribArv2, 1);

            }

            //Se o atribArv1 for válido processa SENÃO abandona o processamento
            if (!atribArv1.isEmpty()) {
                //Incluir o nodo removido na 2° Árvore na posição definida pela 1° Árvore
                retorno.add(atribuirSubArvorePosicaoArvoreDestino(arvore1, 1, atribArv1, arvTemp2));

            }

            //Se o atribArv1 for válido processa SENÃO abandona o processamento
            if (!atribArv2.isEmpty()) {
                //Incluir o nodo removido na 1° Árvore na posição definida pela 2° Árvore
                retorno.add(atribuirSubArvorePosicaoArvoreDestino(arvore2, 1, atribArv2, arvTemp1));

            }

        } catch (Exception e) {
            e.printStackTrace();

        }

        //Definir o retorno
        return retorno;

    }
    //</editor-fold>  

    //<editor-fold defaultstate="collapsed" desc="Funções Pertinentes aos Métodos de Mutação">   
    //Efetuar a Mutação da árvore, a mesma poderá ser de "E"xpansão ou "R"edução
    private void efetuarMutacaoArvores(Arvores arvore, int prof, String tipo, String atributo) throws IOException {
        try {
            //Se a árvore não for nula
            if (arvore == null) {
                return;

            }

            //Se possuir arestas válidas
            if (arvore.getArestas().isEmpty()) {
                return;

            }

            //SE for "E" - EXPANSÃO - Vai até um nodo FOLHA ALEATÓRIO E ADICIONA um AlGEnArDe Aleatóriamente
            //SENÃO  "R" - REDUÇÃO  - Vai até o nodo passado como parâmetro e transforma-se todos as sub-árvores abaixo em folhas
            //Condição de Parada - Se o grau de _profundidade for máximo
            if (prof <= (AlGEnArDe._profundidade + 1)) {
                //Se for "EXPANSÃO"
                if (tipo.equals("E")) {
                    //Declaração Variáveis e Objetos - Selecionar uma posição aleatória
                    int itemPos = arvore.getArestas().size() <= 1 ? 0 : AlGEnArDe.mtw.nextInt(arvore.getArestas().size() - 1);
                    ArrayList<Arvores> temp = (ArrayList<Arvores>) ObjectUtil.deepCopyList(AlGEnArDe._nodos);

                    //Se a aresta selecionada não for nula percorre até encontrar uma aresta nula
                    if (arvore.getArestas(itemPos) != null) {
                        //Se o nodo da aresta não for nulo
                        if (arvore.getArestas(itemPos).getNodo() != null) {
                            //Se atingiu o MAIOR NÍVEL de _profundidade da árvore (O último nodo da aresta DEVERÁ SER nulo) - Avaliando a aresta SELECIONADA aleatóriamente
                            if (arvore.getArestas(itemPos).getNodo().getArestas(itemPos) != null) {
                                //Se atingiu o MAIOR NÍVEL de _profundidade da árvore (O último nodo da aresta DEVERÁ SER nulo) - Avaliando a aresta SELECIONADA aleatóriamente
                                if (arvore.getArestas(itemPos).getNodo().getArestas(itemPos).getNodo() == null) {
                                    //Selecionar aleatóriamente uma árvore p/ ser incluida no nodo raiz e Adicionar o nodo na aresta selecionada e retornar
                                    arvore.getArestas(itemPos).getNodo().getArestas(itemPos).setNodo(temp.get(AlGEnArDe.mtw.nextInt(temp.size() - 1)));
                                    return;

                                }
                            } else {
                                //Chamar a função recursivamente até chegar em um nodo raiz
                                efetuarMutacaoArvores(arvore.getArestas(itemPos).getNodo(), prof + 1, tipo, atributo);

                            }

                        } else {
                            //Se o nodo for nulo, seleciona uma Decision Stump aleatóriamente p/ ser incluida no nodo folha
                            arvore.getArestas(itemPos).setNodo(temp.get(AlGEnArDe.mtw.nextInt(temp.size() - 1)));

                        }

                    } else //Se a aresta for nula insere 
                    {
                        //Declaração variáveis e Objetos
                        Arvores arvTempor = temp.get(AlGEnArDe.mtw.nextInt(temp.size() - 1));
                        ArrayList<Atributos> atribs = new ArrayList<>();

                        //Atribuições
                        atribs.add(new Atributos(arvTempor.getNomeAtributo(), null, "", null));

                        //Se o nodo for nulo, seleciona uma Decision Stump aleatóriamente p/ ser incluida no nodo folha e retornar
                        arvore.setArestas(atribs);
                        return;

                    }

                } else {
                    //Se for "REDUÇÃO"
                    //Prcorrer as Arestas
                    for (int i = 0; i < arvore.getArestas().size(); i++) {
                        //Se a aresta não for nula
                        if (arvore.getArestas(i) != null) {
                            //Se o nodo não for nulo
                            if (arvore.getArestas(i).getNodo() != null) {
                                //Se for o Atributo Selecionado Atribuo nulo senão retorno pra pesquisa
                                if (arvore.getArestas(i).getNodo().getNomeAtributo().equals(atributo)) {
                                    //Transformar o Nodo c/ arestas em Nodo Folha (Mutação de "REDUÇÃO") E Sair do processamento
                                    arvore.getArestas(i).setNodo(null);

                                    //Sair fora da execução
                                    break;

                                } else {
                                    //Chamada recursiva da função atualizando o nível de _profundidade
                                    efetuarMutacaoArvores(arvore.getArestas(i).getNodo(), prof + 1, tipo, atributo);

                                }

                            }

                        }

                    }

                }

            }

        } catch (IOException e) {
            e.printStackTrace();

        }

    }
    //</editor-fold>        

    //<editor-fold defaultstate="collapsed" desc="Funções Destinadas ao Crossover">
    //Remover uma Sub-Árvore da Árvore atual e setar nulo a mesma
    public Arvores PesquisarPosicaoArvoreOrigem(Arvores arvore, String atributo, int prof) {
        //Se a árvore não for nula
        if (arvore == null) {
            return null;

        }

        //Se possuir arestas válidas
        if (arvore.getArestas().isEmpty()) {
            return null;

        }

        //Condição de Parada - Se o grau de _profundidade for máximo
        if (prof <= AlGEnArDe._profundidade) {
            //Percorrer todas as arestas
            for (int i = 0; i < arvore.getArestas().size(); i++) {
                //Se a aresta selecionada não for nula
                if (arvore.getArestas(i) != null) {
                    //Se o nodo não for nulo
                    if (arvore.getArestas(i).getNodo() != null) {
                        //Se for o nodo da aresta selecionado aleatóriamente
                        if (arvore.getArestas(i).getNodo().getNomeAtributo().equals(atributo)) {
                            //Setar os Atributos
                            return arvore.getArestas(i).getNodo();

                        }

                        //Chamar Recursivamente a função até encontrar o nodo
                        PesquisarPosicaoArvoreOrigem(arvore.getArestas(i).getNodo(), atributo, prof + 1);

                    }

                }

            }

        }

        //Definir o retorno
        return arvore;

    }

    //Pesquisar um nodo folha da árvore de destino p/ inserção da Sub-Árvore
    public Arvores atribuirSubArvorePosicaoArvoreDestino(Arvores arvore, int prof, String atributo, Arvores arvTemp) {
        try {
            //Se a árvore for nula
            if (arvore == null) {
                //Definir o retorno
                return null;

            }

            //Se a árvore não possuir arestas
            if (arvore.getArestas().isEmpty()) {
                return null;

            }

            //Condição de Parada - Se o grau de _profundidade for máximo
            if (prof <= AlGEnArDe._profundidade) {
                //percorrer as arestas
                for (int i = 0; i < arvore.getArestas().size(); i++) {
                    //Se a aresta não for nula
                    if (arvore.getArestas(i) != null) {
                        //Se o nodo não for nulo
                        if (arvore.getArestas(i).getNodo() != null) {
                            //Se for o nodo da aresta selecionado aleatóriamente
                            if (arvore.getArestas(i).getNodo().getNomeAtributo().equals(atributo)) {
                                //Atribuição
                                arvore.getArestas(i).setNodo(arvTemp);

                                //Setar os Atributos
                                return arvore;

                            }

                            //Chamada Recursiva da Função p/ Avaliação da Sub-Árvore informada
                            atribuirSubArvorePosicaoArvoreDestino(arvore.getArestas(i).getNodo(), prof + 1, atributo, arvTemp);

                        }

                    }

                }

            }

        } catch (Exception e) {
            e.printStackTrace();

        }

        //Definir o retorno
        return arvore;

    }

    //Atribui ao nodo folha a(s) classe(s) a qual pertence, se não existe atribui a mesma senão atualiza a _quantidade em um unidade(caso exista a mesma)
    public void definirClasseNodosFolhas(Arvores arvore, Instance avaliacao, int prof, int profMaxima) {
        try {
            //Se a árvore não for nula
            if (arvore == null) {
                return;

            }

            //Se possuir arestas válidas
            if (arvore.getArestas().isEmpty()) {
                return;

            }

            //Condição de Parada - Se o grau de _profundidade for máximo
            if (prof <= (profMaxima + 1)) {
                //Declaração Variáveis e objetos
                int posicao = 0;

                //Percorrer todos os atributosArv da instância selecionada (Exceto o atribArv1 Classe)
                for (int k = 0; k < avaliacao.numAttributes(); k++) {
                    //Se o nome do Atributo Classe for igual ao nome do atribArv1 da instância (Raiz ou nodo folha) - DEVIDO A NÃO TER ACESSO PELO NOME DO ATRIBUTO
                    if (avaliacao.attribute(k).name().equals(arvore.getNomeAtributo())) {
                        //Atribuições
                        posicao = k;

                        //Sair fora do for
                        break;

                    }

                }

                //Se o atribArv1 for "Numérico" (BIFURCAÇÃO)
                if (avaliacao.attribute(posicao).isNumeric()) {
                    //Declaração Variáveis e Objetos (Pega a aresta da posição 0 para saber qual delas deve-se selecionar já que a árvore será bifurcada)
                    double valorAresta = Double.valueOf(arvore.getArestas(0).getAtributo().split(" ")[1]);

                    //Se o valor da posição FOR MENOR OU IGUAL ao valor do atribArv1 selecionado (Então posição igual a 0 SENAO 1)
                    int itemPos = arredondarValor(avaliacao.value(posicao), AlGEnArDe._qtdDecimais, 1) <= arredondarValor(valorAresta, AlGEnArDe._qtdDecimais, 1) ? 0 : 1;

                    //se a arestas for válida
                    if (arvore.getArestas(itemPos) != null) {
                        //Se não for um nodo RAIZ, efetua a chamada recursiva da função até chegar em um nodo raiz
                        if (arvore.getArestas(itemPos).getNodo() != null) {
                            //Chama a função recursivamente passando o nodo da aresta
                            definirClasseNodosFolhas(arvore.getArestas(itemPos).getNodo(), avaliacao, prof + 1, profMaxima);

                        } else {
                            //Declaração Variáveis e Objetos
                            ArrayList<Classes> classes = new ArrayList<>();

                            //Se a Classe for vazia Inclui o mesmo (Sendo o 1° Registro)
                            if (arvore.getArestas(itemPos).getClasses() == null) {
                                //Adicionar a Nova classe 
                                classes.add(new Classes(avaliacao.classAttribute().value((int) avaliacao.classValue()), 1));

                                //Atribuir as classes e sair fora da execução para a aresta selecionada
                                arvore.getArestas(itemPos).setClasses(classes);

                            } else //Já Existem Registros na Classe, irá atualizar o mesmo
                            {
                                //Declaração Variáveis e Objetos
                                boolean processar = false;
                                classes = arvore.getArestas(itemPos).getClasses();

                                //Percorre TODAS as classes do Nodo
                                for (Classes classe : classes) {
                                    //Se o valor da aresta FOR IGUAL AO VALOR DO ATRIBUTO DA INSTÂNCIA                                            
                                    //Se o "VALOR" da classe DA INSTÂNCIA SELECIONADA FOR IGUAL a da classe informada atualiza a _quantidade
                                    if (avaliacao.classAttribute().value((int) avaliacao.classValue()).equals(classe.getNome())) {
                                        //Atualizar a _quantidade (Adicionando 1) de registros X Atributo - Para Definir a Classe dominante
                                        classe.somarQuantidade(1);
                                        processar = true;

                                    }

                                }

                                //Se não existe a CLASSE avaliada insere a mesma
                                if (!processar) {
                                    //Se não for nulo (ser o último)
                                    if (avaliacao.classAttribute() != null) {
                                        //Adicionar a Nova classe e atualizar a _quantidade
                                        classes.add(new Classes(avaliacao.classAttribute().value((int) avaliacao.classValue()), 1));

                                    }

                                }

                                //Atribuir as classes e sair fora da execução para a aresta selecionada
                                arvore.getArestas(itemPos).setClasses(classes);

                            }

                        }

                    }

                } else { //Se for uma atribArv1 "Categorico" (Terá N-1 Arestas)                 
                    //Declaração Variáveis Objetos
                    int itemPos = 0;

                    //Percorrer todas as arestas da árvore
                    for (int i = 0; i < arvore.getArestas().size(); i++) {
                        //Se for o atributo avaliado for iagual ao atributo selecionado
                        if (arvore.getArestas(i).getAtributo().equals(avaliacao.attribute(posicao).value((int) avaliacao.classValue()))) {
                            //Atribuir a posição encontrada e sair fora
                            itemPos = i;
                            break;

                        }

                    }

                    //Se a posição não for nula
                    if (arvore.getArestas(itemPos) != null) {
                        //Se não for um nodo RAIZ, efetua a chamada recursiva da função até chegar em um nodo raiz
                        if (arvore.getArestas(itemPos).getNodo() != null) {
                            //Chama a função recursivamente passando o nodo da aresta
                            definirClasseNodosFolhas(arvore.getArestas(itemPos).getNodo(), avaliacao, prof + 1, profMaxima);

                        } else {
                            //Declaração Variáveis e Objetos
                            ArrayList<Classes> classes = new ArrayList<>();

                            //Se a Classe for vazia Inclui o mesmo (Sendo o 1° Registro)
                            if (arvore.getArestas(itemPos).getClasses() == null) {
                                //Percorrer as arestas existentes
                                for (int j = 0; j < arvore.getArestas().size(); j++) {
                                    //Adicionar a Nova classe 
                                    classes.add(new Classes(avaliacao.classAttribute().value(j), 1));

                                }

                                //Atribuir as classes e sair fora da execução para a aresta selecionada
                                arvore.getArestas(itemPos).setClasses(classes);

                            } else //Já Existem Registros na Classe, irá atualizar o mesmo
                            {
                                //Declaração Variáveis e Objetos
                                classes = arvore.getArestas(itemPos).getClasses();

                                //Percorrer todas as classes da Aresta
                                for (Classes classe : classes) {
                                    //Percorrer todos os valores existentes da instância selecionada (MENOS O ATRIBUTO CLASSE)
                                    //for (int l = 0; l < avaliacao.numValues() - 1; l++) {
                                        //Se o nome do Atributo FOR IGUAL AO NOME DO ATRIBUTO da instancia de avaliação selecionada
                                        if (arvore.getArestas(itemPos).getAtributo().equals(avaliacao.attribute(posicao).value(itemPos))) {
                                            //Se o nome da classe dominante for igual a classe avaliada
                                            if (classe.getNome().equals(avaliacao.classAttribute().value((int) avaliacao.classValue()))) {
                                                //Atualizar a _quantidade de registros X Atributo - Para Definir a Classe dominante
                                                classe.somarQuantidade(1);

                                            }

                                        }

                                    //}

                                }

                            }

                        }

                    }

                }

            }

        } catch (NumberFormatException e) {
            e.printStackTrace();

        }

    }

    //Irá percorrer todos os Nodos da árvore(avaliando SOMENTE os _nodos folhas) 
    public void atribuirClasseNodosFolhas(Arvores arvore, int prof, int profMaxima) throws NullPointerException {
        //Se a árvore não for nula
        if (arvore == null) {
            return;

        }

        //Se possuir arestas válidas
        if (arvore.getArestas().isEmpty()) {
            return;

        }

        //Condição de Parada - Se o grau de _profundidade for máximo
        if (prof <= (profMaxima + 1)) {
            //Se o árvore não for nula
            if (!arvore.getArestas().isEmpty()) {
                //Percorrer TODAS as arestas do árvore selecionado para atribuir uma classe as folhas
                for (int i = 0; i < arvore.getArestas().size(); i++) {
                    //Se a aresta selecionada não for NULA pesquisa pela mesma (NULA == Nodo Folha)
                    if (arvore.getArestas(i).getNodo() != null) {
                        //Chamada recursiva da função passando como parâmetros a aresta selecionada
                        atribuirClasseNodosFolhas(arvore.getArestas(i).getNodo(), prof + 1, profMaxima);

                    } else //Chegou em um nodo folha
                    {
                        //Declaração Variáveis e Objetos
                        ArrayList<Classes> classes = arvore.getArestas(i).getClasses();

                        //Se não for nulo
                        if (classes != null) {
                            //Declaração Variáveis e Objetos
                            String clsDominante = "";
                            double qtdOcorrCls = 0;

                            //Percorre todas as Classes
                            for (Classes classe : classes) {
                                //Se for a 1° Ocorrência
                                if (clsDominante.isEmpty()) {
                                    //Atribuições do nome da classe e da _quantidade
                                    clsDominante = classe.getNome();
                                    qtdOcorrCls = classe.getQuantidade();

                                } else {
                                    //Se a _quantidade for MAIOR que a ATUAL ALTERA a classe SENÃO mantém a mesma
                                    if (classe.getQuantidade() > qtdOcorrCls) {
                                        //Atribuições do nome da classe e da _quantidade
                                        clsDominante = classe.getNome();
                                        qtdOcorrCls = classe.getQuantidade();

                                    }

                                }

                            }

                            //Setar a classe Majoritária
                            arvore.getArestas(i).setClasseDominante(clsDominante);

                        }

                    }

                }

            }

        }

    }

    private String BuscarAtributosArvore(Arvores arv) {
        //Declaração Variáveis e Objetos e Inicializações
        String nomeatributo = "";

        try {
            //Declaração Variáveis e Objetos e Inicializações
            ArrayList<String> atributos = new ArrayList<>();

            //Chamada Recursiva p/ montagem dos atributos da árvore(apartir do nodo raiz)
            processarNomesAtributos(arv, 1, atributos);

            //Se for diferente de nulo(carregou algum atribuito)
            if (!atributos.isEmpty()) {
                //Não poderá retornar o nodo raiz, por isto a exclusão do nodo na posição 0, sendo assim remove-se o nodo raiz
                atributos.remove(0);

                //Se possuir Árvores 
                if (atributos.size() > 0) {
                    //Se o tamanho for 1, pega o único existente (sem sorteio) SENÃO Sorteia um entre os possíveis
                    nomeatributo = (atributos.size() <= 1) ? atributos.get(0) : atributos.get(AlGEnArDe.mtw.nextInt(atributos.size() - 1));

                }

            }

        } catch (Exception e) {
            return "Erro Atributos.: " + e.getMessage();

        }

        //Definir o retorno
        return nomeatributo;

    }

    //Localizar os atributos(nomes) existentes na árvore, aonde será sorteado um deles para mutação (EXCETO o nodo Raiz)
    private void processarNomesAtributos(Arvores arv, int prof, ArrayList<String> atributos) {
        try {
            //Se for nula retorna
            if (arv == null) {
                return;

            }

            //Se não possuir arestas válidas
            if (arv.getArestas() == null) {
                return;

            }

            //Condição de Parada - Se o grau de _profundidade for máximo
            if (prof <= AlGEnArDe._profundidade) {
                //Percorrer todas as arestas
                for (int i = 0; i < arv.getArestas().size(); i++) {
                    //Se for vazio Adiciona senão avalia e depois insere sim ou não
                    if (atributos.isEmpty()) {
                        //Adicionar o nome do Atributo SE não Contiver
                        atributos.add(arv.getNomeAtributo());

                    } else {
                        //Se não contiver o atribArv1            
                        if (!atributos.contains(arv.getNomeAtributo())) {
                            //Adicionar o Atributo
                            atributos.add(arv.getNomeAtributo());

                        }

                    }

                    //Chamada recursiva da árvore passando o nodo selecionado
                    processarNomesAtributos(arv.getArestas(i).getNodo(), prof + 1, atributos);

                }

            }

        } catch (Exception e) {
            e.printStackTrace();

        }

    }
    //</editor-fold>        

    //<editor-fold defaultstate="collapsed" desc="Calcular o Valor Médio da Árvores(arestas) - Para Atributos Numéricos">    
    //Efetuar o Cálculo do Indice Gini p/ Atributos Contínuos 
    //Por Exemplo Indice Gini = 1 - (Somatório Quant. Atrib. "A" / Total de Instâncias) ^ 2 - (Somatório Quant. Atrib. "N" / Total de Instâncias) ^ 2.
    private double calcularIndiceGini(Instances dados, int pos) {
        //Declaração Variáveis e objetos
        List<IndiceGini> filtrados, valores = new ArrayList<>();
        List<Double> indice = new ArrayList<>();
        ArrayList<Classes> regs;

        try {
            //Criar o filtro lógico
            Filter<IndiceGini, Double> filtro = new Filter<IndiceGini, Double>() {
                @Override
                public boolean isMatched(IndiceGini object, Double valor) {
                    return (object.getValor() == valor);

                }
            };

            //Pegar distintamente os valores p/ calcular a média de todas as instâncias do atribArv1 informado, adicionar os valores das instâncias na posição informada
            for (int i = 0; i < dados.numInstances(); i++) {
                //Filtragem das ocorrências pelo Atributo "Valor"
                filtrados = new FilterList().filterList(valores, filtro, dados.instance(i).value(pos));
                String clsAtributo = dados.instance(i).classAttribute().value((int) dados.instance(i).classValue());

                //Se não encontrou registros, inclui o mesmo
                if (filtrados.isEmpty()) {
                    //Criação do Objeto
                    regs = new ArrayList<>();

                    //Adicionar as propriedades
                    regs.add(new Classes(clsAtributo, 1));

                    //Incluir o Valor
                    valores.add(new IndiceGini(dados.instance(i).value(pos), regs));

                } else {
                    //Declaração Variáveis e Objetos
                    boolean bOk = false;

                    for (IndiceGini sel : filtrados) {
                        //Somente poderá encontrar um registro e ai percorre as suas classes
                        for (Classes atr : sel.getClsAtribruto()) {
                            //Se o nome do atribArv1 atribArv1 for igual
                            if (atr.getNome().equals(clsAtributo)) {
                                //Atualizar a _quantidade, e sair fora da pesquisa
                                atr.somarQuantidade(1);
                                bOk = true;
                                break;

                            }

                        }

                        //Se não for nenhum deles
                        if (!bOk) {
                            //Adicionar as propriedades
                            sel.getClsAtribruto().add(new Classes(dados.instance(i).classAttribute().value((int) dados.instance(i).classValue()), 1));

                        }

                    }

                }

            }

            //Ordenar Crescente dos atributosArv
            Collections.sort(valores);

            //Percorrer os valores cadastrados
            for (IndiceGini item : valores) {
                //Declaração Variáveis e objetos
                int total = 0;
                double indGini = 1d;

                if (item.getClsAtribruto() != null) {
                    //Percorrer todos os itens
                    for (Classes clsItem : item.getClsAtribruto()) {
                        //Totalizar a _quantidade
                        total += clsItem.getQuantidade();

                    }

                    //Percorrer todos os itens
                    for (Classes cls : item.getClsAtribruto()) {
                        //Calcular o Indice Gini
                        indGini -= Math.pow(((double) cls.getQuantidade() / total), 2);

                    }

                }

                //Adicionar o Indice Gini Calculado
                indice.add(arredondarValor(indGini, AlGEnArDe._qtdDecimais, 1));

            }

            //Ordenar os indices em ordem crescente
            Collections.sort(indice);

        } catch (Exception e) {
            System.out.println(e.getMessage());

        }

        //Definir o Retorno (se o Índice Gini for "EXATAMENTE" 1 deverá ser 0)
        return indice.get(0) == 1 ? 0 : indice.get(0);

    }

    //Parâmetros: 1 - Valor a arredondarValor. 
    //            2 - Quantidade de casas depois da vírgula. 
    //            3 - arredondarValor para cima ou para baixo? Para Cima = 0 (ceil) Para Baixo = 1 ou qualquer outro inteiro (floor)
    public double arredondarValor(double valor, int casas, int ACimaouABaixo) {
        //Atribuições do Cálculo
        valor *= (Math.pow(10, casas));
        valor = (ACimaouABaixo == 0 ? Math.ceil(valor) : Math.floor(valor));
        valor /= (Math.pow(10, casas));

        //Definir o Retorno
        return valor;

    }
    //</editor-fold>        

}
