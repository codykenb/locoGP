package huffmanCodeTable;
public class huffmanNode5223test implements Comparable {
  Byte uniqueChar=null;
  int freq=0;
  huffmanNode5223test left, right;
  public int getFreq(){
    return freq;
  }
  huffmanNode5223test(  byte aChar,  int freq){
    uniqueChar=aChar;
    this.freq=freq;
  }
  huffmanNode5223test(  int freq,  huffmanNode5223test left,  huffmanNode5223test right){
    this.freq=freq;
    this.right=right;
    this.left=left;
  }
  @Override public int compareTo(  Object hN){
    return this.freq - ((huffmanNode5223test)hN).freq;
  }
}
