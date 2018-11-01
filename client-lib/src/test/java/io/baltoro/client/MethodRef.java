package io.baltoro.client;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class MethodRef 
{
 
    /**
     * @param args the command line arguments
     */
 
 
    public static void main(String[] args) 
    {
        // TODO code application logic here
        List names = new ArrayList();
            names.add("David");
            names.add("Richard");
            names.add("Samuel");
            names.add("Rose");
            names.add("John");
 
 
           MethodRef.printNames(names,System.out::println);
 
    }
 
 
    private static void printNames(List list, Consumer c )
    {
        list.forEach(x -> c.accept(x));
    }
}