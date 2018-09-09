package bymihaj;

import java.util.List;

public class AssetsResponse {

    protected List<Property> list;
    
    public AssetsResponse(List<Property> list) {
        this.list = list;
    }
    
    public List<Property> getProperties() {
        return list;
    }
}
