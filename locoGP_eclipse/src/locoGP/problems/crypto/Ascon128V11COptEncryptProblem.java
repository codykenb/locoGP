package locoGP.problems.crypto;

import java.util.Random;

import locoGP.problems.tests.AsconEncryptTestCase;
import locoGP.problems.tests.TestCase;


public class Ascon128V11COptEncryptProblem extends Ascon128V11EncryptProblem{
	

	public Ascon128V11COptEncryptProblem(){
		setNames("COpt");
		problemStrings = loadFiles("COpt");
	}
		
}
