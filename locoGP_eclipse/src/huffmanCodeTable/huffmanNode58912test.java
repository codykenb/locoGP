package huffmanCodeTable;
public class huffmanNode58912test implements Comparable {
  Byte uniqueChar;
  int freq=0;
  huffmanNode58912test left, right;
  public int getFreq(){
    return freq;
  }
  huffmanNode58912test(  byte aChar,  int freq){
    uniqueChar=aChar;
    this.freq=freq;
  }
  huffmanNode58912test(  int freq,  huffmanNode58912test left,  huffmanNode58912test right){
    this.freq=freq;
    this.right=right;
    this.left=left;
  }
  @Override public int compareTo(  Object hN){
    return this.freq - ((huffmanNode58912test)hN).freq;
  }
}
