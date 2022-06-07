package tp1.server.operations;

public abstract class Operation {

	private final OperationType operationType;

	protected Operation(OperationType operationType) {
		this.operationType = operationType;
	}

	public OperationType getOperationType() {
		return operationType;
	}
}
