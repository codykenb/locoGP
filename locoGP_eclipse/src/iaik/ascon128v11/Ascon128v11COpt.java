package iaik.ascon128v11;

import java.nio.ByteBuffer;
import java.util.Arrays;
/*#include <stdio.h>
#include "api.h"
#include "crypto_aead.h"
*/

public class Ascon128v11COpt {

    /* Referenced in optimised C version, but not defined. 
       This value added from ref Java version. */
    public static int CRYPTO_KEYBYTES = 16;
    
    //#define LITTLE_ENDIAN
    public static boolean LITTLE_ENDIAN = true;
    //#define BIG_ENDIAN
    public static boolean BIG_ENDIAN = false;
    
    //typedef unsigned char u8;
    public static char u8;
    //typedef unsigned long long u64;
    public static long u64;
    //typedef long long i64;
    public static long i64;
    
    
    // #define RATE (64 / 8)
    public static int RATE = (64 / 8);
    //#define PA_ROUNDS 12
    public static int PA_ROUNDS = 12; 
    //#define PB_ROUNDS 6
    public static int PB_ROUNDS = 6; 

    //#define ROTR(x,n) (((x)>>(n))|((x)<<(64-(n))))
    public static long ROTR(long x, int n) {
        return Long.rotateRight(x, n);
    }
    
    /*#ifdef BIG_ENDIAN
      #define EXT_BYTE(x,n) ((u8)((u64)(x)>>(8*(n))))
      #define INS_BYTE(x,n) ((u64)(x)<<(8*(n)))
      #define U64BIG(x) (x)
      #endif */
    
    // #ifdef LITTLE_ENDIAN
    //#define EXT_BYTE(x,n) ((u8)((u64)(x)>>(8*(7-(n)))))
    public static byte EXT_BYTE(long x, int n){
    	/*long test = (x >> 8*(7-n)) & 0xff;
    	System.out.println(" -x: " +Long.toHexString(x));*/
    	return (byte) (x >> 8*(7-n));
    }
    
    // #define INS_BYTE(x,n) ((u64)(x)<<(8*(7-(n))))
    public static long INS_BYTE(long x, int n){
    	// return ((long)((byte)x)<<(8*(7-(n))));
    	return ((x)<<(8*(7-(n))));
    }
    //#define U64BIG(x) 
    public static long U64BIG(long x){
    	//((ROTR(x, 8) & (0xFF000000FF000000ULL)) |
    	return ((ROTR(x, 8) & (0xFF000000FF000000L)) |
		(ROTR(x,24) & (0x00FF000000FF0000L)) | 
		(ROTR(x,40) & (0x0000FF000000FF00L)) | 
		(ROTR(x,56) & (0x000000FF000000FFL)));
    }
    // #endif 
    
    // copied in from Java ref
    public static long load64(byte S[]) {
        long x = 0;
        x = ByteBuffer.wrap(S).getLong();
        return x;
      }  
    
    // copied in from Java ref
    public static void store64(byte S[], int offset, long x) {
        int i;
        // byte byteacter[] = ByteBuffer.allocate(8).putLong(x).array();
        for (i = 0; i < 8 && i+offset < S.length; ++i) {
          // S[i + offset] = (byte) byteacter[i]; // pos
        	S[i + offset] = EXT_BYTE(x, i);
        }
      }
    
    //static const int R[5][2] = { {19, 28}, {39, 61}, {1, 6}, {10, 17}, {7, 41} };
    static int[][] R = { {19, 28}, {39, 61}, {1, 6}, {10, 17}, {7, 41} };
    
    // #define ROUND(C) {
/*    public static void ROUND_MACRO(long C){	    	
        x2 ^= C;     
        x0 ^= x4;
        x4 ^= x3;
        x2 ^= x1;
        t0 = x0;
        t4 = x4;
        t3 = x3;
        t1 = x1;
        t2 = x2;
        x0 = t0 ^ ((~t1) & t2);
        x2 = t2 ^ ((~t3) & t4);
        x4 = t4 ^ ((~t0) & t1);
        x1 = t1 ^ ((~t2) & t3);
        x3 = t3 ^ ((~t4) & t0);
        x1 ^= x0;
        t1  = x1;
        x1 = ROTR(x1, R[1][0]);
        x3 ^= x2;
        t2  = x2;
        x2 = ROTR(x2, R[2][0]);
        t4  = x4;
        t2 ^= x2;
        x2 = ROTR(x2, R[2][1] - R[2][0]);
        t3  = x3;
        t1 ^= x1;
        x3 = ROTR(x3, R[3][0]);
        x0 ^= x4;
        x4 = ROTR(x4, R[4][0]);
        t3 ^= x3;
        x2 ^= t2;
        x1 = ROTR(x1, R[1][1] - R[1][0]);
        t0  = x0;
        x2 = ~x2;
        x3 = ROTR(x3, R[3][1] - R[3][0]);
        t4 ^= x4;
        x4 = ROTR(x4, R[4][1] - R[4][0]);
        x3 ^= t3;
        x1 ^= t1;
        x0 = ROTR(x0, R[0][0]);
        x4 ^= t4;
        t0 ^= x0;
        x0 = ROTR(x0, R[0][1] - R[0][0]);
        x0 ^= t0;
    }*/

    // #define P12 
    /*public static void P12_MACRO(){
    	ROUND_MACRO(0xf0);
    	ROUND_MACRO(0xe1);
    	ROUND_MACRO(0xd2);
    	ROUND_MACRO(0xc3);
    	ROUND_MACRO(0xb4);
    	ROUND_MACRO(0xa5);
    	ROUND_MACRO(0x96);
    	ROUND_MACRO(0x87);
    	ROUND_MACRO(0x78);
    	ROUND_MACRO(0x69);
    	ROUND_MACRO(0x5a);
    	ROUND_MACRO(0x4b);
    }*/

