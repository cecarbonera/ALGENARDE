package prjageda;

import java.util.ArrayList;

public class Atributos {

    //<editor-fold defaultstate="collapsed" desc="1° Definição dos Atributos e método Inicializador da classe">    	
    private String atributo;
    private String classeDominante;
    private ArrayList<Classes> classes;
    private Arvores nodo;

    public Atributos() {
        //Inicialização dos atributos
        this.atributo = "";
        this.classeDominante = "";
        this.nodo = null;
        this.classes = null;

    }

    public Atributos(String atr, Arvores no, String clsDominante, ArrayList<Classes> cls) {
        //Inicialização dos atributos
        this.atributo = atr;
        this.classeDominante = clsDominante;
        this.nodo = no;
        this.classes = cls;

    }
    //</editor-fold>    

    //<editor-fold defaultstate="collapsed" desc="2° Definição dos Get´s e Set´s e demais métodos">        
    public void setAtributo(String atributo) {
        this.atributo = atributo;
    }

    public String getAtributo() {
        return this.atributo;

    }

    public Arvores getNodo() {
        //Retornar o nodo
        return this.nodo;

    }

    public void setNodo(Arvores nodo) {
        this.nodo = nodo;

    }

    public void setNodo(Atributos atr, Arvores arv) {
        atr.setNodo(arv);

    }

    public String getClasseDominante() {
        //Forçar a conversão em String caso o objeto seja nulo
        return String.valueOf(this.classeDominante).trim();

    }

    public void setClasseDominante(String classe) {
        this.classeDominante = classe;

    }

    public ArrayList<Classes> getClasses() {
        return this.classes == null ? null : this.classes;

    }

    public void setClasses(ArrayList<Classes> classes) {
        this.classes = classes;

    }
    //</editor-fold>        

}
