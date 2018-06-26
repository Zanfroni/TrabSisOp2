package trabsisiop2;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Random;
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
    private Random rand = new Random();
    
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
        
        
        //LÃŠ A PRIMEIRA LINHA
        input = in.readLine();
        if(input.equals("sequencial") || input.equals("0") || input.equals("s")){
            sequencial = true;
        } else if(input.equals("aleatorio") || input.equals("1") || input.equals("a")){
            sequencial = false;
        } else{
            shutdown();
        }
        
        //LÃŠ A SEGUNDA LINHA
        input = in.readLine();
        if(input.equals("lru")){
            algoritmo_troca_lru = true;
        } else if(input.equals("aleatorio")){
            algoritmo_troca_lru = false;
        } else{
            shutdown();
        }
        
        //LÃŠ A TERCEIRA LINHA
        input = in.readLine();
        pageSize = Integer.parseInt(input);
        if(algoritmo_troca_lru) setLRU();
        
        //LÃŠ A QUARTA LINHA
        input = in.readLine();
        physAddress = Integer.parseInt(input);
        if((Integer.parseInt(input)) % pageSize != 0) shutdown();
        RAM = new String[pageSize][physAddress/pageSize];
        VM = new int[pageSize][physAddress/pageSize];
        
        //LÃŠ A QUINTA LINHA
        input = in.readLine();
        diskAddress = Integer.parseInt(input);
        if((Integer.parseInt(input)) % pageSize != 0) shutdown();
        disk = new int[diskAddress/pageSize][physAddress/pageSize];
        auxDisk = new String[diskAddress/pageSize][physAddress/pageSize];
        System.out.println(disk.length);
        System.out.println(disk[0].length);
        
        populate();
        
        //INICIA-SE A LEITURA DAS INSTRUÃ‡Ã•ES (C,A,M,T)
        adjustInstruction(in, input);
        execute();
        
    }
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    private void execute(){
        String[] inst;
        if(sequencial){
            while(!instructions.isEmpty()){
                inst = instructions.removeFirst();
                switch(inst[0]){
                    case "C": instructionC(inst[1], Integer.parseInt(inst[2]));
                              break;
                    case "A": instructionA(inst[1], Integer.parseInt(inst[2]));
                              break;
                    case "M": instructionM(inst[1], Integer.parseInt(inst[2]));
                              break;
                    case "T": instructionT(inst[1]);
                              break;
                   default: break;
                }
            }
        }else{
            for(int i = 0; i < 2; i++){
                int newProc = rand.nextInt(8);
                int mem = rand.nextInt(21);
                instructionC("p" + Integer.toString(newProc), mem);
            }
            while(true){
                int proc = rand.nextInt(process.size() == 0 ? 1 : process.size());
                int newProc = rand.nextInt(8);
                int mem = rand.nextInt(21);
                int command = rand.nextInt(100);
                if(!process.isEmpty()){
                    if(command >= 98) instructionT(process.get(proc).getId());
                    if(command <=74 && command >= 4) instructionA(process.get(proc).getId(), mem);
                    if(command >=75 && command <= 97) instructionM(process.get(proc).getId(), mem);
                    if(command <= 3) instructionC("p" + Integer.toString(newProc), mem);
                }
                
                int stopTime = 0;
                for(int i = 0; i < fullPage.length; i++){
                    if(ocuppiedPage[i]) stopTime++;
                }
                for(int i = 0; i < fullPage.length; i++){
                    if(fullPage[i]) stopTime++;
                }
                for(int i = 0; i < auxDisk.length; i++){
                    if(!auxDisk[i][0].equals("X")) stopTime++;
                }
                if(stopTime >= ((fullPage.length*2)-2  + auxDisk.length)) break;
                if(process.isEmpty()) break;
            }
        }
    }
    
    private void instructionT(String id){
        Process removed = searchProcess(id);
        for(int i = 0; i < RAM.length; i++){
            if(RAM[i][0].equals(id)){
                for(int j = 0; j < RAM[0].length; j++){
                    RAM[i][j] = "X";
                    VM[i][j] = -1;
                }
                ocuppiedPage[i] = false;
                fullPage[i] = false;
            }
        }
        for(int i = 0; i < auxDisk.length; i++){
            if(auxDisk[i][0].equals(id)){
                for(int j = 0; j < auxDisk[0].length; j++){
                    auxDisk[i][j] = "X";
                    disk[i][j] = -1;
                }
            }
        }
        processNames.remove(removed);
        process.remove(removed);
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
                    System.out.println("=======================");
                    System.out.println("Memória insuficiente");
                    System.out.println("=======================");
                    return;
                }
                for(int i = 0; i < auxDisk.length; i++){
                    if(auxDisk[i][0].equals("X")){
                        //SWAP
                        int swapPage = 0;
                        if(algoritmo_troca_lru){
                            swapPage = lruOrder.removeFirst();
                            lruOrder.add(swapPage);
                        }else{
                            swapPage = rand.nextInt(pageSize);
                        }
                        Process diskProc = searchProcess(RAM[swapPage][0]);
                        for(int j = 0; j < auxDisk[0].length; j++){
                            disk[i][j] = VM[swapPage][j];
                            auxDisk[i][j] = RAM[swapPage][j];
                            if(diskProc != null) diskProc.setDisk(true);
                        }
                        if(diskProc != null){
                            for(int r = 0; r < diskProc.getPages().size(); r++){
                                if(diskProc.getPages().get(r) == swapPage){
                                    diskProc.getPages().remove(r);
                                }
                            }
                        }
                        LinkedList<Integer> newPages = new LinkedList<>();
                        newPages.add(swapPage);
                        proc.setPages(newPages);
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
                        fullPage[swapPage] = true;
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
                        memSize--;                    }
                    k++;
                    if(k == pageSize) fullPage[actualPage] = true;
                    if(memSize == 0) break;
                }
                
                if(algoritmo_troca_lru){
                    for(int r = 0; r < lruOrder.size(); r++){
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
            System.out.println("=======================");
            System.out.println("Novo espaço alocado");
            System.out.println("=======================");
            
        }
    }
    
    //MÃ‰TODO QUE REALIZA O ACESSO A UM ENDEREÃ‡O EM PÃGINA
    //Lembrar de botar o tempo para LRU
    private void instructionA(String id, int index){
        if(processNames.contains(id)){
            Process proc = searchProcess(id);
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
                                if(lruOrder.get(r) == actualPage){
                                    int lruPage = lruOrder.remove(r);
                                    lruOrder.add(lruPage);
                                    return;
                                }
                            }
                        }
                    }
                }
            }
            if(proc.getDisk()){
                for(int i = 0; i < auxDisk.length; i++){
                    if(auxDisk[i][0].equals(proc.getId())){
                        for(int j = 0; j < auxDisk[0].length; j++){
                            if(disk[i][j] == index){
                                int swapPage = 0;
                                if(algoritmo_troca_lru){
                                    swapPage = lruOrder.removeFirst();
                                    lruOrder.add(swapPage);
                                }else{
                                    swapPage = rand.nextInt(pageSize);
                                }
                                Process diskProc = searchProcess(RAM[swapPage][0]);
                                String[] auxNew = new String[auxDisk[0].length];
                                int[] auxNew2 = new int[auxDisk[0].length];
                                for(int k = 0; k < auxDisk[0].length; k++){
                                    auxNew[k] = RAM[swapPage][k];
                                    auxNew2[k] = VM[swapPage][k];
                                }
                                for(int k = 0; k < auxDisk[0].length; k++){
                                    RAM[swapPage][k] = auxDisk[i][k];
                                    VM[swapPage][k] = disk[i][k];
                                }
                                for(int k = 0; k < auxDisk[0].length; k++){
                                    auxDisk[i][k] = auxNew[k];
                                    disk[i][k] = auxNew2[k];
                                }
                                fullPage[swapPage] = true;
                                for(int r = 0; r < RAM[0].length; r++){
                                    if(VM[swapPage][r] == -1){
                                        fullPage[swapPage] = false;
                                    }
                                }
                                LinkedList<Integer> newPage = new LinkedList<>();
                                if(diskProc != null) diskProc.setDisk(true);
                                if(diskProc != null){
                                    for(int r = 0; r < diskProc.getPages().size(); r++){
                                        if(diskProc.getPages().get(r) == swapPage){
                                            diskProc.getPages().remove(r);
                                        }
                                    }
                                }
                                newPage.add(swapPage);
                                proc.setPages(newPage);
                                proc.setDisk(false);
                                for(int r = 0; r < auxDisk.length; r++){
                                    if(auxDisk[r][0].equals(proc.getId())){
                                        proc.setDisk(true);
                                    }
                                }
                            }
                        }
                    }
                }
                System.out.println("=======================");
                System.out.println("Page Fault");
                System.out.println("=======================");
            }
        }
    }
    
    private Process searchProcess(String id){
        for(int i = 0; i < process.size(); i++){
            if(process.get(i).getId().equals(id)) return process.get(i);
        }
        return null;
    }
    
    //MÃ‰TODO QUE CRIA NOVO PROCESSO NAS MEMÃ“RIAS
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
                System.out.println("=======================");
                System.out.println("Memória insuficiente");
                System.out.println("=======================");
                return;
            }
            
            Process newProc = new Process(id, memSize);
            process.add(newProc);
            processNames.add(id);
            
            newProc.setPages(foundPages);
            int currentAd = newProc.getCurrentAddress();
            
            for(int i = 0; i < foundPages.size(); i++){
                int actualPage = foundPages.get(i);
                ocuppiedPage[actualPage] = true;
                int k = 0;
                for(int j = 0; j < RAM[0].length; j++){
                    RAM[actualPage][j] = newProc.getId();
                    VM[actualPage][j] = currentAd;
                    currentAd++;
                    k++;
                    memSize--;
                    if(k == pageSize-1) fullPage[actualPage] = true;
                    if(memSize == 0) break;
                }
                
                if(algoritmo_troca_lru){
                    for(int r = 0; r < lruOrder.size(); r++){
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
            
            
            
        }
    }
    
    //MÃ‰TODO QUE APLICA A SEPARAÃ‡ÃƒO DAS INSTRUÃ‡Ã•ES
    private void adjustInstruction(BufferedReader in, String input) throws IOException{
        String[] inst;
        if(sequencial){
            while((input = in.readLine()) != null){
                inst = input.split(" ");
                instructions.add(inst);
            }
        }else{
            LinkedList<String[]> newinst = new LinkedList<>();
            while((input = in.readLine()) != null){
                inst = input.split(" ");
                if(inst[0].equals("C")) instructionC(inst[1], Integer.parseInt(inst[2]));
            }
        }
    }
    
    //MÃ‰TODO QUE POPULA AS MATRIZES E VETORES DE INTERESSE
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
    
    //MÃ‰TODO QUE FINALIZA O PROGRAMA EM CASO DE ERROS NO ARQUIVO DE ENTRADA
    private void shutdown(){
        System.out.print("O programa foi fechado inesperadamente (condiÃ§Ãµes de entrada nÃ£o atingidas)");
        System.exit(0);
    }
}
