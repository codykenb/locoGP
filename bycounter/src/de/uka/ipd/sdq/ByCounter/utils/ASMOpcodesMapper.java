package de.uka.ipd.sdq.ByCounter.utils;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.logging.Logger;

import org.objectweb.asm.Opcodes;

/**This helper class provides the mapping from <code>int</code> opcodes of ASM
 * to their <code>String</code> meanings and back. It is intended to be used as 
 * a singleton. It is not optimized for performance. 
 * @author Michael Kuperberg
 * @author Martin Krogmann
 * @since 0.1
 * @version 1.2
 */
public class ASMOpcodesMapper implements Opcodes {

	/**
	 * Singleton instance; dao = display ASM opcodes (not "data access object")
	 */
	private static final ASMOpcodesMapper mapper = new ASMOpcodesMapper();
	
	/** 
	 * Get the singleton instance
	 *  @return the singleton instance
	 */
	public static ASMOpcodesMapper getInstance(){
		return instance();
	}
	
	/**
	 * Get the singleton instance
	 * @return the singleton instance
	 */
	public static ASMOpcodesMapper instance(){
		return mapper;
	}
	
	/**The main method prints all ASM instructions (bytecodes) to the screen;
	 * the instructions are sorted by their numeric (<code>int</code>) values.
	 * @param args not utilized in this class
	 */
	@SuppressWarnings("nls")
	public static void main(String[] args){
		ASMOpcodesMapper daoInstance = ASMOpcodesMapper.instance();
		System.out.println("Running from "+new File(".").getAbsolutePath());
		System.out.println("If no further output appears below this line, " +
				"check you logger installation and configuration.");
		daoInstance.printAllInstructions(daoInstance);
	}
	
	/**
	 * This <code>Map</code> contains the bytecode <code>Integer</code>s 
	 * as keys and their <code>String</code> descriptions as values.
	 */
	private Map<Integer, String> codes;

	/**
	 * This field determines whether to list the elements of <code>codes</code>
	 * during the construction of the singleton instance or not.
	 * */
	private boolean displayList = false;

	/**
	 * The java.util logger
	 */
	private Logger log;

	private TreeMap<String, Integer> opcodesByName;
	
	/**
	 * The default constructor fills the <code>fields</code> array using 
	 * Java Reflection API. The list of opcodes is not printed at startup. 
	 */
	private ASMOpcodesMapper(){
		this(false);
	}
	
	/**The default constructor fills the <code>fields</code> array using Java Reflection API. 
	 * @param displayListAtStartup Flag showing whether to list all opcodes at startup.
	 */
	@SuppressWarnings({ "nls", "boxing" })
	private ASMOpcodesMapper(boolean displayListAtStartup){
		super();
		this.displayList = displayListAtStartup;
		this.log = Logger.getLogger(this.getClass().getCanonicalName());

		this.codes = new HashMap<Integer,String>();
		this.opcodesByName = new TreeMap<String,Integer>();
		/**The array of class fields is used to retrieve through Java Reflection 
		 * API the names of the fields inside <b>this</b> class. Therefore, since 
		 * <code>java.lang.Class.getFields()</code> returns public fields only, 
		 * it is required that all non-ASM fields in this class are private.*/
		Field[] fields = this.getClass().getFields();
		int counter = 0;
		Integer opcode_curr;
		String opcodeName_curr;
		for (Field field: fields) {
			try {
				// with ASM 3.3, there are additional non-integer fields, so 
				// we need to check for these
				if(field.get(this).getClass().equals(Integer.class)) {
					opcode_curr = ((Integer) field.get(this));
				} else {
					continue;
				}
				opcodeName_curr = field.getName();
				this.codes.put(opcode_curr, opcodeName_curr);
				this.opcodesByName.put(opcodeName_curr, opcode_curr);
				if(this.displayList){
					this.log.fine(field.getName()+": "+field.get(this));
				}
			} catch (IllegalArgumentException e1) {
				throw new RuntimeException(e1);
			} catch (IllegalAccessException e1) {
				throw new RuntimeException(e1);
			}
			counter++;
		}
		//TODO document this...
		this.codes.put(IByCounterConstants.NEWARRAY_I,"NEWARRAY_I");
		this.codes.put(IByCounterConstants.NEWARRAY_L,"NEWARRAY_L");
		this.codes.put(IByCounterConstants.NEWARRAY_F,"NEWARRAY_F");
		this.codes.put(IByCounterConstants.NEWARRAY_D,"NEWARRAY_D");
		this.codes.put(IByCounterConstants.NEWARRAY_BOOL,"NEWARRAY_BOOL");
		this.codes.put(IByCounterConstants.NEWARRAY_CHAR,"NEWARRAY_CHAR");
		this.codes.put(IByCounterConstants.NEWARRAY_BYTE,"NEWARRAY_BYTE");
		this.codes.put(IByCounterConstants.NEWARRAY_SHORT,"NEWARRAY_SHORT");
		this.codes.put(IByCounterConstants.MULTIANEWARRAY_2,"MULTIANEWARRAY_2");
		this.codes.put(IByCounterConstants.MULTIANEWARRAY_3,"MULTIANEWARRAY_3");
		this.codes.put(IByCounterConstants.MULTIANEWARRAY_4,"MULTIANEWARRAY_4");
//		this.codes.put(1008,"NOP");
		
		this.log.fine("Number of opcodes saved by DisplayASMOpcode: "+counter);
	}
	
