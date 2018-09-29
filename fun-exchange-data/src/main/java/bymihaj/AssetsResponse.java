package bymihaj;

import java.util.Map;

public class AssetsResponse {

    protected Map<Symbol, Property> propertyMap;
    
    public AssetsResponse(Map<Symbol, Property> propertyMap) {
        this.propertyMap = propertyMap;
    }
    
    public Map<Symbol, Property> getProperties() {
        return propertyMap;
    }
}
