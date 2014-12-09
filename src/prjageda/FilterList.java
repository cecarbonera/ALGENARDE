package prjageda;

import java.util.ArrayList;
import java.util.List;

public class FilterList<E> {

    /**
     * @param <T> Tipo de Objeto
     * @param original - Listagem original
     * @param filter - Objeto p/ aplicação do Filtro
     * @param text - Condição de Filtro
     * @return - Registros Filtrados
     */
    @SuppressWarnings("unchecked")
    public  <T> List filterList(List<T> original, Filter filter, E text) {
        //Declaração Variáveis e Objetos
        List<T> lista = new ArrayList<>();
        
        //Percorrer a lista orginal
        for (T obj : original) {
            //Se encontrou
            if (filter.isMatched(obj, text)) {
                //Adiciona o objeto
                lista.add(obj);
                
            } 
            
        }
    
        //Definir o retorno
        return lista;
        
    }
    
}