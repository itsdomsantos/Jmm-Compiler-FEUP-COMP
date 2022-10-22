package pt.up.fe.comp;

import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.Type;

import java.util.Map;

public class MySymbol extends Symbol {
    private final EntityTypeAuxiliar entity;

    public MySymbol(Type type, String variableName, EntityTypeAuxiliar variable) {
        super(type, variableName);
        this.entity = variable;
    }


    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.getName() == null) ? 0 : this.getName().hashCode());
        result = prime * result + ((this.getType() == null) ? 0 : this.getType().hashCode());
        result = prime * result + ((this.getEntity() == null) ? 0 : this.getEntity().hashCode());

        return result;
    }

    private EntityTypeAuxiliar getEntity() {
        return this.entity;
    }

}
