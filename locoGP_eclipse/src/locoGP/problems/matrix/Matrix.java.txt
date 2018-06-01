package sfriberg.matrixmultiplication;

import static java.util.Objects.requireNonNull;

import java.util.Arrays;
import java.util.function.Supplier;

public class Matrix {

	private final double[][] matrix;
	private final int rows;
	private final int cols;

	private Matrix(int rows, int cols, double[][] matrix) {
		requireNonNull(matrix);
		this.rows = rows;
		this.cols = cols;
		this.matrix = matrix;
	}

	public Matrix(int rows, int cols) {
		this(rows, cols, new double[rows][cols]);
	}

	public Matrix(int rows, int cols, Supplier<Double> initialValues) {
		this(rows, cols, new double[rows][cols]);
		for (int row = 0; row < rows; row++) {
			for (int col = 0; col < cols; col++) {
				this.matrix[row][col] = initialValues.get();
			}
		}
	}

	public int rows() {
		return rows;
	}

	public int cols() {
		return cols;
	}

	public double getValue(int row, int col) {
		return matrix[row][col];
	}

	public void setValue(int row, int col, double value) {
		matrix[row][col] = value;
	}

	public Matrix multiply(Matrix other) {
		if (this.cols() != other.rows()) {
			throw new IllegalArgumentException(String.format("Can't matrix multiply a %d x %d with a %d x %d.", this.rows(), this.cols(), other.rows(), other.cols()));
		}

		Matrix result = new Matrix(this.rows(), other.cols());

		for (int row = 0; row < result.rows(); row++) {
			for (int col = 0; col < result.cols(); col++) {
				for (int i = 0; i < this.cols(); i++) {
					result.setValue(row, col, result.getValue(row, col) + this.getValue(row, i) * other.getValue(i, col));
				}
			}
		}

		return result;
	}

	public Matrix multiplyInlined(Matrix other) {
		if (this.cols() != other.rows()) {
			throw new IllegalArgumentException(String.format("Can't matrix multiply a %d x %d with a %d x %d.", this.rows(), this.cols(), other.rows(), other.cols()));
		}

		Matrix result = new Matrix(this.rows(), other.cols());
		int rRows = result.rows(), rCols = result.cols(), innerSize = this.cols();

		for (int row = 0; row < rRows; row++) {
			for (int col = 0; col < rCols; col++) {
				for (int i = 0; i < innerSize; i++) {
					result.matrix[row][col] += this.matrix[row][i] * other.matrix[i][col];
				}
			}
		}

		return result;
	}

	public Matrix multiplyBlock(Matrix other) {
		return multiplyBlock(other, 32);
	}

	public Matrix multiplyBlock(Matrix other, int block) {
		if (this.cols() != other.rows()) {
			throw new IllegalArgumentException(String.format("Can't matrix multiply a %d x %d with a %d x %d.", this.rows(), this.cols(), other.rows(), other.cols()));
		}

		Matrix result = new Matrix(this.rows(), other.cols());

		int b = block;
		for (int row0 = 0; row0 < result.rows(); row0 += b) {
			for (int col0 = 0; col0 < result.cols(); col0 += b) {
				for (int i0 = 0; i0 < this.cols(); i0 += b) {
					for (int row = row0; row < Math.min(row0 + b, result.rows()); row++) {
						for (int i = i0; i < Math.min(i0 + b, this.cols()); i++) {
							for (int col = col0; col < Math.min(col0 + b, result.cols()); col++) {
								result.matrix[row][col] += this.matrix[row][i] * other.matrix[i][col];
							}
						}
					}
				}
			}
		}

		return result;
	}

	public Matrix multiplyReorderedInlined(Matrix other) {
		if (this.cols() != other.rows()) {
			throw new IllegalArgumentException(String.format("Can't matrix multiply a %d x %d with a %d x %d.", this.rows(), this.cols(), other.rows(), other.cols()));
		}

		Matrix result = new Matrix(this.rows(), other.cols());
		int rRows = result.rows(), rCols = result.cols(), innerSize = this.cols();

		for (int row = 0; row < rRows; row++) {
			double[] resultRow = result.matrix[row];
			for (int i = 0; i < innerSize; i++) {
				double thisValue = this.matrix[row][i];
				double[] otherRow = other.matrix[i];
				for (int col = 0; col < rCols; col++) {
					resultRow[col] += thisValue * otherRow[col];
				}
			}
		}

		return result;
	}

	@Override
	public int hashCode() {
		return Arrays.hashCode(matrix);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final Matrix other = (Matrix) obj;
		if (this.rows != other.rows) {
			return false;
		}
		if (this.cols != other.cols) {
			return false;
		}
		if (!Arrays.deepEquals(this.matrix, other.matrix)) {
			return false;
		}
		return true;
	}
}
