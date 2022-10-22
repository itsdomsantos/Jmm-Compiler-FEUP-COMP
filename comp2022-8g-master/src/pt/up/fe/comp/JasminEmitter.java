package pt.up.fe.comp;

import org.specs.comp.ollir.*;
import org.specs.comp.ollir.Type;
import pt.up.fe.comp.jmm.jasmin.JasminBackend;
import pt.up.fe.comp.jmm.jasmin.JasminResult;
import pt.up.fe.comp.jmm.ollir.OllirResult;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.Stage;

import java.util.*;

public class JasminEmitter implements JasminBackend {

    int labelOperCounter = 0;
    int instrCurrStackSize = 0;
    int instrMaxStackSize = 0;
    int comparisons = 0;
    Method currentMethod;
    String className;

    @Override
    public JasminResult toJasmin(OllirResult ollirResult) {
        ClassUnit classUnit = ollirResult.getOllirClass();

        try {
            classUnit.checkMethodLabels();
        } catch(Exception e) {
            return new JasminResult(classUnit.getClassName(), null,
                    Collections.singletonList(Report.newError(Stage.GENERATION, 31, 8, "Exception during Jasmin generation", e)));
        }
        classUnit.buildCFGs();
        classUnit.buildVarTables();

        className = classUnit.getClassName();

        StringBuilder jasminCode = new StringBuilder();
        List<Report> reports = ollirResult.getReports();
        Map<String, String> config = ollirResult.getConfig();

        jasminCode.append(".class public ").append(className).append('\n');

        String ollirSuperClass = (classUnit.getSuperClass() == null ) ? "java/lang/Object" : getFullyQualifiedName(classUnit);
        jasminCode.append(".super ").append(ollirSuperClass).append("\n\n");

        for (Field field: classUnit.getFields())
            jasminCode.append(fieldToJasminString(field));

        if (!classUnit.getFields().isEmpty()) jasminCode.append("\n");

        for (Method method : classUnit.getMethods())
            jasminCode.append(methodToJasminString(method, ollirSuperClass));

        return new JasminResult(className, jasminCode.toString(), reports, config);
    }

    private String fieldToJasminString(Field field) {

        StringBuilder res = new StringBuilder(".field ");

        res.append( accessModifierToJasminString(field.getFieldAccessModifier())).append(" ");

        if (field.isStaticField()) res.append("static ");
        if (field.isFinalField()) res.append("final ");

        res.append(field.getFieldName()).append(" ");
        res.append(typeToJasminString(field.getFieldType())).append(" ");

        if (field.isInitialized()) res.append(" = ").append(field.getInitialValue());

        return res.append('\n').toString();
    }

    private String methodToJasminString(Method method, String ollirSuperClass) { //TODO: check if second argument is correct
        currentMethod = method;
        instrCurrStackSize = 0;
        instrMaxStackSize = 0;
        comparisons = 0;

        StringBuilder res = new StringBuilder(".method ");

        res.append( accessModifierToJasminString(method.getMethodAccessModifier())).append(" ");

        if (method.isConstructMethod())
            return res.append("<init>()V\n\taload_0\n\tinvokenonvirtual ").append(ollirSuperClass).
                    append("/<init>()V\n\treturn\n.end method\n\n").toString();

        if (method.isStaticMethod()) res.append("static ");
        if (method.isFinalMethod()) res.append("final ");

        res.append(method.getMethodName()).append("(");
        for (Element param : method.getParams())
            res.append(typeToJasminString(param.getType()));
        res.append(")");

        res.append(typeToJasminString(method.getReturnType())).append("\n");

        StringBuilder methodBody = new StringBuilder();
        for (Instruction instruction : method.getInstructions()) {
            for (String s : method.getLabels().keySet()) {
                if (method.getLabels().get(s) == instruction) {
                    methodBody.append(s).append(":\n");
                }
            }
            methodBody.append(instructionToJasminString(instruction));
            if (instruction.getInstType() == InstructionType.CALL) {
                if (((CallInstruction) instruction).getReturnType().getTypeOfElement() != ElementType.VOID)
                    methodBody.append("\tpop\n");
            }
            instrCurrStackSize = 0;
        }

        ArrayList<Integer> locals = new ArrayList<>();
        for (Descriptor d : method.getVarTable().values()) {
            if (!locals.contains(d.getVirtualReg()))
                locals.add(d.getVirtualReg());
        }
        if (!locals.contains(0) && !method.isConstructMethod())
            locals.add(0);

        res.append("\t.limit stack ").append(instrMaxStackSize).append("\n\t.limit locals ").append(locals.size()).append("\n").append(methodBody);
        return res.append(".end method\n\n").toString();
    }

