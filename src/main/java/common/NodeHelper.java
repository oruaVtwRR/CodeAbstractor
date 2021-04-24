package common;

import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.PrimitiveType;
import com.github.javaparser.ast.type.VoidType;
import com.github.javaparser.resolution.UnsolvedSymbolException;

import java.util.ArrayList;
import java.util.List;


public class NodeHelper {

    public static synchronized String getConstantName(Node node){

        String returnString = CommonStrings.EMPTYSTRING;

        if(node instanceof Modifier){
            returnString = ((Modifier)node).getKeyword().toString();
        } else if(node instanceof VoidType){
            returnString =  CommonStrings.TYPENAME_VOID;
        } else if(node instanceof NullLiteralExpr){
            returnString =  CommonStrings.KEYWORD_NULL;
        } else if(node instanceof BooleanLiteralExpr){
            returnString =  ((BooleanLiteralExpr) node).getValue() ? CommonStrings.LITERAL_TRUE : CommonStrings.LITERAL_FALSE;
        } else if(node instanceof BinaryExpr){
            returnString =  ((BinaryExpr)node).getOperator().toString();
        } else if(node instanceof PrimitiveType){
            returnString =  ((PrimitiveType) node).getType().name();
        } else if(node instanceof UnaryExpr){
            returnString = ((UnaryExpr) node).getOperator().toString();
        } else if(node instanceof Name){
            returnString = "";
        }

        //replace chars that separate nodes in the String representation of the paths
        return returnString.replaceAll(CommonStrings.SEPARATOR_PATH_PARTS, CommonStrings.EMPTYSTRING);
    }

    /**
     * if other nodetypes are related e.g. sharing the same ids (methods, classes)
     * @param nodeType initial nodeType for which the related nodeTypes get returned
     * @return all the related nodeTypes to the initial nodeType
     */
    public static List<String> getAllRelatedNodeTypes(String nodeType){
        List<String> output = new ArrayList<>();

        switch (nodeType) {
            case CommonStrings.AST_CLASS_DEC:
            case CommonStrings.AST_CLASS_TYPE:
                output.add(CommonStrings.AST_CLASS_DEC);
                output.add(CommonStrings.AST_CLASS_TYPE);
                break;
            case CommonStrings.AST_METHOD_CALL:
            case CommonStrings.AST_METHOD_DEC:
                output.add(CommonStrings.AST_METHOD_DEC);
                output.add(CommonStrings.AST_METHOD_CALL);
                break;
            case CommonStrings.AST_CONSTRUCTOR_DEC:
            case CommonStrings.AST_OBJECT_CREATION:
                output.add(CommonStrings.AST_CONSTRUCTOR_DEC);
                output.add(CommonStrings.AST_OBJECT_CREATION);
                break;
            default:
                output.add(nodeType);
                break;
        }
        return output;
    }

    public static synchronized String getQualifiedNameMethodCallExpr(MethodCallExpr methodCallExpr){
        try{
            return methodCallExpr.resolve().getQualifiedSignature();
        } catch (RuntimeException e){
            return methodCallExpr.getName().toString();
        }
    }

    public static synchronized String getQualifiedNameClassOrInterfaceType(ClassOrInterfaceType classOrInterfaceType){
        try{
            return classOrInterfaceType.resolve().asReferenceType().getQualifiedName();
        } catch(UnsolvedSymbolException | UnsupportedOperationException e){
            return classOrInterfaceType.getNameWithScope();
        } catch (Exception e){
            return classOrInterfaceType.getName().toString();
        }
    }

    public static synchronized String getQualifiedNameClassOrInterfaceDeclaration(ClassOrInterfaceDeclaration classOrInterfaceDeclaration){
        try{
            return classOrInterfaceDeclaration.resolve().asReferenceType().getQualifiedName();
        } catch(UnsolvedSymbolException e){
            return classOrInterfaceDeclaration.getName().toString();
        }
    }

    public static synchronized String getQualifiedNameMethodDeclaration(MethodDeclaration methodDeclaration){
        try{
            return methodDeclaration.resolve().getQualifiedSignature();
        } catch(UnsolvedSymbolException | UnsupportedOperationException e){
            return methodDeclaration.getName().toString();
        }
    }

    public static synchronized String getQualifiedSignatureConstructor(ConstructorDeclaration constructorDeclaration){
        try{
            return constructorDeclaration.resolve().getQualifiedSignature();
        } catch(RuntimeException e){
            return constructorDeclaration.getName().toString();
        }
    }

    public static synchronized String getQualifiedSignatureObjectCreation(ObjectCreationExpr objectCreationExpr){
        try{
            return objectCreationExpr.resolve().getQualifiedSignature();
        } catch(RuntimeException e){
            return objectCreationExpr.getTypeAsString();// getName().toString();
        }
    }
}
