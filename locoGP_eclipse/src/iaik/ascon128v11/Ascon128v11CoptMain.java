package iaik.ascon128v11;



public class Ascon128v11CoptMain {
  public final static int MAXLEN = 65536;

  public static void main(String[] args) {

    int i;
    int MLEN = 1;
    if (args.length == 1)
      MLEN = Integer.decode(args[0]);

    int alen = MAXLEN;
    int mlen = MAXLEN;
    int clen = MAXLEN + Ascon128v11.CRYPTO_ABYTES;
    // associated data
    byte a[] = {'A','S','C','O','N', 'A', 'S','C','O','N'}; //new byte[alen];
    // plaintext message
    byte m[] = {'a','s','c','o','n','A','S','C','O','N' , 'N'}; //new byte[mlen];
    // cipher text or "tag" ?
    byte c[] = new byte[m.length + Ascon128v11.CRYPTO_ABYTES];
    byte nsec[] = new byte[Ascon128v11.CRYPTO_NSECBYTES]; // ???
    // public message number (nonce)
    byte npub[] =
    	{(byte) 0x0, (byte) 0x0, (byte) 0x0, (byte) 0x0, (byte) 0x0, (byte) 0x0, (byte) 0x0,
            (byte) 0x0, (byte) 0x0, (byte) 0x0, (byte) 0x0, (byte) 0x0, (byte) 0x0,
            (byte) 0x0, (byte) 0x0, (byte) 0x0};
    
        /*{(byte) 0x0, (byte) 0xc2, (byte) 0x54, (byte) 0xf8, (byte) 0x1b, (byte) 0xe8, (byte) 0xe7,
            (byte) 0x8d, (byte) 0x76, (byte) 0x5a, (byte) 0x2e, (byte) 0x63, (byte) 0x33,
            (byte) 0x9f, (byte) 0xc9, (byte) 0x9a};*/
    // secret key
    byte k[] =
        {0x0, (byte) 0x0, 0x0, 0x0, 0x0, (byte) 0x0, 0x0, (byte) 0x0, 0x0, (byte) 0x0,
            (byte) 0x0, (byte) 0x0, (byte) 0x0, (byte) 0x0, (byte) 0x0, 0x0};
    /*{0x67, (byte) 0xc6, 0x69, 0x73, 0x51, (byte) 0xff, 0x4a, (byte) 0xec, 0x29, (byte) 0xcd,
        (byte) 0xba, (byte) 0xab, (byte) 0xf2, (byte) 0xfb, (byte) 0xe3, 0x46};*/

    /*for (i = 0; i < MLEN; ++i)
      a[i] = (byte) ('A' + i % 26);
    for (i = 0; i < MLEN; ++i)
      m[i] = (byte) ('a' + i % 26);*/

   // for (alen = 0; alen <= MLEN; ++alen)
   //   for (mlen = 0; mlen <= MLEN; ++mlen) {
    alen=a.length; 
    mlen=m.length;
    clen=c.length;
        Ascon128v11.print("k", k, Ascon128v11.CRYPTO_KEYBYTES, 0);
        Ascon128v11.print("n", npub, Ascon128v11.CRYPTO_KEYBYTES, 0);
        Ascon128v11.print("a", a, alen, 0);
        Ascon128v11.print("m", m, mlen, 0);
        clen = Ascon128v11COpt.crypto_aead_encrypt(c, clen, m, mlen, a, alen, nsec, npub, k);
        //clen = Ascon128v11.crypto_aead_encrypt(c, clen, m, mlen, a, alen, nsec, npub, k);
        System.out.printf("\n");
        Ascon128v11.print("c", c, mlen, 0);
        Ascon128v11.print("t", c, clen-mlen,mlen); //Ascon128v11.CRYPTO_ABYTES, clen - Ascon128v11.CRYPTO_ABYTES);
        mlen = Ascon128v11COpt.crypto_aead_decrypt(m, mlen, nsec, c, clen, a, alen, npub, k);
        //mlen = Ascon128v11.crypto_aead_decrypt(m, mlen, nsec, c, clen, a, alen, npub, k);
        if (mlen != -1) {
          Ascon128v11.print("p", m, mlen, 0);
        } else
          System.out.printf("verification failed\n");
        System.out.printf("\n");
     // }
    return;
  }

}