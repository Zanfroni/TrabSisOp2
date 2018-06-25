/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package t2sisop;

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
    private LinkedList<String[]> instructions = new LinkedList<>();
    
    private String[][] RAM;
    private int[][] VM;
    private int[][] disk;
    private String[][] auxDisk;
    private boolean[] ocuppiedPage;
    private boolean[] fullPage;
    
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
        RAM = new String[physAddress/pageSize][pageSize];
        VM = new int[physAddress/pageSize][pageSize];
        //System.out.println(RAM.length);
        
        //LÊ A QUINTA LINHA
        input = in.readLine();
        //System.out.println(input);
        diskAddress = Integer.parseInt(input);
        if((Integer.parseInt(input)) % pageSize != 0) shutdown();
        disk = new int[diskAddress/pageSize][pageSize];
        auxDisk = new String[diskAddress/pageSize][pageSize];
        System.out.println(disk.length);
        System.out.println(disk[0].length);
        
        populate();
        //System.out.println("ok");
        
        //SE O PROGRAMA FOR ALEATÓRIO, INICIA-SE A CRIAÇÃO DE TODOS
        //OS PROCESSOS E EMBARALHAMENTO DE INSTRUÇÕES
        if(!sequencial){
            //TO DO
        }
        
        //INICIA-SE A LEITURA DAS INSTRUÇÕES (C,A,M)
        adjustInstruction(in, input);
        execute();
        
    }
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    private void execute(){
        String[] inst;
        while(!instructions.isEmpty()){
            inst = instructions.removeFirst();
            switch(inst[0]){
                case "C": instructionC(inst[1], Integer.parseInt(inst[2]));
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
            int pages = (int) Math.ceil((double)memSize/(double)pageSize);
            boolean foundPage = false;
            LinkedList<Integer> foundPages = new LinkedList<>();
            for(int i = 0; i < ocuppiedPage.length; i++){
                if(ocuppiedPage[i] == false){
                    foundPages.add(i);
                    pages--;
                }
                if(pages == 0){
                    foundPage = true;
                    break;
                }
            }
            
            if(!foundPage){
                return;
                //IMPRIMIR QUE DEU FALTA DE MEMÓRIA
            }
            
            Process newProc = new Process(id, memSize);
            process.add(newProc);
            processNames.add(id);
            
            /*for(int i = 0; i < process.size(); i++){
                System.out.print(process.get(i).getId());
            }*/
            
            newProc.setPages(foundPages);
            int currentAd = newProc.getCurrentAddress();
            
            System.out.println("penis " + foundPages.size());
            for(int i = 0; i < foundPages.size(); i++){
                int actualPage = foundPages.get(i);
                ocuppiedPage[actualPage] = true;
                int k = 0;
                System.out.println("jdsisjd  "  + actualPage);
                for(int j = 0; j < RAM[0].length; j++){
                    RAM[actualPage][j] = newProc.getId();
                    VM[actualPage][j] = currentAd;
                    currentAd++;
                    k++;
                    memSize--;
                    System.out.println(memSize);
                    if(k == pageSize-1) fullPage[actualPage] = true;
                    if(memSize == 0) break;
                    //System.out.println("corno "  + RAM.length);
                }
                if(memSize == 0) break;
            }
            newProc.setCurrentAddress(currentAd);
            
        }
    }
    
    //MÉTODO QUE APLICA A SEPARAÇÃO DAS INSTRUÇÕES
    private void adjustInstruction(BufferedReader in, String input) throws IOException{
        String[] inst;
        while((input = in.readLine()) != null){
            inst = input.split(" ");
            instructions.add(inst);
        }
        
        /*for(int i = 0; i < instructions.size(); i++){
            String lixo[] = instructions.get(i);
            System.out.print(lixo[0] + " ");
            System.out.print(lixo[1] + " ");
            System.out.println(lixo[2]);
        }*/
    }
    
    //MÉTODO QUE POPULA AS MATRIZES E VETORES DE INTERESSE
    private void populate(){
        for(int i = 0; i < RAM.length; i++){
            for(int j = 0; j < RAM[0].length; j++){
                RAM[i][j] = "X";
                VM[i][j] = -1;
            }
        }
        
        for(int i = 0; i < disk.length; i++){
            for(int j = 0; j < disk[0].length; j++){
                auxDisk[i][j] = "X";
                disk[i][j] = -1;
            }
        }
        
        ocuppiedPage = new boolean[physAddress/pageSize];
        fullPage = new boolean[physAddress/pageSize];
        for(int i = 0; i < fullPage.length; i++){
            ocuppiedPage[i] = false;
            fullPage[i] = false;
        }
    }
    
    public void print(){
        for(int i = 0; i < RAM.length; i++){
            for(int j = 0; j < RAM[0].length; j++){
                System.out.print(RAM[i][j] + "\t");
            }
            System.out.println();
        }
        
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        
        for(int i = 0; i < VM.length; i++){
            for(int j = 0; j < VM[0].length; j++){
                System.out.print(VM[i][j] + "\t");
            }
            System.out.println();
        }
        
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        
        for(int i = 0; i < disk.length; i++){
            for(int j = 0; j < disk[0].length; j++){
                System.out.print(disk[i][j] + "\t");
            }
            System.out.println();
        }
        
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        
        for(int i = 0; i < auxDisk.length; i++){
            for(int j = 0; j < auxDisk[0].length; j++){
                System.out.print(auxDisk[i][j] + "\t");
            }
            System.out.println();
        }
        
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        
        for(int i = 0; i < ocuppiedPage.length; i++){
            System.out.print(ocuppiedPage[i] + "\t");
        }
        
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        
        for(int i = 0; i < fullPage.length; i++){
            System.out.print(fullPage[i] + "\t");
        }
        
    }
    
    //MÉTODO QUE FINALIZA O PROGRAMA EM CASO DE ERROS NO ARQUIVO DE ENTRADA
    private void shutdown(){
        System.out.print("O programa foi fechado inesperadamente (condições de entrada não atingidas)");
        System.exit(0);
    }
}
