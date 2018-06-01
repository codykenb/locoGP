package locoGP.problems.crypto;

import java.util.Random;

import locoGP.problems.tests.AsconEncryptTestCase;
import locoGP.problems.tests.TestCase;


public class Ascon128V11COptDecryptProblem extends Ascon128V11DecryptProblem{
	

	public Ascon128V11COptDecryptProblem(){
		setNames("COpt");
		problemStrings = loadFiles("COpt");
	}
		
}
