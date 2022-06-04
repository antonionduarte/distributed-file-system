package tp1.server.operations;

public class JsonOperation {

	private final Operation operation;
	private final OperationType operationType;

	public JsonOperation(Operation operation, OperationType operationType) {
		this.operation = operation;
		this.operationType = operationType;
	}

	public Operation getOperation() {
		return operation;
	}

	public OperationType getOperationType() {
		return operationType;
	}
}
