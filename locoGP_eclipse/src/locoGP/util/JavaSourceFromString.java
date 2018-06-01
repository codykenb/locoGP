package locoGP.util;

import java.net.URI;

import javax.tools.SimpleJavaFileObject;
/*Thanks:
 * http://www.javablogging.com/dynamic-in-memory-compilation/
*/
public class JavaSourceFromString extends SimpleJavaFileObject {
	final String code;

	JavaSourceFromString(String code, String name) {
		super(URI.create("string:///" + name.replace('.', '/')
				+ Kind.SOURCE.extension), Kind.SOURCE);
		this.code = code;
	}

	public CharSequence getCharContent(boolean ignoreEncodingErrors) {
		return code;
	}
}
