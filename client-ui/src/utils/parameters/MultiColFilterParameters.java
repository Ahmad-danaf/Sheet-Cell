package utils.parameters;

import java.util.List;
import java.util.Map;

public class MultiColFilterParameters {
    private String rangeInput;
    private Map<Integer, List<String>> filterCriteria;

    public MultiColFilterParameters(String rangeInput, Map<Integer, List<String>> filterCriteria) {
        this.rangeInput = rangeInput;
        this.filterCriteria = filterCriteria;
    }

    public String getRangeInput() {
        return rangeInput;
    }

    public Map<Integer, List<String>> getFilterCriteria() {
        return filterCriteria;
    }
}

