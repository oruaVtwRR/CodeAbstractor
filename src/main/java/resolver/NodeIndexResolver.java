package resolver;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.expr.SimpleName;

import java.util.HashMap;

public class NodeIndexResolver {

    private HashMap<Node, Integer> assignedIndexValues;
    private HashMap<Integer, Integer> assignedIndexValuesHashed;
    private HashMap<String, Integer> maxIndexOfType;

    public NodeIndexResolver(){
        this.assignedIndexValues = new HashMap<>();
        this.assignedIndexValuesHashed = new HashMap<>();
        this.maxIndexOfType = new HashMap<>();
    }

    public int getNodeIndex(Node node){
        if(this.assignedIndexValues.containsKey(node)){
            return this.assignedIndexValues.get(node);
        } else {
            String classString = node.getClass().toString();
            Integer newIndex = this.maxIndexOfType.getOrDefault(classString, 0) + 1;
            this.maxIndexOfType.put(classString, newIndex);
            this.assignedIndexValues.put(node, newIndex);
            return newIndex;
        }
    }

    public int getNodeIndexHashed(Node node){

        int hash = node.hashCode();
        if(node instanceof SimpleName){
            hash = ((SimpleName)node).asString().hashCode();
        }

        if(this.assignedIndexValuesHashed.containsKey(hash)){
            return this.assignedIndexValuesHashed.get(hash);
        } else {
            String classString = node.getClass().toString();
            Integer newIndex = this.maxIndexOfType.getOrDefault(classString, 0) + 1;
            this.maxIndexOfType.put(classString, newIndex);
            this.assignedIndexValuesHashed.put(hash, newIndex);
            return newIndex;
        }
    }

}
