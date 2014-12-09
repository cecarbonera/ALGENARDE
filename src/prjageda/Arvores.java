package prjageda;

import java.util.ArrayList;

public class Arvores implements Comparable<Arvores> {

    //<editor-fold defaultstate="collapsed" desc="1° Definição dos Atributos e método Inicializador da classe">    
    private String nomeAtr;
    private double fitness;
    private int qtdOcorr;
    private ArrayList<Atributos> arestas;
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="2° Métodos Inicializadores da classe e Get´s E Set´s">
    public Arvores() {
        //Inicializações
        this.nomeAtr = "";
        this.fitness = 0d;
        this.qtdOcorr = 0;
        this.arestas = null;

    }

    public Arvores(String nome, ArrayList<Atributos> galhos) {
        //Atribuições
        this.nomeAtr = nome;
        this.fitness = 0d;
        this.qtdOcorr = 0;
        this.arestas = galhos;

    }

    public String getNomeAtributo() {
        //Definir o retorno
        return nomeAtr == null ? "" : !nomeAtr.isEmpty() ? this.nomeAtr : "";

    }

    public void setNomeAtributo(String nomeAtr) {
        this.nomeAtr = nomeAtr;

    }

    public ArrayList<Atributos> getArestas() {
        //Retornar as arestas desde que existam
        return this.arestas.isEmpty() ? new ArrayList<Atributos>() : this.arestas;

    }

    public Atributos getArestas(int pos) {
        //Retornar a aresta da posição desde que exista
        if (!this.arestas.isEmpty()) {
            //Se posição for inválida retorna Nulo
            if (pos >= this.arestas.size()) {
                return null;

            }

            if (this.arestas.get(pos) != null) {
                return this.arestas.get(pos);

            } else {
                return null;

            }

        } else {
            return null;

        }

    }

    public void SetNodo(Atributos atr, Arvores arv) {
        atr.setNodo(arv);

    }

    public void setArestas(ArrayList<Atributos> arestas) {
        this.arestas = arestas == null ? null : arestas;

    }

    public void setArvore(Arvores arv) {
        //Atribuições
        this.nomeAtr = arv.getNomeAtributo();
        this.fitness = arv.getFitness();
        this.qtdOcorr = arv.getQtdOcorrencias();
        this.arestas = arv.getArestas();

    }

    public void removerAresta(int pos) {
        try {
            if (this.arestas == null) {
                return;
            }

            if (this.arestas.get(pos) != null) {
                this.arestas.remove(pos);

            }

        } catch (Exception e) {
            System.out.println(e.getMessage());

        }

    }

    public Arvores getArvoreApartirAresta(int pos) {
        //Retornar a aresta da posição desde que exista
        if (this.arestas != null) {
            //Se posição for inválida retorna Nulo
            if (pos >= this.arestas.size()) {
                return null;

            }

            if (this.arestas.get(pos).getNodo() != null) {
                return this.arestas.get(pos).getNodo();

            } else {

                return null;

            }

        } else {
            return null;

        }

    }

    public double getFitness() {
        return this.fitness;

    }

    public void setFitness(double fitness) {
        this.fitness = fitness;

    }

    public int getQtdOcorrencias() {
        return this.qtdOcorr;
    }

    public void setQtdOcorrencias(int qtd) {
        this.qtdOcorr = qtd;
    }

    public void AtuQtdOcorrencias(int qtd) {
        this.qtdOcorr += qtd;
    }
    //</editor-fold>   

    //<editor-fold defaultstate="collapsed" desc="3° Métodos de Ordenação dos registros e Copia Profunda">
    @Override
    public int compareTo(Arvores obj) {
        //Efetuar a ordenação dos registros crescente, por exemplo.: 0.1, 0.2, 0.3...1.0
        return (this.getFitness() < obj.getFitness()) ? -1 : (this.getFitness() > obj.getFitness()) ? 1 : 0;

    }
    //</editor-fold>    

}