    private String instructionToJasminString(Instruction instruction) {
        StringBuilder res = new StringBuilder("\t");

        switch (instruction.getInstType().toString()){
            case "ASSIGN":
                res.append(assignInstructionToJasminString((AssignInstruction) instruction));
                break;
            case "CALL":
                res.append(callInstructionToJasminString((CallInstruction) instruction));
                break;
            case "GOTO":
                res.append(gotoInstructionToJasminString((GotoInstruction) instruction));
                break;
            case "BRANCH":
                res.append(branchInstructionToJasminString((CondBranchInstruction) instruction));
                break;
            case "RETURN":
                res.append(returnInstructionToJasminString((ReturnInstruction) instruction));
                break;
            case "PUTFIELD":
                res.append(putFieldInstructionToJasminString((PutFieldInstruction) instruction));
                break;
            case "GETFIELD":
                res.append(getFieldInstructionToJasminString((GetFieldInstruction) instruction));
                break;
            case "UNARYOPER":
                res.append(unaryOpInstructionToJasminString((UnaryOpInstruction) instruction));
                break;
            case "BINARYOPER":
                res.append(binaryOpInstructionToJasminString((BinaryOpInstruction) instruction));
                break;
            case "NOPER":
                res.append(nOpInstructionToJasminString((SingleOpInstruction)instruction));
                break;
        }

        return res.append('\n').toString();
    }


    //TODO: refactor
    private String createLoadStr(Element elem) {
        instrCurrStackSize++;
        if (instrMaxStackSize < instrCurrStackSize)
            instrMaxStackSize = instrCurrStackSize;

        if (elem.isLiteral()) {
            int value = Integer.parseInt(((LiteralElement) elem).getLiteral());
            if (value == -1) return "iconst_m1";
            if (value >= 0 && value <= 5) return "iconst_" + value;
            if (value >= -128 && value <= 127) return "bipush " + value;
            if (value >= -32768 && value <= 32767) return "sipush " + value;
            return "ldc " + value;
        }

        // Optimization
        if (elem instanceof Operand && elem.getType().getTypeOfElement() == ElementType.BOOLEAN) {
            if (((Operand) elem).getName().equals("true"))
                return "iconst_1";
            if (((Operand) elem).getName().equals("false"))
                return "iconst_0"; // TODO: see if this works
        }

        Descriptor descriptor = (elem.getType().getTypeOfElement() == ElementType.THIS) ?
                currentMethod.getVarTable().get("this") :
                currentMethod.getVarTable().get(((Operand) elem).getName());

        if (descriptor.getScope().toString().equals("FIELD")){
            Type fieldType = descriptor.getVarType();
            GetFieldInstruction getField = new GetFieldInstruction(new Element(new Type(ElementType.THIS)), new Element(fieldType), fieldType);
            return instructionToJasminString(getField);
        }


        String type = descriptor.getVarType().getTypeOfElement().toString();
        int virtualReg = descriptor.getVirtualReg();

        return (type.equals("INT32") || type.equals("BOOLEAN") ? "i" : "a") + "load" + (virtualReg <= 3 ? "_" : " ") + (type.equals("THIS") ? "0" : virtualReg);
    }

