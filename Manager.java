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
    
    //ATRIBUTOS
    Scanner in = new Scanner(System.in);
    private boolean sequencial = true;
    private boolean algoritmo_troca_lru = true;
    private int diskAddress, physAddress, pageSize;
    private LinkedList<String> processNames = new LinkedList<>();
    private LinkedList<Process> process = new LinkedList<>();
    private LinkedList<String[]> instructions = new LinkedList<>();
    private LinkedList<Integer> lruOrder = new LinkedList<>();
    private Random rand = new Random();
    
    //MEMORIAS RAM, VIRTUAL, DISCO E VETORES QUE MOSTRAM O QUAO OCUPADAS
    //AS MEMORIAS ESTAO
    private String[][] RAM;
    private int[][] VM;
    private int[][] disk;
    private String[][] auxDisk;
    private boolean[] ocuppiedPage;
    private boolean[] fullPage;
    
    //CONSTRUTOR
    public Manager() throws FileNotFoundException, IOException{
        System.out.println("Digite o endereco do arquivo de entrada:");
	String input = "";
        BufferedReader in = new BufferedReader(new FileReader("entrada" + ".txt"));
        
        
        //LE A PRIMEIRA LINHA
        input = in.readLine();
        if(input.equals("sequencial") || input.equals("0") || input.equals("s")){
            sequencial = true;
        } else if(input.equals("aleatorio") || input.equals("1") || input.equals("a")){
            sequencial = false;
        } else{
            shutdown();
        }
        
        //LE A SEGUNDA LINHA
        input = in.readLine();
        if(input.equals("lru")){
            algoritmo_troca_lru = true;
        } else if(input.equals("aleatorio")){
            algoritmo_troca_lru = false;
        } else{
            shutdown();
        }
        
        //LE A TERCEIRA LINHA
        input = in.readLine();
        pageSize = Integer.parseInt(input);
        if(algoritmo_troca_lru) setLRU();
        
        //LE A QUARTA LINHA
        input = in.readLine();
        physAddress = Integer.parseInt(input);
        if((Integer.parseInt(input)) % pageSize != 0) shutdown();
        RAM = new String[pageSize][physAddress/pageSize];
        VM = new int[pageSize][physAddress/pageSize];
        
        //LE A QUINTA LINHA
        input = in.readLine();
        diskAddress = Integer.parseInt(input);
        if((Integer.parseInt(input)) % pageSize != 0) shutdown();
        disk = new int[diskAddress/pageSize][physAddress/pageSize];
        auxDisk = new String[diskAddress/pageSize][physAddress/pageSize];
        System.out.println(disk.length);
        System.out.println(disk[0].length);
        
        populate();
        
        //INICIA-SE A LEITURA DAS INSTRUCOES (C,A,M,T)
        adjustInstruction(in, input);
        execute();
        
    }
    
    //INICIA O PROCESSAMENTO DE CADA INSTRUCAO. DIFERENCIA SE E SEQUENCIAL OU
    //ALEATORIO
    private void execute(){
        String[] inst;
        if(sequencial){
            while(!instructions.isEmpty()){
                System.out.println("Pressione um botão para continuar");
                in.nextLine();
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
                int proc = rand.nextInt(process.isEmpty() ? 1 : process.size());
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
    
    //INSTRUCAO DE TERMINO DE PROCESSO
    //ESTA INSTRUCAO VAI REMOVER TODAS AS INSTANCIAS DO PROCESSO DA
    //MEMORIA E DISCO, LIBERANDO ESPACO
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
        LinkedList<Integer> removedPlaces = new LinkedList<>();
        removedPlaces = removed.getPages();
        processNames.remove(removed);
        process.remove(removed);
        print();
        System.out.print("Processo " + id + " terminado em ");
        for(int i = 0; i < removedPlaces.size(); i++){
            System.out.print("PAGINA " + removedPlaces.get(i) + " | ");
        }
        for(int i = 0; i < auxDisk.length; i++){
            if(auxDisk[i][0].equals("X")){
                System.out.print("PAGINA DE DISCO " + i + " | ");
            }
        }
        System.out.println("\n");
    }
    
    //INSTRUCAO DE ALOCACAO
    //ESTA INSTRUCAO VAI ALOCAR ESPACO PARA O TAL PROCESSO
    //ELA INFELIZMENTE TEM UMA LIMITACAO. ELA NAO ALOCA EM DISCO SE O TAMANHO
    //DO QUE PRECISA E MAIOR QUE DE UMA LINHA TODA DO DISCO
    private void instructionM(String id, int memSize){
        
        //ESTE INICIO, ELE CALCULA QUANTAS PAGINAS ELE PRECISA PARA ALOCAR
        //E SE A MEMORIA POSSUI ESTA QUANTIDADE LIVRE
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
                        //SE ELE PUDER ALOCAR DENTRO DE UMA PAGINA JA OCUPADA
                        //CAI NESTA CONDICAO
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
            
            //SE ELE NAO ENCONTRAR PAGINAS SUFICIENTES NA MEMORIA,
            //ELE VAI FINALMENTE PROCURAR NO DISCO SE TEM ESPACO LA.
            //SE TIVER, ELE REALIZA UM SWAP.
            Process proc = searchProcess(id);
            if(!foundPage && (!inside)){
                if(memSize > auxDisk[0].length){
                    print();
                    System.out.println("Memória insuficiente para alocação de " + memSize + " enderecos");
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
                        print();
                        System.out.println("PAGE FAULT");
                        System.out.println("Pagina " + swapPage + " alocada para disco");
                        System.out.print("Memoria alocada em PAGINA " + swapPage);
                        System.out.println();
                        return;
                    }
                }
            }
            
            //FINALMENTE, SE ELE ENCONTRAR ESPACO NA MEMORIA, VAI OCUPAR
            //OS ENDERECOS DESIGNADOS NELA
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
            
            print();
            System.out.print("Processo " + id + " alocado em ");
            for(int i = 0; i < foundPages.size(); i++){
                System.out.print("PAGINA " + foundPages.get(i) + " | ");
            }
            System.out.println("\n");
        }
    }
    
    //METODO QUE EXECUTA INSTRUCAO DE ACESSO
    
    //ELE SIMPLESMENTE PROCURA NA MEMORIA SE O EQUIVALENTE AO INDEX DA INSTRUCAO
    //EQUIVALE AO INDEX DO PROCESSO MANDADO. SE FOR, ELE ACESSA. CASO NAO
    //ENCONTRE NA MEMORIA, ELE TENTA PROCURAR NO DISCO. SE ENCONTRAR LA,
    //REALIZA UM SWAP
    private void instructionA(String id, int index){
        if(processNames.contains(id)){
            Process proc = searchProcess(id);
            if(index >= proc.getCurrentAddress()){
                print();
                System.out.println("PAGE FAULT para acesso em " + "| " + id + " " + index + " |");
                return;
            }
            //lembrar de futuramente ver o disco
            LinkedList<Integer> procPages = proc.getPages();
            for(int i = 0; i < procPages.size(); i++){
                int actualPage = procPages.get(i);
                for(int j = 0; j < RAM[0].length; j++){
                    if(VM[actualPage][j] == index){
                        print();
                        System.out.println("Memoria acesso em FRAME " + index + " em PAGINA " + actualPage);
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
            int swapPage = -1;
            if(proc.getDisk()){
                for(int i = 0; i < auxDisk.length; i++){
                    if(auxDisk[i][0].equals(proc.getId())){
                        for(int j = 0; j < auxDisk[0].length; j++){
                            if(disk[i][j] == index){
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
                                for(int r = 0; r < auxDisk.length ;r++){
                                    if(auxDisk[r][0].equals(proc.getId())){
                                        proc.setDisk(true);
                                    }
                                }
                            }
                        }
                    }
                }
                print();
                System.out.println("PAGE FAULT");
                System.out.println("Pagina " + swapPage + " alocada para disco");
                System.out.print("Memoria acesso em FRAME " + index + " em PAGINA " + swapPage);
                System.out.println();
            }
        }
    }
    
    //METODO AUXILIAR QUE PROCURA PROCESSO POR STRING
    private Process searchProcess(String id){
        for(int i = 0; i < process.size(); i++){
            if(process.get(i).getId().equals(id)) return process.get(i);
        }
        return null;
    }
    
    //METODO QUE EXECUTA INSTRUCAO DE CRIACAO DE PROCESSO. BASTANTE SIMPLES.
    //SE NAO TER ESPACO, MANDA MENSAGEM SEM MEMORIA.
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
                    print();
                    System.out.println("Memória insuficiente para alocação de " + memSize + " enderecos");
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
            print();
            System.out.print("Processo " + id + " criado em ");
            for(int i = 0; i < foundPages.size(); i++){
                System.out.print("PAGINA " + foundPages.get(i) + " | ");
            }
            System.out.println("\n");
        }
    }
    
    //ESTE METODO AJUSTA O ARQUIVO TXT PARA SER PREPARADO PARA SER PROCESSADO
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
    
    //METODO QUE POPULA AS MATRIZES E VETORES DE INTERESSE
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
    
    //METODO QUE PREPARA O ALGORITMO LRU. NOTA QUE AQUI TEMPO NAO E CALCULADO.
    //SIMPLESMENTE E REALIZADO UMA FILA E CADA VEZ QUE A PAGINA TEM ACESSO/ALOCACAO,
    //ESTA MESMA VAI PARA A O FINAL DA FILA. BASTANTE INTUITIVO.
    private void setLRU(){
        for(int i = 0; i < pageSize; i++){
            lruOrder.add(i);
        }
    }
    
    //METODO QUE PRINTA MEMORIA RAM, VIRTUAL E DISCO
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
        
        for(int i = 0; i < VM.length; i++){
            for(int j = 0; j < VM[0].length; j++){
                System.out.print(VM[i][j] + "\t");
            }
            System.out.println();
        }
        
        System.out.println();
        System.out.println();
        System.out.println();
        
        for(int i = 0; i < disk.length; i++){
            for(int j = 0; j < disk[0].length; j++){
                System.out.print(disk[i][j] + "\t");
            }
            System.out.println();
        }
        System.out.println("\n");
    }
    
    //METODO QUE FINALIZA O PROGRAMA EM CASO DE ERROS NO ARQUIVO DE ENTRADA
    private void shutdown(){
        System.out.print("O programa foi fechado inesperadamente (condicoes de entrada nao atingidas)");
        System.exit(0);
    }
}
