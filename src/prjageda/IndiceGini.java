package prjageda;

import java.util.ArrayList;

public class IndiceGini implements Comparable<IndiceGini> {

    //<editor-fold defaultstate="collapsed" desc="1° Definição dos Atributos e método Inicializador da classe">    	
    private double valor;
    private ArrayList<Classes> clsAtribruto;

    public IndiceGini() {
        //setar o atributo
        this.valor = 0d;
        this.clsAtribruto = null;

    }

    public IndiceGini(double vlr, ArrayList<Classes> cls) {
        //setar o atributo
        this.valor = vlr;
        this.clsAtribruto = cls;

    }
    //</editor-fold>        
   
    //<editor-fold defaultstate="collapsed" desc="2° Definição dos Get´s e Set´s e demais métodos">
    public double getValor() {
        return this.valor;
    }

    public void setValor(double valor) {
        this.valor = valor;
    }

    public ArrayList<Classes> getClsAtribruto() {
        return this.clsAtribruto;
        
    }

    public void setClsAtribruto(ArrayList<Classes> clsAtribruto) {
        this.clsAtribruto = clsAtribruto;
        
    }
    //</editor-fold>
    
    //<editor-fold defaultstate="collapsed" desc="3° Definição Métodos de Ordenação">
    @Override
    public int compareTo(IndiceGini o) {
        //Definir o retorno
        return this.valor == o.getValor() ? 0 : (this.valor > o.getValor() ? 1 : -1);
        
    }
    //</editor-fold>

}