    //TODO: refactor
    private String assignInstructionToJasminString(AssignInstruction assign){
        Instruction rhs = assign.getRhs();
        Element dest = assign.getDest(); // may cause errors? careful

        Descriptor descriptor = dest.getType().getTypeOfElement().toString().equals("THIS") ?
                currentMethod.getVarTable().get("this") :
                currentMethod.getVarTable().get(((Operand) dest).getName());

        /*
        // Optimization
        if (rhs.getInstType().toString().equalsIgnoreCase("BINARYOPER")) {
            BinaryOpInstruction binaryOpRhs = (BinaryOpInstruction) rhs;
            Element leftOperand = binaryOpRhs.getLeftOperand();
            Element rightOperand = binaryOpRhs.getRightOperand();

            String unaryOpTypeString = binaryOpRhs.getOperation().getOpType().toString();
            if (((Operand) leftOperand).getName().equals( ((Operand) dest).getName()) && (unaryOpTypeString.equals("ADD") || unaryOpTypeString.equals("SUB"))) {

                String literal = "";

                if (!leftOperand.isLiteral() && rightOperand.isLiteral())
                    literal = ((LiteralElement) rightOperand).getLiteral();

                else if (leftOperand.isLiteral() && !rightOperand.isLiteral())
                    literal = ((LiteralElement) binaryOpRhs.getLeftOperand()).getLiteral();

                if (!literal.equals(""))
                    return "\t\tiinc " + descriptor.getVirtualReg() + " " +
                            (binaryOpRhs.getOperation().getOpType().toString().equalsIgnoreCase("ADD") ? "" : "-") + literal + "\n";
            }
        }
        */

        String rhsInstr = instructionToJasminString(rhs);
        rhsInstr = rhsInstr.substring(1); // Removes initial /t

        if (dest instanceof ArrayOperand) { // TODO: refactor and test identation
            ArrayOperand destArray = (ArrayOperand) dest;
            return createLoadStr(destArray) + "\n\t" + createLoadStr(destArray.getIndexOperands().get(0)) + "\n\t" + rhsInstr + "\tiastore";
        }

        String type = descriptor.getVarType().getTypeOfElement().toString();

        return rhsInstr + "\t" + (type.equals("INT32") || type.equals("BOOLEAN") ? "i" : "a") + "store" +
                (descriptor.getVirtualReg() > 3 ? " " : "_") + (type.equals("THIS") ? "0" : descriptor.getVirtualReg());
    }

    //TODO: refactor
    private String generateMethodCallBody(CallInstruction call, Element element, LiteralElement method) {
        StringBuilder builder = new StringBuilder();

        switch (element.getType().getTypeOfElement()) {
            case THIS:
            case OBJECTREF:
                builder.append(((ClassType) element.getType()).getName());
                break;
            case CLASS:
                builder.append(((Operand) element).getName());
        }

        builder.append(".").append(method.getLiteral().replace("\"", "")).append("(");

        for (Element param : call.getListOfOperands())
            builder.append(typeToJasminString(param.getType()));

        return builder.append(")").append(typeToJasminString(call.getReturnType())).toString();
    }

    //TODO: refactor/do
    private String callInstructionToJasminString(CallInstruction call) {
        Element firstArg = call.getFirstArg();
        Element secondArg = call.getSecondArg();
        List<Element> operands = call.getListOfOperands();
        CallType invocationType = call.getInvocationType();

        StringBuilder builder = new StringBuilder();

        // invokevirtual, invokeinterface, invokespecial, invokestatic, NEW, arraylength, ldc;
        switch (invocationType) {
            case NEW:
                String typeFirst = firstArg.getType().getTypeOfElement().toString();

                if (typeFirst.equals("OBJECTREF"))
                    return builder.append("new ").append(((Operand)firstArg).getName()).toString();

                if (typeFirst.equals("ARRAYREF") && operands.size() > 0)
                    return builder.append(createLoadStr(operands.get(0))).append("\n\tnewarray int\n").toString();

                return "";

            case arraylength:
                return createLoadStr(firstArg) + "\n\tarraylength\n";

            case invokevirtual:
            case invokespecial:
                builder.append(createLoadStr(firstArg)).append("\n\t");
            case invokestatic:
                for (Element elem : call.getListOfOperands())
                    builder.append(createLoadStr(elem)).append("\n\t");
                return builder.append(invocationType).append(" ").append(generateMethodCallBody(call, firstArg, (LiteralElement) secondArg)).toString();

        }

        return "";
    }

    private String gotoInstructionToJasminString(GotoInstruction goTo) { //TODO: descomentar o generatePops
        return "goto " + goTo.getLabel();
    }

