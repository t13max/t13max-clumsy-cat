package com.t13max.cc.enhance;

import javassist.*;

import java.lang.reflect.Method;


/**
 * set方法增强器
 * 增加失败回滚逻辑
 *
 * @Author t13max
 * @Date 16:59 2025/8/16
 */
public class SetterEnhancer {

    public static Class<?> enhance(String className) throws Exception {
        ClassPool pool = ClassPool.getDefault();
        CtClass ctClass = pool.get(className);

        // 给所有 set 方法增强
        for (CtMethod method : ctClass.getDeclaredMethods()) {
            if (method.getName().startsWith("set") && method.getParameterTypes().length == 1) {
                String fieldName = Character.toLowerCase(method.getName().charAt(3)) + method.getName().substring(4);
                String code = "{ " +
                        "  if(this._oldValueMap == null) this._oldValueMap = new java.util.HashMap();" +
                        "  if(!this._oldValueMap.containsKey(\"" + fieldName + "\")) {" +
                        "    this._oldValueMap.put(\"" + fieldName + "\", ($w)this." + fieldName + ");" +
                        "    this.update();" +
                        "  }" +
                        "  this." + fieldName + " = $1;" +
                        "}";

                method.setBody(code);
            }
        }

        // rollback 方法
        // 遍历字段生成 rollback 代码
        StringBuilder rollbackCode = new StringBuilder();
        rollbackCode.append("public void rollback() {");
        rollbackCode.append("if(_oldValueMap != null) {");

        for (CtField field : ctClass.getDeclaredFields()) {
            String name = field.getName();
            String type = field.getType().getName();

            // 跳过 _oldValueMap 字段自身
            if (name.equals("_oldValueMap")) continue;

            String assignCode = getAssignCode(type, name);

            rollbackCode.append("if(_oldValueMap.containsKey(\"").append(name).append("\")) {")
                    .append(assignCode)
                    .append("}");
        }

        rollbackCode.append("_oldValueMap = null;");
        rollbackCode.append("this.clear();");
        rollbackCode.append("}");
        rollbackCode.append("}");

        // 添加方法
        ctClass.addMethod(CtNewMethod.make(rollbackCode.toString(), ctClass));

        // commit 方法
        String commitCode =
                "public void commit() {"
                        + "this._oldValueMap = null;"
                        + "}";
        ctClass.addMethod(CtNewMethod.make(commitCode, ctClass));

        return ctClass.toClass();
    }

    private static String getAssignCode(String type, String name) {
        String assignCode = switch (type) {
            case "int" -> name + " = ((Integer)_oldValueMap.get(\"" + name + "\")).intValue();";
            case "long" -> name + " = ((Long)_oldValueMap.get(\"" + name + "\")).longValue();";
            case "short" -> name + " = ((Short)_oldValueMap.get(\"" + name + "\")).shortValue();";
            case "byte" -> name + " = ((Byte)_oldValueMap.get(\"" + name + "\")).byteValue();";
            case "float" -> name + " = ((Float)_oldValueMap.get(\"" + name + "\")).floatValue();";
            case "double" -> name + " = ((Double)_oldValueMap.get(\"" + name + "\")).doubleValue();";
            case "boolean" -> name + " = ((Boolean)_oldValueMap.get(\"" + name + "\")).booleanValue();";
            case "char" -> name + " = ((Character)_oldValueMap.get(\"" + name + "\")).charValue();";
            default -> name + " = (" + type + ")_oldValueMap.get(\"" + name + "\");";
        };
        return assignCode;
    }

    // 测试用例
    public static void main(String[] args) throws Exception {
        Class<?> enhanced = enhance("com.example.MemberData");
        Object obj = enhanced.getDeclaredConstructor().newInstance();

        Method setName = enhanced.getMethod("setName", String.class);
        Method getName = enhanced.getMethod("getName");

        setName.invoke(obj, "Alice");
        System.out.println("Name = " + getName.invoke(obj));

        Method rollback = enhanced.getMethod("rollback");
        Method commit = enhanced.getMethod("commit");

        setName.invoke(obj, "Bob");
        System.out.println("修改后 Name = " + getName.invoke(obj));

        rollback.invoke(obj);
        System.out.println("rollback 后 Name = " + getName.invoke(obj));

        commit.invoke(obj);
    }
}