	/**
	 * Gets string descriptions for the opcodes specified in the list.
	 * @param opcodes The list of opcodes to get the description for.
	 * @return A list of strings that contains the descriptions for the 
	 * specified opcodes.
	 */
	public List<String> describeOpcodeList(List<Integer> opcodes){
		List<String> descriptions = new ArrayList<String>();
		for(int opcode : opcodes) {
			descriptions.add(this.getOpcodeString(opcode));
		}
		return descriptions;
	}

	/**
	 * TODO MK refactor? use iterator?
	 * Gets string descriptions for lists of opcodes.
	 * @param opcodes A list of lists of opcodes.
	 * @return A list of lists of opcode descriptions.
	 */
	public List<List<String>> describeOpcodeListList(List<List<Integer>> opcodes){
		ArrayList<List<String>> descriptions = new ArrayList<List<String>>();
		for(int i=0;i<opcodes.size();i++){
			descriptions.add(this.describeOpcodeList(opcodes.get(i)));
		}
		return descriptions;
	}
	
	/**
	 * 
	 * @return The {@link Set} of the integers that represent all opcodes.
	 */
	public Set<Integer> getAllOpcodes(){
		return this.codes.keySet();
	}

	/**
	 * 
	 * @return A {@link List} of the integers that represent all opcodes.
	 */
	public List<Integer> getAllOpcodesAsList(){
		Set<Integer> opcodesSet = this.codes.keySet();
		List<Integer> opcodesList = new ArrayList<Integer>();
		for (Integer opcode : opcodesSet){
			opcodesList.add(opcode);
		}
		return opcodesList;
	}

	/**Get operations <code>int</code> value from its String description 
	 * (name of the field in <code>org.objectweb.asm.Opcodes</code>).
	 * @param opcode name of the bytecode instruction in ASM
	 * @return value of the bytecode instruction in ASM*/
	public int getOpcodeInt(String opcode){
		return this.getOpcodeInteger(opcode).intValue();
	}
	
	/**Get operations <code>Integer</code> value from its String description 
	 * (name of the field in <code>org.objectweb.asm.Opcodes</code>).
	 * @param opcode name of the bytecode instruction in ASM
	 * @return value of the bytecode instruction in ASM*/
	public Integer getOpcodeInteger(String opcode){
		for (Iterator<Integer> iter = this.codes.keySet().iterator(); iter.hasNext();) {
			Integer element = iter.next();
			String value = this.codes.get(element);
			if(value.equals(opcode)){
				return element;
			}
		}
		return new Integer(Integer.MIN_VALUE);
	}
	
	/**Get operations <code>String</code> description from its <code>int</code> 
	 * value (name of the field is in <code>org.objectweb.asm.Opcodes</code>).
	 * @param opcode name of the bytecode instruction in ASM
	 * @return value of the bytecode instruction in ASM (null if opcode is not 
	 * inside internal data structures).*/
	@SuppressWarnings("boxing")
	public String getOpcodeString(int opcode){
		return this.codes.get(opcode);
	}
	
	/**Iterate over <code>int</code>s from 0 to 255 (incl.) and print the name 
	 * of the corresponding ASM bytecode instruction (if it exists; 
	 * <code>null</code> otherwise.
	 * @param daoInstance the instance of <code>DisplayASMOpcodes</code>
	 */
	@SuppressWarnings("nls")
	private void printAllInstructions(ASMOpcodesMapper daoInstance){
		for (int i = 0; i < 256; i++) {
//			log.fine("self-test: "+i+" corresponds to "+
//					daoInstance.getOpcodeString(i));
			System.out.println("self-test: "+i+" corresponds to "+
					daoInstance.getOpcodeString(i));
		}
		Iterator<String> iterStr = this.opcodesByName.keySet().iterator();
		String key;
		System.out.println("Opcodes by name, sorted alphabetically"); 
		while(iterStr.hasNext()){
			key = iterStr.next();
			System.out.println(key+" : "+this.opcodesByName.get(key));
		}
	}
}