    private String branchInstructionToJasminString(CondBranchInstruction condBranch) {
        comparisons++;
        if (condBranch instanceof OpCondInstruction) {
            OpInstruction opInstruction = ((OpCondInstruction) condBranch).getCondition();
            Operation operation = opInstruction.getOperation();

            // (1/3) BinaryOp, if (a < b)
            if (opInstruction instanceof BinaryOpInstruction) {
                Element l = ((BinaryOpInstruction) opInstruction).getLeftOperand();
                Element r = ((BinaryOpInstruction) opInstruction).getRightOperand();

                if (operation.getOpType() == OperationType.ANDB) {
                    instrCurrStackSize = 0;

                    return  createLoadStr(l) + "\n\tifeq False" + comparisons + "\n" +
                            createLoadStr(r) + "\n\tifeq False" + comparisons + "\n\tgoto " + condBranch.getLabel()+ "\nFalse" + comparisons + ":";
                }

                StringBuilder jasminCode = new StringBuilder(createLoadStr(l)).append("\n\t").append(createLoadStr(r)).append("\n\t");
                switch (operation.getOpType()) {
                    //TODO: add more ops
                    case GTE:
                        jasminCode.append("if_icmpge ");
                        break;
                    case LTH:
                        jasminCode.append("if_icmplt ");
                        break;
                    case EQ:
                        jasminCode.append("if_icmpeq ");
                        break;
                    case NOTB:
                    case NEQ:
                        jasminCode.append("if_icmpne ");
                        break;
                    default:
                        System.out.println(operation.getOpType());
                        jasminCode.append("ERROR comparison not implemented yet");
                }
                instrCurrStackSize = 0;
                return jasminCode.append(condBranch.getLabel()).toString();
            }
            else {
                // (2/3) UnaryOp, if (!a)
                Element e = ((UnaryOpInstruction) opInstruction).getOperand();
                StringBuilder jasminCode = new StringBuilder(createLoadStr(e));
                switch (operation.getOpType()) {
                    case NOT:
                    case NOTB:
                        jasminCode.append("\n\tifne");
                    default:
                        System.out.println("This may lead to errors, careful"); //TODO: check if this is correct
                        jasminCode.append("\n\tifeq");
                }
                return jasminCode.append(" False").append(comparisons).append("\n\tgoto ").append(condBranch.getLabel()).append("\nFalse").append(comparisons).append(":").toString();
            }

        }

        // (3/3) SingleOpCond, if (a)
        Element e = ((SingleOpCondInstruction) condBranch).getCondition().getSingleOperand();
        return createLoadStr(e) + "\n\tifeq False" + comparisons + "\n\tgoto " + condBranch.getLabel()+ "\nFalse" + comparisons + ":";

    }

    private String returnInstructionToJasminString(ReturnInstruction returnInst) {
        if (!returnInst.hasReturnValue()) return "return";

        Element elem = returnInst.getOperand();
        StringBuilder builder = new StringBuilder(createLoadStr(elem));
        switch (elem.getType().getTypeOfElement().toString()) {
            case "INT32":
            case "BOOLEAN":
                builder.append("\n\tireturn");
                break;
            case "ARRAYREF":
                builder.append("\n\tareturn");
                break;
            default:
                builder.append("\n\treturn");
        }
        return builder.toString();
    }

    private String putFieldInstructionToJasminString(PutFieldInstruction putField) {
        Operand second = (Operand) putField.getSecondOperand();
        String loadFirstOperand = createLoadStr(putField.getFirstOperand()) + "\n\t";
        String loadThirdOperand = createLoadStr(putField.getThirdOperand()) + "\n\t";

        return loadFirstOperand + loadThirdOperand + "putfield " + className + "/" + second.getName() + " " + typeToJasminString(second.getType());
    }

    private String getFieldInstructionToJasminString(GetFieldInstruction getField) {
        Operand second = (Operand) getField.getSecondOperand();
        String loadOperand = createLoadStr(getField.getFirstOperand()) + "\n\t";

        return  loadOperand + "getfield " + className + "/" + second.getName() + " " + typeToJasminString(second.getType());
    }

    private String unaryOpInstructionToJasminString(UnaryOpInstruction unaryOp) {
        String loadElem = createLoadStr(unaryOp.getOperand());

        switch (unaryOp.getOperation().getOpType()) {
            //TODO: ask what SHRR instructions are
            case SHRR:
                return "";

            case NOT:
            case NOTB:
                comparisons++;
                return loadElem + "\n\tifne True" + comparisons + "\n\ticonst_1\n\tgoto Continue" + comparisons + "\nTrue" + comparisons + ":\n\ticonst_0\nContinue" + comparisons + ":";

            default:
                return "";
        }
    }

