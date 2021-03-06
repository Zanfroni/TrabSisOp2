package trabsisiop2;

import java.util.LinkedList;

/**
 *
 * @author Gabriel Franzoni 15105090
 * @author Igor Brehm 16180276
 */
public class Process {
    
    private String id;
    private int mem;
    private int currentAddress = 0;
    private LinkedList<Integer> pages = new LinkedList<>();
    private boolean inDisk = false;
    
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
    
    public void setCurrentAddress(int ad){
        this.currentAddress = ad;
    }
    
    public int getCurrentAddress(){
        return currentAddress;
    }
    
    public void setPages(LinkedList<Integer> pages){
        for(int i = 0; i < pages.size(); i++){
            if(!this.pages.contains(pages.get(i))) this.pages.add(pages.get(i));
        }
    }
    
    public LinkedList<Integer> getPages(){
        return pages;
    }
    
    public void setDisk(boolean disk){
        inDisk = disk;
    }
    
    public boolean getDisk(){
        return inDisk;
    }
    
}