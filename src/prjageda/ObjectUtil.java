package prjageda;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

public final class ObjectUtil {

    private ObjectUtil() {
    }

    @SuppressWarnings("unchecked")
    public static <T> T deepCopy(T objeto) {
        try {
            //Se o objeto for nulo retorna
            if (objeto == null) {
                return null;

            }

            //Declaração Variáveis e Objetos
            Class<?> classe = objeto.getClass();
            T clone = (T) classe.newInstance();
            Field[] campos = classe.getDeclaredFields();

            //percorrer todos os objetos
            for (Field obj : campos) {
                //setar o objeto como acessível
                obj.setAccessible(true);

                if (!Modifier.isFinal(obj.getModifiers())) {
                    //Se o objeto for uma instância do Objeto avaliado
                    if (obj.get(objeto) instanceof List<?>) {
                        //Copiar os objetos
                        List<?> copiados = deepCopyList((List<?>) obj.get(objeto));
                        obj.set(clone, copiados);

                    } else {
                        //Setar um clone do objetos
                        obj.set(clone, obj.get(objeto));

                    }

                }

            }

            //Enquanto verdadeiro
            while (true) {
                //Se for igual a classe sair fora
                if (Object.class.equals(classe)) {
                    break;
                }

                //Declaração Variáveis e Objetos e inicializações
                classe = classe.getSuperclass();
                Field[] sCampos = classe.getDeclaredFields();

                for (Field _campo : sCampos) {
                    //Setar acessível
                    _campo.setAccessible(true);

                    if (!Modifier.isFinal(_campo.getModifiers())) {
                        //Se o objeto for uma instância do Objeto avaliado
                        if (_campo.get(objeto) instanceof List<?>) {
                            //Copiar os objetos
                            List<?> listaCopiada = deepCopyList((List<?>) _campo.get(objeto));
                            _campo.set(clone, listaCopiada);

                        } else {
                            //Setar um clone do objetos
                            _campo.set(clone, _campo.get(objeto));

                        }

                    }

                }

            }
            
            //Definir o retorno
            return clone;

        } catch (InstantiationException | IllegalAccessException e) {
            return null;

        }

    }

    public static <T> List<T> deepCopyList(List<T> objetos) {
        //Se o objeto for nulo retorna
        if (objetos == null) {
            return null;

        }
        //Declaração variáveis e Objetos
        List<T> retorno = new ArrayList<>();

        //Adicionar todos os objetos
        for (T obj : objetos) {
            retorno.add(deepCopy(obj));
        }

        //Definir o retorno
        return retorno;

    }

}