    // #define P6 {
    /*public static void P6_MACRO(){
	ROUND_MACRO(0xf0);
	ROUND_MACRO(0xe1);
	ROUND_MACRO(0xd2);
	ROUND_MACRO(0xc3);
	ROUND_MACRO(0xb4);
	ROUND_MACRO(0xa5);
    }*/
    
    /*int crypto_aead_encrypt(
      unsigned char *c, unsigned long long *clen,
      const unsigned char *m, unsigned long long mlen,
      const unsigned char *ad, unsigned long long adlen,
      const unsigned char *nsec,
      const unsigned char *npub,
      const unsigned char *k) {*/
    public static int crypto_aead_encrypt(byte c[], int clen, byte m[], int mlen, byte ad[], int adlen,
					  byte nsec[], byte npub[], byte k[]) {
	
	// u64 K0 = U64BIG(((u64*)k)[0]);
	long K0 = U64BIG((k)[0]); // BUG! - take the first 8 bytes as long, (instead of the first single byte of k as long)
	//System.out.println("K0:" +Long.toHexString(K0));
	// u64 K1 = U64BIG(((u64*)k)[1]);
	long K1 = U64BIG((k)[1]);
	// u64 N0 = U64BIG(((u64*)npub)[0]);
	long N0 = U64BIG((npub)[0]);
	// u64 N1 = U64BIG(((u64*)npub)[1]);
	long N1 = U64BIG((npub)[1]);
	// u64 x0, x1, x2, x3, x4;
	long x0, x1, x2, x3, x4;
	// u64 t0, t1, t2, t3, t4;
	long t0 = 0, t1 = 0, t2 = 0, t3 = 0, t4 = 0;
	// u64 rlen;
	int rlen;
	int i;
	
	// initialization
	// x0 = (u64)((CRYPTO_KEYBYTES * 8) << 24 | (RATE * 8) << 16 | PA_ROUNDS << 8 | PB_ROUNDS << 0) << 32;
	// KEYBYTES not defined in original optimised C version 
	x0 = ((long)((CRYPTO_KEYBYTES * 8) << 24 | (RATE * 8) << 16 | PA_ROUNDS << 8 | PB_ROUNDS << 0)) << 32;
	x1 = K0;
	x2 = K1;
	x3 = N0;
	x4 = N1;
	System.out.println("-------------------------------------Encrypt P12");
	System.out.println(" x0:"+ Long.toHexString(x0));
	System.out.println(" x4:"+ Long.toHexString(x4));

	// P12
	long[] constantsP12 = {0xf0, 0xe1, 0xd2, 0xc3, 0xb4, 0xa5, 0x96, 0x87, 0x78, 0x69, 0x5a, 0x4b};
	for ( long C :  constantsP12){
	    x2 ^= C; 
	    //System.out.println("x0:" +Long.toHexString(x0));
	    //System.out.println("x4:" +Long.toHexString(x4));
	    x0 ^= x4; //System.out.println("x0:" +Long.toHexString(x0));
	    x4 ^= x3; x2 ^= x1; t0 = x0; t4 = x4; t3 = x3; t1 = x1; t2 = x2;
	    x0 = t0 ^ ((~t1) & t2); //System.out.println("x0:" +Long.toHexString(x0));
	    x2 = t2 ^ ((~t3) & t4); x4 = t4 ^ ((~t0) & t1);
	    x1 = t1 ^ ((~t2) & t3); x3 = t3 ^ ((~t4) & t0); 
	    x1 ^= x0; t1  = x1; x1 = ROTR(x1, R[1][0]);
	    x3 ^= x2; t2  = x2; x2 = ROTR(x2, R[2][0]);
	    t4  = x4; t2 ^= x2; x2 = ROTR(x2, R[2][1] - R[2][0]);
	    t3  = x3; t1 ^= x1; x3 = ROTR(x3, R[3][0]);
	    x0 ^= x4; //System.out.println("x0:" +Long.toHexString(x0)); 
	    x4 = ROTR(x4, R[4][0]);
	    t3 ^= x3; x2 ^= t2; x1 = ROTR(x1, R[1][1] - R[1][0]);
	    t0  = x0; x2 = ~x2; x3 = ROTR(x3, R[3][1] - R[3][0]);
	    t4 ^= x4; x4 = ROTR(x4, R[4][1] - R[4][0]);
	    x3 ^= t3; x1 ^= t1; 
	    x0 = ROTR(x0, R[0][0]); //System.out.println("x0:" +Long.toHexString(x0));
	    x4 ^= t4; t0 ^= x0; 
	    x0 = ROTR(x0, R[0][1] - R[0][0]); //System.out.println("x0:" +Long.toHexString(x0));
	    x0 ^= t0;
	    //System.out.println("x0:" +Long.toHexString(x0));
	    
	} // P12_MACRO();
	System.out.println("After P12 1 x0:" +Long.toHexString(x0) +" x1:" +Long.toHexString(x1)+" x2:" +Long.toHexString(x2)+" x3:" +Long.toHexString(x3)+" x4:" +Long.toHexString(x4) +	" t0:" +Long.toHexString(t0) +" t1:" +Long.toHexString(t1)+" t2:" +Long.toHexString(t2)+" t3:" +Long.toHexString(t3)+" t4:" +Long.toHexString(t4) );
	x3 ^= K0;
	x4 ^= K1;
	
	long[] constantsP6 = {0xf0, 0xe1, 0xd2, 0xc3, 0xb4, 0xa5};
	// process associated data
	//if (adlen) {
	if (adlen>0) {
	    rlen = adlen;
	    //while (rlen >= RATE) {
	    System.out.print("Just before PAD P6 1 ");
	    for (i=0; i < ad.length && rlen>=RATE ; i+=RATE, rlen-=RATE) {
	    	// x0 ^= U64BIG(ad); // packing 8 bytes into long?
	    //x0 ^= U64BIG(load64(Arrays.copyOfRange(ad, i, (i)+RATE))); 
	    x0 ^= load64(Arrays.copyOfRange(ad, i, (i)+RATE));
	    System.out.print(" x0: "+ Long.toHexString(x0));
		// P6
		for ( long C :  constantsP6){
		    x2 ^= C; x0 ^= x4; x4 ^= x3; x2 ^= x1; t0 = x0; t4 = x4; t3 = x3; t1 = x1; t2 = x2;
		    x0 = t0 ^ ((~t1) & t2); x2 = t2 ^ ((~t3) & t4); x4 = t4 ^ ((~t0) & t1);
		    x1 = t1 ^ ((~t2) & t3); x3 = t3 ^ ((~t4) & t0); 
		    x1 ^= x0; t1  = x1; x1 = ROTR(x1, R[1][0]);
		    x3 ^= x2; t2  = x2; x2 = ROTR(x2, R[2][0]);
		    t4  = x4; t2 ^= x2; x2 = ROTR(x2, R[2][1] - R[2][0]);
		    t3  = x3; t1 ^= x1; x3 = ROTR(x3, R[3][0]);
		    x0 ^= x4; x4 = ROTR(x4, R[4][0]);
		    t3 ^= x3; x2 ^= t2; x1 = ROTR(x1, R[1][1] - R[1][0]);
		    t0  = x0; x2 = ~x2; x3 = ROTR(x3, R[3][1] - R[3][0]);
		    t4 ^= x4; x4 = ROTR(x4, R[4][1] - R[4][0]);
		    x3 ^= t3; x1 ^= t1; x0 = ROTR(x0, R[0][0]);
		    x4 ^= t4; t0 ^= x0; x0 = ROTR(x0, R[0][1] - R[0][0]);
		    x0 ^= t0;
			
		} //P6_MACRO();
		System.out.println("\nPAD P6 1  x0:" +Long.toHexString(x0) +" x1:" +Long.toHexString(x1)+" x2:" +Long.toHexString(x2)+" x3:" +Long.toHexString(x3)+" x4:" +Long.toHexString(x4) +	" t0:" +Long.toHexString(t0) +" t1:" +Long.toHexString(t1)+" t2:" +Long.toHexString(t2)+" t3:" +Long.toHexString(t3)+" t4:" +Long.toHexString(t4) );

		// rlen -= RATE;
		// adCount += RATE;
	    }
	    //System.out.println("After PAD P6 1 x0:" +Long.toHexString(x0) +" x1:" +Long.toHexString(x1)+" x2:" +Long.toHexString(x2)+" x3:" +Long.toHexString(x3)+" x4:" +Long.toHexString(x4) +	"t0:" +Long.toHexString(t0) +" t1:" +Long.toHexString(t1)+" t2:" +Long.toHexString(t2)+" t3:" +Long.toHexString(t3)+" t4:" +Long.toHexString(t4) );
	    // for (i = 0; i < rlen; ++i, ++ad)
	    for (i = 0; i < rlen; i++){ // moving along byte by byte
	    	//x0 ^= INS_BYTE(*ad, i);
	    	x0 ^= INS_BYTE((byte)load64(Arrays.copyOfRange(ad, i+1, i+1+RATE)), i); // not really understanding why this (+1) works?
	    	//System.out.println("    INS_BYTE(load64(Arrays.copyOfRange(ad, i,:" +Long.toHexString(INS_BYTE(load64(Arrays.copyOfRange(ad, i, i+RATE)), i)) );
	    	//x0 ^= INS_BYTE((long)ad[i], i);
	    	//System.out.println("    INS_BYTE((long)ad[i]:" +Long.toHexString(INS_BYTE((long)ad[i], i)) );
			System.out.println("    x0:" +Long.toHexString(x0) );
	    }

	    System.out.println("Before PAD p6 2 (before ins_byte) x0:" +Long.toHexString(x0) );	 
	    x0 ^= INS_BYTE(0x80, rlen);
	    System.out.println("After PAD p6 2 (after ins_byte)  x0:" +Long.toHexString(x0) );
	    // P6;
	    for ( long C :  constantsP6){
		x2 ^= C; x0 ^= x4; x4 ^= x3; x2 ^= x1; t0 = x0; t4 = x4; t3 = x3; t1 = x1; t2 = x2;
		x0 = t0 ^ ((~t1) & t2); x2 = t2 ^ ((~t3) & t4); x4 = t4 ^ ((~t0) & t1);
		x1 = t1 ^ ((~t2) & t3); x3 = t3 ^ ((~t4) & t0); 
		x1 ^= x0; t1  = x1; x1 = ROTR(x1, R[1][0]);
		x3 ^= x2; t2  = x2; x2 = ROTR(x2, R[2][0]);
		t4  = x4; t2 ^= x2; x2 = ROTR(x2, R[2][1] - R[2][0]);
		t3  = x3; t1 ^= x1; x3 = ROTR(x3, R[3][0]);
		x0 ^= x4; x4 = ROTR(x4, R[4][0]);
		t3 ^= x3; x2 ^= t2; x1 = ROTR(x1, R[1][1] - R[1][0]);
		t0  = x0; x2 = ~x2; x3 = ROTR(x3, R[3][1] - R[3][0]);
		t4 ^= x4; x4 = ROTR(x4, R[4][1] - R[4][0]);
		x3 ^= t3; x1 ^= t1; x0 = ROTR(x0, R[0][0]);
		x4 ^= t4; t0 ^= x0; x0 = ROTR(x0, R[0][1] - R[0][0]);
		x0 ^= t0;
		//System.out.println("PAD P6 2 x0:" +Long.toHexString(x0) +" x1:" +Long.toHexString(x1)+" x2:" +Long.toHexString(x2)+" x3:" +Long.toHexString(x3)+" x4:" +Long.toHexString(x4) + 	    		"t0:" +Long.toHexString(t0) +" t1:" +Long.toHexString(t1)+" t2:" +Long.toHexString(t2)+" t3:" +Long.toHexString(t3)+" t4:" +Long.toHexString(t4) );
	    } //P6_MACRO();
	}
	x4 ^= 1;
	System.out.println("After PAD Before Plaintext  x0:" +Long.toHexString(x0) +" x1:" +Long.toHexString(x1)+" x2:" +Long.toHexString(x2)+" x3:" +Long.toHexString(x3)+" x4:" +Long.toHexString(x4) +
    		" t0:" +Long.toHexString(t0) +" t1:" +Long.toHexString(t1)+" t2:" +Long.toHexString(t2)+" t3:" +Long.toHexString(t3)+" t4:" +Long.toHexString(t4) );
	
	// process plaintext
	rlen = mlen;
	// while (rlen >= RATE) {
	for (i=0; i < m.length && rlen >= RATE; i+=RATE,rlen -= RATE) { // move along 8 bytes at a time
	    //x0 ^= U64BIG(*(u64*)m);
	    x0 ^= load64(Arrays.copyOfRange(m, i, (i)+RATE)); // bck
	    //*(u64*)c = U64BIG(x0);
	    // c = U64BIG(x0); // why? needed? oh yes it is..
	    store64(c,0,x0);
	    // P6;
	    for ( long C :  constantsP6){
		x2 ^= C; x0 ^= x4; x4 ^= x3; x2 ^= x1; t0 = x0; t4 = x4; t3 = x3; t1 = x1; t2 = x2;
		x0 = t0 ^ ((~t1) & t2); x2 = t2 ^ ((~t3) & t4); x4 = t4 ^ ((~t0) & t1);
		x1 = t1 ^ ((~t2) & t3); x3 = t3 ^ ((~t4) & t0); 
		x1 ^= x0; t1  = x1; x1 = ROTR(x1, R[1][0]);
		x3 ^= x2; t2  = x2; x2 = ROTR(x2, R[2][0]);
		t4  = x4; t2 ^= x2; x2 = ROTR(x2, R[2][1] - R[2][0]);
		t3  = x3; t1 ^= x1; x3 = ROTR(x3, R[3][0]);
		x0 ^= x4; x4 = ROTR(x4, R[4][0]);
		t3 ^= x3; x2 ^= t2; x1 = ROTR(x1, R[1][1] - R[1][0]);
		t0  = x0; x2 = ~x2; x3 = ROTR(x3, R[3][1] - R[3][0]);
		t4 ^= x4; x4 = ROTR(x4, R[4][1] - R[4][0]);
		x3 ^= t3; x1 ^= t1; x0 = ROTR(x0, R[0][0]);
		x4 ^= t4; t0 ^= x0; x0 = ROTR(x0, R[0][1] - R[0][0]);
		x0 ^= t0;
	    } //P6_MACRO();
	    // rlen -= RATE;
	    // m += RATE;
	    // c += RATE;
	}
	System.out.println("CAfter PPlaintext  x0:" +Long.toHexString(x0) +" x1:" +Long.toHexString(x1)+" x2:" +Long.toHexString(x2)+" x3:" +Long.toHexString(x3)+" x4:" +Long.toHexString(x4) +
    		" t0:" +Long.toHexString(t0) +" t1:" +Long.toHexString(t1)+" t2:" +Long.toHexString(t2)+" t3:" +Long.toHexString(t3)+" t4:" +Long.toHexString(t4) );
	// for (i = 0; i < rlen; ++i, ++m, ++c) {
	System.out.println("Before Finalization ");
	for (i = 0; i < rlen ; i++) { // move byte by byte
	    // x0 ^= INS_BYTE(*m, i);
	     //x0 ^= INS_BYTE(load64(Arrays.copyOfRange(m, i, i+RATE)), i);
		x0 ^= INS_BYTE((byte)load64(Arrays.copyOfRange(m, i+1, i+1+RATE)), i); //bck?
		//x0 ^= INS_BYTE(load64(Arrays.copyOfRange(m, i, (i)+RATE)), i);
		//x0 ^= INS_BYTE((long)m[i], i);
	    // *c = EXT_BYTE(x0, i);
	    c[RATE+i] = EXT_BYTE(x0, i);//store64(c,i,x0); 
	    System.out.print(" x0:"+Long.toHexString(x0)+" c["+(i)+"]: " +Long.toHexString(0xff&c[RATE+i]));
	}
	//store64(c,0,x0);
	x0 ^= INS_BYTE(0x80, rlen);
	
	// finalization
	x1 ^= K0;
	x2 ^= K1;
	// P12;
	for ( long C :  constantsP12){
	    x2 ^= C; x0 ^= x4; x4 ^= x3; x2 ^= x1; t0 = x0; t4 = x4; t3 = x3; t1 = x1; t2 = x2;
	    x0 = t0 ^ ((~t1) & t2); x2 = t2 ^ ((~t3) & t4); x4 = t4 ^ ((~t0) & t1);
	    x1 = t1 ^ ((~t2) & t3); x3 = t3 ^ ((~t4) & t0); 
	    x1 ^= x0; t1  = x1; x1 = ROTR(x1, R[1][0]);
	    x3 ^= x2; t2  = x2; x2 = ROTR(x2, R[2][0]);
	    t4  = x4; t2 ^= x2; x2 = ROTR(x2, R[2][1] - R[2][0]);
	    t3  = x3; t1 ^= x1; x3 = ROTR(x3, R[3][0]);
	    x0 ^= x4; x4 = ROTR(x4, R[4][0]);
	    t3 ^= x3; x2 ^= t2; x1 = ROTR(x1, R[1][1] - R[1][0]);
	    t0  = x0; x2 = ~x2; x3 = ROTR(x3, R[3][1] - R[3][0]);
	    t4 ^= x4; x4 = ROTR(x4, R[4][1] - R[4][0]);
	    x3 ^= t3; x1 ^= t1; x0 = ROTR(x0, R[0][0]);
	    x4 ^= t4; t0 ^= x0; x0 = ROTR(x0, R[0][1] - R[0][0]);
	    x0 ^= t0;
	} // P12_MACRO();
	x3 ^= K0;
	x4 ^= K1;
	System.out.println("\nEncrypt After finalization "+" x0:" +Long.toHexString(x0) +" x1:" +Long.toHexString(x1)+" x2:" +Long.toHexString(x2)+" x3:" +Long.toHexString(x3)+" x4:" +Long.toHexString(x4) +
    		" t0:" +Long.toHexString(t0) +" t1:" +Long.toHexString(t1)+" t2:" +Long.toHexString(t2)+" t3:" +Long.toHexString(t3)+" t4:" +Long.toHexString(t4) );
	// return tag
	// ((u64*)c)[0] = U64BIG(x3); // orig 
	// store64(c,mlen,u64x3);
	store64(c,mlen,x3); // bck april
	//load64(Arrays.copyOfRange(c, 0, (0)+RATE));
	// ((u64*)c)[1] = U64BIG(x4); // orig
	/*for( i = 0 ; i < 16 ; i++){ // this is not a useful comparison with c version, 'c' is incremented so does not align the same
		System.out.print(" c["+(i)+"]: "+(c[i]));
	}*/
	
	store64(c,mlen+RATE, x4);
	System.out.println(" u64x3: "+Long.toHexString(U64BIG(x3))+" u64x4: "+Long.toHexString(U64BIG(x4))+" x0:" +Long.toHexString(x0) +" x1:" +Long.toHexString(x1)+" x2:" +Long.toHexString(x2)+" x3:" +Long.toHexString(x3)+" x4:" +Long.toHexString(x4) +
    		" t0:" +Long.toHexString(t0) +" t1:" +Long.toHexString(t1)+" t2:" +Long.toHexString(t2)+" t3:" +Long.toHexString(t3)+" t4:" +Long.toHexString(t4) );
	// *clen = mlen + CRYPTO_KEYBYTES;
	clen = mlen + CRYPTO_KEYBYTES;
	System.out.println("\nEnd mlen " +mlen);
	/*System.out.println("Encrypt END c vals: ");
	for( i = 0 ; i < 16 ; i++){
		System.out.print(" c["+(i)+"]: "+(c[i]));
	}*/
	System.out.print("\nEnd clen " +clen);
	// return 0;
	return clen;
    }
    
