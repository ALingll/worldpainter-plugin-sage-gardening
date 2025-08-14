package org.cti.wpplugin.utils.debug;

/**
 * @author: ALingll
 * @desc:
 * @create: 2025-08-15 03:49
 **/
public class DebugUtils {

    public static void sayCalled(){sayCalled(3);}
    public static void sayCalled(int depth){
        StackTraceElement[] stack = Thread.currentThread().getStackTrace();

        // stack[0] = getStackTrace
        // stack[1] = printCallStack
        // stack[2] = 调用 printCallStack 的方法（顶层）
        int start = 3;
        int end = Math.min(stack.length, start + depth);

        StringBuilder sb = new StringBuilder();
        for (int i = start; i < end; i++) {
            if (i > start) {
                sb.append(" <- ");
            }
            String fullClassName = stack[i].getClassName();
            String simpleClassName = fullClassName.substring(fullClassName.lastIndexOf('.') + 1);
            sb.append(simpleClassName)
                    .append(".")
                    .append(stack[i].getMethodName());
        }

        System.out.println(sb.toString());
    }
}
