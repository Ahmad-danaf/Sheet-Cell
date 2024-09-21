package desktop.utils.parameters;

public class SortParameters {
    String rangeInput;
    String columnsInput;

    public SortParameters(String rangeInput, String columnsInput) {
        this.rangeInput = rangeInput;
        this.columnsInput = columnsInput;
    }

    public String getRangeInput() {
        return rangeInput;
    }

    public String getColumnsInput() {
        return columnsInput;
    }
}
