/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package trabsisiop2;

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
    private LinkedList<Integer> lruOrder = new LinkedList<>();
    
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
        if(algoritmo_troca_lru) setLRU();
        
        //LÊ A QUARTA LINHA
        input = in.readLine();
        //System.out.println(input);
        physAddress = Integer.parseInt(input);
        if((Integer.parseInt(input)) % pageSize != 0) shutdown();
        RAM = new String[pageSize][physAddress/pageSize];
        VM = new int[pageSize][physAddress/pageSize];
        //System.out.println(RAM.length);
        
        //LÊ A QUINTA LINHA
        input = in.readLine();
        //System.out.println(input);
        diskAddress = Integer.parseInt(input);
        if((Integer.parseInt(input)) % pageSize != 0) shutdown();
        disk = new int[diskAddress/pageSize][physAddress/pageSize];
        auxDisk = new String[diskAddress/pageSize][physAddress/pageSize];
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
                case "A": instructionA(inst[1], Integer.parseInt(inst[2]));
                          break;
                case "M": instructionM(inst[1], Integer.parseInt(inst[2]));
                          break;
                /*case "T": instructionT(inst[1], Integer.parseInt(inst[2]));
                          break;*/
               default: break;
            }
        }
    }
    
    private void instructionM(String id, int memSize){
        if(processNames.contains(id)){
            boolean foundPage = false;
            LinkedList<Integer> foundPages = new LinkedList<>();
            int adSize = physAddress/pageSize;
            boolean inside = false;
            int pages = (int) Math.ceil((double)memSize/(double)adSize);
            for(int i = 0; i < RAM.length; i++){
                if(!fullPage[i]){
                    if(RAM[i][0].equals("X")){
                        foundPages.add(i);
                        pages--;
                    }if(RAM[i][0].equals(id)){
                        int aux = memSize;
                        for(int j = 0; j < RAM[0].length; j++){
                            if(RAM[i][j].equals("X")){
                                aux--;
                                if(aux == 0){
                                    inside = true;
                                    break;
                                }
                            }
                        }
                        if(inside){
                            foundPages.add(i);
                            pages--;
                        }
                    }
                }
                if(pages == 0){
                    foundPage = true;
                    break;
                }
            }
            
            Process proc = searchProcess(id);
            
            //=========
            
            if(!foundPage && (!inside)){
                if(memSize > auxDisk[0].length){
                    //printa problema de memória
                    return;
                }
                for(int i = 0; i < auxDisk.length; i++){
                    System.out.println("ESTOU BEM AQUI " + auxDisk[i]);
                    if(auxDisk[i][0].equals("X")){
                        //SWAP
                        int swapPage = lruOrder.removeFirst();
                        lruOrder.add(swapPage);
                        //lembrar que falta ver a ID da matriz, pages e full/occ
                        Process diskProc = searchProcess(RAM[swapPage][0]);
                        for(int j = 0; j < auxDisk[0].length; j++){
                            disk[i][j] = VM[swapPage][j];
                            auxDisk[i][j] = RAM[swapPage][j];
                            diskProc.setDisk(true);
                            //falta contar pra ver se ta lotado
                        }
                        for(int r = 0; r < diskProc.getPages().size(); r++){
                            if(diskProc.getPages().get(i) == swapPage) diskProc.getPages().remove(r);
                        }
                        LinkedList<Integer> newPages = new LinkedList<>();
                        newPages.add(swapPage);
                        diskProc.setPages(newPages);
                        int currentAd = proc.getCurrentAddress();
                        for(int r = 0; r < RAM[0].length; r++){
                            if(memSize != 0){
                                RAM[swapPage][r] = proc.getId();
                                VM[swapPage][r] = currentAd;
                                memSize--;
                                currentAd++;
                            }
                            else{
                                RAM[swapPage][r] = "X";
                                VM[swapPage][r] = -1;
                            }
                        }
                        proc.setCurrentAddress(currentAd);
                        for(int r = 0; r < RAM[0].length; r++){
                            if(VM[swapPage][r] == -1){
                                fullPage[swapPage] = false;
                            }
                        }
                        System.out.println("=======================");
                        System.out.println("Page Fault");
                        System.out.println("=======================");
                        return;
                    }
                }
            }
            
            //=========
            
            for(int i =0; i < foundPages.size();i++){
                if(!proc.getPages().contains(foundPages.get(i))) proc.setPages(foundPages);
            }
            
            int currentAd = proc.getCurrentAddress();
            
            for(int i = 0; i < foundPages.size(); i++){
                int actualPage = foundPages.get(i);
                ocuppiedPage[actualPage] = true;
                int k = 0;
                for(int j = 0; j < RAM[0].length; j++){
                    if(RAM[actualPage][j].equals("X") && VM[actualPage][j] == -1){
                        RAM[actualPage][j] = proc.getId();
                        VM[actualPage][j] = currentAd;
                        currentAd++;
                        memSize--;
                        System.out.println(memSize);
                    }
                    k++;
                    if(k == pageSize) fullPage[actualPage] = true;
                    if(memSize == 0) break;
                }
                
                System.out.println("CU CGADAO " + actualPage);
                if(algoritmo_troca_lru){
                    for(int r = 0; r < lruOrder.size(); r++){
                        System.out.println("AVELIXO " + r);
                        if(lruOrder.get(r) == actualPage){
                            int lruPage = lruOrder.remove(r);
                            lruOrder.add(lruPage);
                            break;
                        }
                    }
                }
                
                if(memSize == 0) break;
            }
            
            proc.setCurrentAddress(currentAd);
            
            for(int i = 0; i < lruOrder.size(); i++){
                System.out.println("ACUTAL ORDER ---> " + lruOrder.get(i));
            }
            
        }
    }
    
    //MÉTODO QUE REALIZA O ACESSO A UM ENDEREÇO EM PÁGINA
    //Lembrar de botar o tempo para LRU
    private void instructionA(String id, int index){
        //System.out.println("=======================");
        if(processNames.contains(id)){
            Process proc = searchProcess(id);
            //System.out.println("=======================");
            if(index >= proc.getCurrentAddress()){
                System.out.println("=======================");
                System.out.println("Page Fault");
                System.out.println("=======================");
                return;
            }
            //lembrar de futuramente ver o disco
            LinkedList<Integer> procPages = proc.getPages();
            for(int i = 0; i < procPages.size(); i++){
                int actualPage = procPages.get(i);
                for(int j = 0; j < RAM[0].length; j++){
                    if(VM[actualPage][j] == index){
                        System.out.println("=======================");
                        System.out.println("Acessado, lembre se do LRU");
                        System.out.println("=======================");
                        if(algoritmo_troca_lru){
                            for(int r = 0; r < lruOrder.size(); r++){
                                System.out.println("AVELIXO " + r);
                                if(lruOrder.get(r) == actualPage){
                                    int lruPage = lruOrder.remove(r);
                                    lruOrder.add(lruPage);
                                    break;
                                }
                            }
                        }
                    }
                }
            }
            for(int i = 0; i < lruOrder.size(); i++){
                System.out.println("ACUTAL ORDER ---> " + lruOrder.get(i));
            }
        }
    }
    
    private Process searchProcess(String id){
        for(int i = 0; i < process.size(); i++){
            if(process.get(i).getId().equals(id)) return process.get(i);
        }
        return null;
    }
    
    //MÉTODO QUE CRIA NOVO PROCESSO NAS MEMÓRIAS
    private void instructionC(String id, int memSize){
        if(!processNames.contains(id)){
            int adSize = physAddress/pageSize;
            int pages = (int) Math.ceil((double)memSize/(double)adSize);
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
                System.out.println("jdsisjd323232323  "  + RAM[0].length);
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
                
                System.out.println("CU CGADAO " + actualPage);
                if(algoritmo_troca_lru){
                    for(int r = 0; r < lruOrder.size(); r++){
                        System.out.println("AVELIXO " + r);
                        if(lruOrder.get(r) == actualPage){
                            int lruPage = lruOrder.remove(r);
                            lruOrder.add(lruPage);
                            break;
                        }
                    }
                }
                
                if(memSize == 0) break;
            }
            newProc.setCurrentAddress(currentAd);
            
            for(int i = 0; i < lruOrder.size(); i++){
                System.out.println("ACUTAL ORDER ---> " + lruOrder.get(i));
            }
            
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
        
        ocuppiedPage = new boolean[pageSize];
        fullPage = new boolean[pageSize];
        for(int i = 0; i < fullPage.length; i++){
            ocuppiedPage[i] = false;
            fullPage[i] = false;
        }
    }
    
    private void setLRU(){
        for(int i = 0; i < pageSize; i++){
            lruOrder.add(i);
        }
        
        for(int i = 0; i < lruOrder.size(); i++){
            System.out.println("----> " + lruOrder.get(i));
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
