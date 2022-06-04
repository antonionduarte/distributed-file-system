package tp1.server.operations;

public class JsonOperation {

	private String json;
	private OperationType operationType;

	public JsonOperation(String json, OperationType operationType) {
		this.json = json;
		this.operationType = operationType;
	}

	public String getJson() {
		return json;
	}

	public OperationType getOperationType() {
		return operationType;
	}
}
