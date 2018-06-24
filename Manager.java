//package t2sisop;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Scanner;

/**
 *
 * @author Gabriel Franzoni 15105090
 * @author Igor Brehm 16180276
 */
public class Manager {
    
    private boolean sequencial = true;
    private boolean algoritmo_troca_lru = true;
    private int diskAddress, physAddress, pageSize;
    private LinkedList<String> processNames = new LinkedList<>();
    private LinkedList<Process> process = new LinkedList<>();
    
    public Manager() throws FileNotFoundException, IOException{
        System.out.println("Digite o endereco do arquivo de entrada:");
	Scanner scan = new Scanner(System.in);
	String input = "";//scan.nextLine();
        BufferedReader in = new BufferedReader(new FileReader("entrada" + ".txt"));
        
        
        //LÊ A PRIMEIRA LINHA
        input = in.readLine();
        //System.out.println(input);
        if(input.equals("sequencial") || input.equals("0") || input.equals("s")){
            sequencial = true;
        } else if(input.equals("aleatorio") || input.equals("1") || input.equals("a")){
            sequencial = false;
        } else{
            shutdown();
        }
        
        //LÊ A SEGUNDA LINHA
        input = in.readLine();
        //System.out.println(input);
        if(input.equals("lru")){
            algoritmo_troca_lru = true;
        } else if(input.equals("aleatorio")){
            algoritmo_troca_lru = false;
        } else{
            shutdown();
        }
        
        //LÊ A TERCEIRA LINHA
        input = in.readLine();
        //System.out.println(input);
        pageSize = Integer.parseInt(input);
        
        //LÊ A QUARTA LINHA
        input = in.readLine();
        //System.out.println(input);
        physAddress = Integer.parseInt(input);
        if((Integer.parseInt(input)) % pageSize != 0) shutdown();
        
        //LÊ A QUINTA LINHA
        input = in.readLine();
        //System.out.println(input);
        diskAddress = Integer.parseInt(input);
        if((Integer.parseInt(input)) % pageSize != 0) shutdown();
        
        //SE O PROGRAMA FOR ALEATÓRIO, INICIA-SE A CRIAÇÃO DE TODOS
        //OS PROCESSOS E EMBARALHAMENTO DE INSTRUÇÕES
        if(!sequencial){
            //TO DO
        }
        
        //INICIA-SE A LEITURA DAS INSTRUÇÕES (C,A,M)
        execute(in, input);
        
    }
    
    private void execute(BufferedReader in, String input) throws IOException{
        String[] instruction;
        while((input = in.readLine()) != null){
            instruction = adjustInstruction(input);
            switch(instruction[0]){
                case "C": instructionC(instruction[1], Integer.parseInt(instruction[2]));
                          break;
                /*case "A": instructionA(instruction);
                          break;
                case "M": instructionM(instruction);
                          break;*/
                default: break;
            }
        }
    }
    
    //MÉTODO QUE CRIA NOVO PROCESSO NAS MEMÓRIAS
    private void instructionC(String id, int memSize){
        if(!processNames.contains(id)){
            process.add(new Process(id, memSize));
            processNames.add(id);
            /*for(int i = 0; i < process.size(); i++){
                System.out.print(process.get(i).getId());
            }*/
            
            int pages = (int) Math.ceil((double)memSize/(double)pageSize);
            //System.out.println(pages);
            
            
        }
    }
    
    //MÉTODO QUE APLICA A SEPARAÇÃO DAS INSTRUÇÕES
    private String[] adjustInstruction(String input){
        String[] instruction = input.split(" ");
        //System.out.println(instruction[0]);
        //System.out.println(instruction[1]);
        //System.out.println(instruction[2]);
        return instruction;
    }
    
    //MÉTODO QUE FINALIZA O PROGRAMA EM CASO DE ERROS NO ARQUIVO DE ENTRADA
    private void shutdown(){
        System.out.print("O programa foi fechado inesperadamente (condições de entrada não atingidas)");
        System.exit(0);
    }
}
