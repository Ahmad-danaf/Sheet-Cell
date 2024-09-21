package desktop.utils.parameters;

import java.util.List;

public class FilterParameters {
    private String rangeInput;
    private String columnInput;
    private List<String> filterValues;

    public FilterParameters(String rangeInput, List<String> filterValues) {
        this.rangeInput = rangeInput;
        this.columnInput = columnInput;
        this.filterValues = filterValues;
    }

    public String getRangeInput() {
        return rangeInput;
    }

    public String getColumnInput() {
        return columnInput;
    }

    public List<String> getFilterValues() {
        return filterValues;
    }


}
