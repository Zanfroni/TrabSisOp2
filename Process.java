package t2sisop;

import java.util.LinkedList;

/**
 *
 * @author Gabriel Franzoni 15105090
 * @author Igor Brehm 16180276
 */
public class Process {
    
    private String id;
    private int mem;
    private LinkedList<Integer> pages = new LinkedList<>();
    
    public Process(String id, int mem){
        this.id = id;
        this.mem = mem;
    }
    
    public String getId(){
        return id;
    }
    
    public void setMem(int mem){
        this.mem = mem;
    }
    
    public int getMem(){
        return mem;
    }
    
    /*public void setPages(int[] pages){
        this.pages = pages;
    }
    
    public int[] getPages(){
        return pages;
    }*/
    
}
