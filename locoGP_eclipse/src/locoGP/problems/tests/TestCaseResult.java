package locoGP.problems.tests;

public class TestCaseResult {
	private int errorCount ;
	private boolean runtimeException;
	
	public TestCaseResult(int errorCount, boolean runtimeException){
		setErrorCount(errorCount);
		setRuntimeException(runtimeException);
	}

	public int getErrorCount() {
		return errorCount;
	}

	private void setErrorCount(int errorCount) {
		this.errorCount = errorCount;
	}

	public boolean hadRuntimeException() {
		return runtimeException;
	}

	private void setRuntimeException(boolean runtimeException) {
		this.runtimeException = runtimeException;
	}
}
