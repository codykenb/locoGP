package huffmanCodeTable;
public class huffmanNode58856 implements Comparable {
  Byte uniqueChar=null;
  int freq=0;
  huffmanNode58856 left, right;
  public int getFreq(){
    return freq;
  }
  huffmanNode58856(  byte aChar,  int freq){
    uniqueChar=aChar;
    this.freq=freq;
  }
  huffmanNode58856(  int freq,  huffmanNode58856 left,  huffmanNode58856 right){
    this.freq=freq;
    this.right=right;
    this.left=left;
  }
  @Override public int compareTo(  Object hN){
    return this.freq - ((huffmanNode58856)hN).freq;
  }
}