    private String binaryOpInstructionToJasminString(BinaryOpInstruction binaryOp) {
        String leftElemLoad = createLoadStr(binaryOp.getLeftOperand()) + "\n\t";
        String rightElemLoad = createLoadStr(binaryOp.getRightOperand()) + "\n\t";

        StringBuilder builder = new StringBuilder();
        String labelTrue = "LABEL_" + labelOperCounter++;
        String labelContinue = "LABEL_" + labelOperCounter++;
        OperationType op = binaryOp.getOperation().getOpType();

        switch (op) {
            case AND: // Logical And
            case ANDB:
                return leftElemLoad + rightElemLoad + "iand";

            case OR: // Logical Or
            case ORB:
                return leftElemLoad + rightElemLoad + "ior";

            case XOR: // Logical Exclusive Or
                return leftElemLoad + rightElemLoad + "ixor";

            case ADD: // Arithmetic Addition
                return leftElemLoad + rightElemLoad + "iadd";

            case SUB: // Arithmetic Subtraction
                return leftElemLoad + rightElemLoad + "isub";

            case MUL: // Arithmetic Multiplication
                return leftElemLoad + rightElemLoad + "imul";

            case DIV: // Arithmetic Division
                return leftElemLoad + rightElemLoad + "idiv";

            case SHR: // Logical Shift Right
                return leftElemLoad + rightElemLoad + "ishr";

            case SHL: // Logical Shift Left
                return leftElemLoad + rightElemLoad + "ishl";
        }

        builder.append(leftElemLoad).append(rightElemLoad).append("\t\t");

        switch (op) {
            case EQ: // Equals (negation of if_icmpeq)
                builder.append("if_icmpeq ");
                break;

            case NEQ: // Not equals (negation of if_icmpne)
                builder.append("if_icmpne ");
                break;

            case LTH: // Less Than (negation of if_icmplt)
                builder.append("if_icmplt ");
                break;

            case LTE: // Less Than or Equal (negação de if_icmple)
                builder.append("if_icmple ");
                break;

            case GTH: // Greater Than (negação de if_icmpgt)
                builder.append("if_icmpgt ");
                break;

            case GTE: // Greater Than or Equal (negação de if_icmpge)
                builder.append("if_icmpge ");
                break;

        }

        return builder.append(labelTrue).append("\n\t\ticonst_0\n").append("\t\tgoto ").append(labelContinue).append("\n\t").append(labelTrue).append(":\n").append("\t\ticonst_1\n\t").append(labelContinue).append(":").toString();
    }

    private String nOpInstructionToJasminString(SingleOpInstruction singleOp) {
        if (singleOp.getSingleOperand() instanceof ArrayOperand) {
            Element index = ((ArrayOperand)singleOp.getSingleOperand()).getIndexOperands().get(0);
            return createLoadStr(singleOp.getSingleOperand()) + "\n\t" + createLoadStr(index) + "\n\tiaload";
        }
        return createLoadStr(singleOp.getSingleOperand());
    }

    private String typeToJasminString(Type type){
        // TODO: check THIS
        // INT32, BOOLEAN, ARRAYREF, OBJECTREF, CLASS, THIS, STRING, VOID;
        switch(type.getTypeOfElement().toString()) {
            case "ARRAYREF":
                ArrayType arrayType = (ArrayType) type;
                return "[".repeat(arrayType.getNumDimensions()) + typeToJasminString(arrayType.getArrayType());
            case "THIS":
                return ""; // ?
            case "OBJECTREF":
            case "CLASS":
                return "L"+((ClassType) type).getName()+";";
            default:
                return typeToJasminString(type.getTypeOfElement());
        }
    }

    private String typeToJasminString(ElementType elementType){
        switch (elementType.toString()) {
            case "INT32": return "I";
            case "BOOLEAN": return "Z";
            case "STRING": return "Ljava/lang/String;";
            case "VOID": return "V";
            default: return "";
        }
    }

    public String getFullyQualifiedName(ClassUnit classUnit) {
        String className = classUnit.getSuperClass();

        for (String importString : classUnit.getImports()){
            String[] splittedImport = importString.split("\\.");

            String lastName = (splittedImport.length == 0)? importString : splittedImport[splittedImport.length - 1];

            if (lastName.equals(className)) return importString.replace('.', '/');
        }

        throw new RuntimeException("Could not find import for class " + className);
    }

    private String accessModifierToJasminString(AccessModifiers accessModifier) {
        if (accessModifier.toString().equalsIgnoreCase("default")) return "public";
        return accessModifier.toString().toLowerCase();
    }

}
