package prjageda;

public class PrjAGEDA {
    //>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
    //
    //  REQUISITOS MÍNIMOS NECESSÁRIOS
    //
    //  1°) Arquivo de Teste p/ Atributos Numéricos c/ classe definida
    //  2°) Arquivo de Teste p/ Atributos Nominais c/ classe definida
    //    
    //>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>

    //Arquivo com Atributos Numéricos
    private static final String _arquivo = "C:\\ArffTeste\\ionosphere.arff";
    
    //Arquivo com Atributos Nominais
    //private static final String _arquivo = "C:\\ArffTeste\\vote.arff";

    public static void main(String[] args) throws Exception, Throwable {
        /**
         * -------------------------------------------------------------------------------------------------------------------------------------------------------------------------
         * 01° - Leitura do(s) Arquivo(s) <nome(s)>.arff 
         * 02° - Processamento das Instâncias (Decision Stumps) - Criação das Árvores com seus atributos { Numérico - Bifurcado / Demais - Quantidade de atributos existentes } 
         * 03° - Geração das Árvore e das Sub-Árvores - Sorteia-se o nodo raiz de forma aleatória - Percorrer até atingir o nível de profundidade definido por parâmetro - Para cada
         *       atributos da Árvore será sorteado c/ 50% de probabilidade a inserção de uma nova Sub-Árvore - Retornar a Árvore com todas as suas Sub-Árvores geradas 
         * 04° - Geração da População Inicial até atingir a quantidade de Árvores parametrizada 
         * 05° - Efetuado o Treinamento das Árvores (para definição
         * das classes dos nós folhas E da classe dominante de cada um dos nodos folhas) 
         * 06° - Efetuado a Avaliação das Árvores (quantidade de acertos encontrados) 
         * 07° - Cálculo do Fitness de todas as Árvores - Somatório dos acertos DIVIDINDO-SE pelo Total de Instâncias avaliadas 
         * 08° - Ordenação da População Crescentemente (0.2, 0.3, 0.4 ...) 
         * 09° - Utilização do Elitismo p/ a geração futura aonde sempre leva-se os 2 melhores Árvores da geração atual 
         * 10° - Crossover - Sorteia-se 2 Árvores Aleatóriamente e efetua-se a troca genética entre elas criando 2 Novas Árvores 
         * 11° - Mutação - Percorre-se todos os árvores da nova geração(exceto as 2 primeiras devido ao Elitismo) e sorteando um valor, e se o mesmo estiver no intervalo 
         * estabelecido efetua o processo que consiste na troca material genético da própria Árvore, que poderá ser: - "E"xpansão - Adiciona-se alguma das Decisions Stumps 
         * (sorteada aleatóriamente) dentre as existentes - "R"edução - Transforma-se um nodo e todas as suas sub-árvores em um nodo folha
         * 12° - Repete-se a partir do passo 07° Novamente até atingir o nro de Gerações 13° - Retorna a melhor árvore
         *
         * >>>>>>>>>>>>>>>>>>>>>>>>>>>>>> ATENÇÃO <<<<<<<<<<<<<<<<<<<<<< ATENÇÃO <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<< ATENÇÃO <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
         *
         * OBSERVAÇÃO: 1°) A Opção DEEPCOPY não funciona para objetos do tipo "ArrayList<>", ou seja, apartir da posição 1 o mesmo guardará as referências do Objetos vinculado 2°)
         * Cuidar com o nível de profundidade da árvore, quando do "CROSSOVER E DA MUTAÇÃO", poderá ocorrer caso em que o nível de profundidade da árvore DUPLIQUE.
         *
         * >>>>>>>>>>>>>>>>>>>>>>>>>>>>>> ATENÇÃO <<<<<<<<<<<<<<<<<<<<<< ATENÇÃO <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<< ATENÇÃO <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
         ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------
         */
        new AlGEnArDe().AlGenArDe(new Processamento(_arquivo).lerArquivoDados());

    }

}