    /*int crypto_aead_decrypt(
      unsigned char *m, unsigned long long *mlen,
      unsigned char *nsec,
      const unsigned char *c, unsigned long long clen,
      const unsigned char *ad, unsigned long long adlen,
      const unsigned char *npub,
      const unsigned char *k) {*/
    public static int crypto_aead_decrypt(byte m[], int mlen, byte nsec[], byte c[], int clen, byte ad[],
					  int adlen, byte npub[], byte k[]) {
    	System.out.println("Start of decryption        ++++++++++\n");
    	for(int i = 0 ; i < 27 ; i++){
    		System.out.print(" c["+(i)+"]: "+(c[i]));
    	}
    	//System.out.println("c[0]:"+(0xff&c[0])+" c[15]:"+(0xff&c[15]));
	// u64 K0 = U64BIG(((u64*)k)[0]);
	long K0 = U64BIG((k)[0]);
	// u64 K1 = U64BIG(((u64*)k)[1]);
	long K1 = U64BIG((k)[1]);
	// u64 N0 = U64BIG(((u64*)npub)[0]);
	long N0 = U64BIG((npub)[0]);
	// u64 N1 = U64BIG(((u64*)npub)[1]);
	long N1 = U64BIG((npub)[1]);
	// u64 x0, x1, x2, x3, x4;
	long x0, x1, x2, x3, x4;
	// u64 t0, t1, t2, t3, t4;
	long t0 = 0, t1 = 0, t2 = 0, t3 = 0, t4 = 0;
	// u64 rlen;
	int rlen;
	int i;
	
	// *mlen = 0;
	mlen = 0;
	if (clen < CRYPTO_KEYBYTES)
	    return -1;
	
	// initialization
	// x0 = (u64)((CRYPTO_KEYBYTES * 8) << 24 | (RATE * 8) << 16 | PA_ROUNDS << 8 | PB_ROUNDS << 0) << 32;
	x0 = ((long)((CRYPTO_KEYBYTES * 8) << 24 | (RATE * 8) << 16 | PA_ROUNDS << 8 | PB_ROUNDS << 0)) << 32;
	x1 = K0;
	x2 = K1;
	x3 = N0;
	x4 = N1;
	// P12
	long[] constantsP12 = {0xf0, 0xe1, 0xd2, 0xc3, 0xb4, 0xa5, 0x96, 0x87, 0x78, 0x69, 0x5a, 0x4b};
	for ( long C :  constantsP12){
	    x2 ^= C; x0 ^= x4; x4 ^= x3; x2 ^= x1; t0 = x0; t4 = x4; t3 = x3; t1 = x1; t2 = x2;
	    x0 = t0 ^ ((~t1) & t2); x2 = t2 ^ ((~t3) & t4); x4 = t4 ^ ((~t0) & t1);
	    x1 = t1 ^ ((~t2) & t3); x3 = t3 ^ ((~t4) & t0); 
	    x1 ^= x0; t1  = x1; x1 = ROTR(x1, R[1][0]);
	    x3 ^= x2; t2  = x2; x2 = ROTR(x2, R[2][0]);
	    t4  = x4; t2 ^= x2; x2 = ROTR(x2, R[2][1] - R[2][0]);
	    t3  = x3; t1 ^= x1; x3 = ROTR(x3, R[3][0]);
	    x0 ^= x4; x4 = ROTR(x4, R[4][0]);
	    t3 ^= x3; x2 ^= t2; x1 = ROTR(x1, R[1][1] - R[1][0]);
	    t0  = x0; x2 = ~x2; x3 = ROTR(x3, R[3][1] - R[3][0]);
	    t4 ^= x4; x4 = ROTR(x4, R[4][1] - R[4][0]);
	    x3 ^= t3; x1 ^= t1; x0 = ROTR(x0, R[0][0]);
	    x4 ^= t4; t0 ^= x0; x0 = ROTR(x0, R[0][1] - R[0][0]);
	    x0 ^= t0;

	} // P12_MACRO();
	System.out.println("\nDecrypt after P12  x0:" +Long.toHexString(x0) +" x1:" +Long.toHexString(x1)+" x2:" +Long.toHexString(x2)+" x3:" +Long.toHexString(x3)+" x4:" +Long.toHexString(x4) +
    		" t0:" +Long.toHexString(t0) +" t1:" +Long.toHexString(t1)+" t2:" +Long.toHexString(t2)+" t3:" +Long.toHexString(t3)+" t4:" +Long.toHexString(t4) );
	x3 ^= K0;
	x4 ^= K1;
	
	long[] constantsP6 = {0xf0, 0xe1, 0xd2, 0xc3, 0xb4, 0xa5};
	// process associated data
	// if (adlen) {
	if (adlen > 0) {
	    rlen = adlen;
	    //while (rlen >= RATE) {
	    for (i=0; i < ad.length && rlen >= RATE; i+=RATE, rlen-=RATE) {
	    	// x0 ^= U64BIG(*(u64*)ad);
		    //x0 ^= U64BIG(load64(Arrays.copyOfRange(ad, i, (i)+RATE)));
	    	x0 ^= load64(Arrays.copyOfRange(ad, i, (i)+RATE));
		    // P6;
		    for ( long C :  constantsP6){
		    	x2 ^= C; x0 ^= x4; x4 ^= x3; x2 ^= x1; t0 = x0; t4 = x4; t3 = x3; t1 = x1; t2 = x2;
		    	x0 = t0 ^ ((~t1) & t2); x2 = t2 ^ ((~t3) & t4); x4 = t4 ^ ((~t0) & t1);
		    	x1 = t1 ^ ((~t2) & t3); x3 = t3 ^ ((~t4) & t0); 
		    	x1 ^= x0; t1  = x1; x1 = ROTR(x1, R[1][0]);
		    	x3 ^= x2; t2  = x2; x2 = ROTR(x2, R[2][0]);
		    	t4  = x4; t2 ^= x2; x2 = ROTR(x2, R[2][1] - R[2][0]);
		    	t3  = x3; t1 ^= x1; x3 = ROTR(x3, R[3][0]);
		    	x0 ^= x4; x4 = ROTR(x4, R[4][0]);
		    	t3 ^= x3; x2 ^= t2; x1 = ROTR(x1, R[1][1] - R[1][0]);
		    	t0  = x0; x2 = ~x2; x3 = ROTR(x3, R[3][1] - R[3][0]);
		    	t4 ^= x4; x4 = ROTR(x4, R[4][1] - R[4][0]);
		    	x3 ^= t3; x1 ^= t1; x0 = ROTR(x0, R[0][0]);
		    	x4 ^= t4; t0 ^= x0; x0 = ROTR(x0, R[0][1] - R[0][0]);
		    	x0 ^= t0;
		    } //P6_MACRO();
		    
		// rlen -= RATE;
		// ad += RATE;
	    }
	    System.out.println("Decrypt after P6 1 loop  x0:" +Long.toHexString(x0) +" x1:" +Long.toHexString(x1)+" x2:" +Long.toHexString(x2)+" x3:" +Long.toHexString(x3)+" x4:" +Long.toHexString(x4) +
	    		" t0:" +Long.toHexString(t0) +" t1:" +Long.toHexString(t1)+" t2:" +Long.toHexString(t2)+" t3:" +Long.toHexString(t3)+" t4:" +Long.toHexString(t4) );
	    //for (i = 0; i < rlen; ++i, ++ad)
	    for (i = 0; i < rlen  ; ++i){ 
	    	// x0 ^= INS_BYTE(*ad, i);
	    	System.out.println(" before ins_byte " +ad[i]);
	    	//x0 ^= INS_BYTE(load64(Arrays.copyOfRange(ad, i, i+(RATE))), i);
	    	x0 ^= INS_BYTE((long)ad[i], i);
	    	System.out.println("ins_byte(load64(Arrays.copyOfRange" +Long.toHexString(INS_BYTE((long)ad[i], i)));
		    System.out.println("Decrypt before 2 loop x0:" +Long.toHexString(x0) +" x1:" +Long.toHexString(x1)+" x2:" +Long.toHexString(x2)+" x3:" +Long.toHexString(x3)+" x4:" +Long.toHexString(x4) +
		    		"t0:" +Long.toHexString(t0) +" t1:" +Long.toHexString(t1)+" t2:" +Long.toHexString(t2)+" t3:" +Long.toHexString(t3)+" t4:" +Long.toHexString(t4) );
	    }
	    	x0 ^= INS_BYTE(0x80, rlen);
	    	// P6;
	    	for ( long C :  constantsP6){
	    		x2 ^= C; x0 ^= x4; x4 ^= x3; x2 ^= x1; t0 = x0; t4 = x4; t3 = x3; t1 = x1; t2 = x2;
	    		x0 = t0 ^ ((~t1) & t2); x2 = t2 ^ ((~t3) & t4); x4 = t4 ^ ((~t0) & t1);
	    		x1 = t1 ^ ((~t2) & t3); x3 = t3 ^ ((~t4) & t0); 
	    		x1 ^= x0; t1  = x1; x1 = ROTR(x1, R[1][0]);
	    		x3 ^= x2; t2  = x2; x2 = ROTR(x2, R[2][0]);
	    		t4  = x4; t2 ^= x2; x2 = ROTR(x2, R[2][1] - R[2][0]);
	    		t3  = x3; t1 ^= x1; x3 = ROTR(x3, R[3][0]);
	    		x0 ^= x4; x4 = ROTR(x4, R[4][0]);
	    		t3 ^= x3; x2 ^= t2; x1 = ROTR(x1, R[1][1] - R[1][0]);
	    		t0  = x0; x2 = ~x2; x3 = ROTR(x3, R[3][1] - R[3][0]);
	    		t4 ^= x4; x4 = ROTR(x4, R[4][1] - R[4][0]);
	    		x3 ^= t3; x1 ^= t1; x0 = ROTR(x0, R[0][0]);
	    		x4 ^= t4; t0 ^= x0; x0 = ROTR(x0, R[0][1] - R[0][0]);
	    		x0 ^= t0;
	    	} //P6_MACRO();
	    	System.out.println("Decrypt after P6 2 x0:" +Long.toHexString(x0) +" x1:" +Long.toHexString(x1)+" x2:" +Long.toHexString(x2)+" x3:" +Long.toHexString(x3)+" x4:" +Long.toHexString(x4) +
		    		"t0:" +Long.toHexString(t0) +" t1:" +Long.toHexString(t1)+" t2:" +Long.toHexString(t2)+" t3:" +Long.toHexString(t3)+" t4:" +Long.toHexString(t4) );
	    
	}
	    x4 ^= 1;
	
	    // process plaintext
	    rlen = clen - CRYPTO_KEYBYTES;
	    //	while (rlen >= RATE) {
	    for (i=0; i < ad.length && rlen >= RATE; i+=RATE) {
	    	//*(u64*)m = U64BIG(x0) ^ *(u64*)c;
	    	store64(m,i*RATE, U64BIG(x0) ^ load64(Arrays.copyOfRange(c, i, (i)+RATE)));
	    	// x0 = U64BIG(*((u64*)c));
	    	x0 = U64BIG(load64(Arrays.copyOfRange(c,i, (i)+RATE)));
	    	// 	P6;
	    	for ( long C :  constantsP6){
	    		x2 ^= C; x0 ^= x4; x4 ^= x3; x2 ^= x1; t0 = x0; t4 = x4; t3 = x3; t1 = x1; t2 = x2;
	    		x0 = t0 ^ ((~t1) & t2); x2 = t2 ^ ((~t3) & t4); x4 = t4 ^ ((~t0) & t1);
	    		x1 = t1 ^ ((~t2) & t3); x3 = t3 ^ ((~t4) & t0); 
	    		x1 ^= x0; t1  = x1; x1 = ROTR(x1, R[1][0]);
	    		x3 ^= x2; t2  = x2; x2 = ROTR(x2, R[2][0]);
	    		t4  = x4; t2 ^= x2; x2 = ROTR(x2, R[2][1] - R[2][0]);
	    		t3  = x3; t1 ^= x1; x3 = ROTR(x3, R[3][0]);
	    		x0 ^= x4; x4 = ROTR(x4, R[4][0]);
	    		t3 ^= x3; x2 ^= t2; x1 = ROTR(x1, R[1][1] - R[1][0]);
	    		t0  = x0; x2 = ~x2; x3 = ROTR(x3, R[3][1] - R[3][0]);
	    		t4 ^= x4; x4 = ROTR(x4, R[4][1] - R[4][0]);
	    		x3 ^= t3; x1 ^= t1; x0 = ROTR(x0, R[0][0]);
	    		x4 ^= t4; t0 ^= x0; x0 = ROTR(x0, R[0][1] - R[0][0]);
	    		x0 ^= t0;
	    	} //P6_MACRO();
	    	System.out.println("Decrypt after P6 3 rounds  x0:" +Long.toHexString(x0) +" x1:" +Long.toHexString(x1)+" x2:" +Long.toHexString(x2)+" x3:" +Long.toHexString(x3)+" x4:" +Long.toHexString(x4) +
		    		"t0:" +Long.toHexString(t0) +" t1:" +Long.toHexString(t1)+" t2:" +Long.toHexString(t2)+" t3:" +Long.toHexString(t3)+" t4:" +Long.toHexString(t4) );
	    	//rlen -= RATE;
	    	//m += RATE;
	    	//c += RATE;
	    }
	    // for (i = 0; i < rlen; ++i, ++m, ++c) {
	    for (i = 0; i < rlen ; ++i) {
	    // 	*m = EXT_BYTE(x0, i) ^ *c;
	    	// store64(m, i,(EXT_BYTE(x0, i) ^ load64(Arrays.copyOfRange(c, i*RATE, (i*RATE)+RATE))));
	    	//store64(m, i,(EXT_BYTE(x0, i) ^ (long)c[i])); //]load64(Arrays.copyOfRange(c, i, (i)+RATE))));
	    	System.out.println("x0:" +Long.toHexString(x0) +" c["+i+"]: " +c[i]);
	    	m[i] = (byte)(EXT_BYTE(x0, i) ^ 0xff&c[i]); //]load64(Arrays.copyOfRange(c, i, (i)+RATE))));
	    	x0 &= ~INS_BYTE(0xff, i);
	    	// 	x0 |= INS_BYTE(*c, i);
	    	x0 |= INS_BYTE((long)0xff&c[i], i);
	    	System.out.println("Decrypt loop before finalisation  x0:" +Long.toHexString(x0) +" x1:" +Long.toHexString(x1)+" x2:" +Long.toHexString(x2)+" x3:" +Long.toHexString(x3)+" x4:" +Long.toHexString(x4) +
		    		"t0:" +Long.toHexString(t0) +" t1:" +Long.toHexString(t1)+" t2:" +Long.toHexString(t2)+" t3:" +Long.toHexString(t3)+" t4:" +Long.toHexString(t4) );
	    }
	
	    x0 ^= INS_BYTE(0x80, rlen);
	
	    System.out.println("Decrypt before finalisation  x0:" +Long.toHexString(x0) +" x1:" +Long.toHexString(x1)+" x2:" +Long.toHexString(x2)+" x3:" +Long.toHexString(x3)+" x4:" +Long.toHexString(x4) +
	    		"t0:" +Long.toHexString(t0) +" t1:" +Long.toHexString(t1)+" t2:" +Long.toHexString(t2)+" t3:" +Long.toHexString(t3)+" t4:" +Long.toHexString(t4) );
	    // finalization
	    x1 ^= K0;
	    x2 ^= K1;
	    // 	P12;
	    for ( long C :  constantsP12){
	    	x2 ^= C; x0 ^= x4; x4 ^= x3; x2 ^= x1; t0 = x0; t4 = x4; t3 = x3; t1 = x1; t2 = x2;
	    	x0 = t0 ^ ((~t1) & t2); x2 = t2 ^ ((~t3) & t4); x4 = t4 ^ ((~t0) & t1);
	    	x1 = t1 ^ ((~t2) & t3); x3 = t3 ^ ((~t4) & t0); 
	    	x1 ^= x0; t1  = x1; x1 = ROTR(x1, R[1][0]);
	    	x3 ^= x2; t2  = x2; x2 = ROTR(x2, R[2][0]);
	    	t4  = x4; t2 ^= x2; x2 = ROTR(x2, R[2][1] - R[2][0]);
	    	t3  = x3; t1 ^= x1; x3 = ROTR(x3, R[3][0]);
	    	x0 ^= x4; x4 = ROTR(x4, R[4][0]);
	    	t3 ^= x3; x2 ^= t2; x1 = ROTR(x1, R[1][1] - R[1][0]);
	    	t0  = x0; x2 = ~x2; x3 = ROTR(x3, R[3][1] - R[3][0]);
	    	t4 ^= x4; x4 = ROTR(x4, R[4][1] - R[4][0]);
	    	x3 ^= t3; x1 ^= t1; x0 = ROTR(x0, R[0][0]);
	    	x4 ^= t4; t0 ^= x0; x0 = ROTR(x0, R[0][1] - R[0][0]);
	    	x0 ^= t0;
	    } // P12_MACRO();
	    x3 ^= K0;
	    x4 ^= K1;
	    System.out.println("Decrypt after P12 2 rounds  x0:" +Long.toHexString(x0) +" x1:" +Long.toHexString(x1)+" x2:" +Long.toHexString(x2)+" x3:" +Long.toHexString(x3)+" x4:" +Long.toHexString(x4) +
	    		"t0:" +Long.toHexString(t0) +" t1:" +Long.toHexString(t1)+" t2:" +Long.toHexString(t2)+" t3:" +Long.toHexString(t3)+" t4:" +Long.toHexString(t4) );
	    
	    // return -1 if verification fails
	    // 	if (((u64*)c)[0] != U64BIG(x3) ||
	    if (load64(Arrays.copyOfRange(c,rlen,rlen+RATE)) != x3 ||
	    		//	((u64*)c)[1] != U64BIG(x4))
	    		load64(Arrays.copyOfRange(c,rlen+RATE,rlen+(2*RATE))) != x4)
	    	return -1;
	
	// return plaintext
	// *mlen = clen - CRYPTO_KEYBYTES;
	mlen = clen - CRYPTO_KEYBYTES;
	// return 0;
	return mlen;
	}
}



