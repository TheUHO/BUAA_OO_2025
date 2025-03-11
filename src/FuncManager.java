import java.util.HashMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FuncManager {
    private static HashMap<String, String> functions = new HashMap<>(); // 储存函数定义：f{0}, f{1}, g, h
    private static HashMap<String, ArrayList<String>> parameters = new HashMap<>(); // 储存函数形参列表 f, g
    private static HashMap<String, String> recurrences = new HashMap<>(); // 储存递推关系 f

    public static void FuncDefinition(String input) {
        // 将输入中的 x 替换为 u，y 替换为 v，防止冲突 .replaceAll("x", "u").replaceAll("y", "v")
        String newInput = input;
        // 正则表达式匹配初始定义和递推定义
        Pattern initialPattern = 
            Pattern.compile("([f])\\{(\\d)\\}\\s*\\(([^)]+)\\)\\s*=\\s*(.*)");
        Pattern recurrencePattern = 
            Pattern.compile("([f])\\{(n)\\}\\s*\\(([^)]+)\\)\\s*=\\s*(.*)");
        Pattern normalPattern = Pattern.compile("([gh])\\s*\\(([^)]+)\\)\\s*=\\s*(.*)");
        Matcher initialMatcher = initialPattern.matcher(newInput);
        Matcher recurrenceMatcher = recurrencePattern.matcher(newInput);
        Matcher normalMatcher = normalPattern.matcher(newInput);
        String funcName = new String();
        String params = new String();
        String expr = new String(); 
        ArrayList<String> factors = new ArrayList<>();
        if (normalMatcher.matches()) { // 处理普通函数定义
            funcName = normalMatcher.group(1); // 函数名
            params = normalMatcher.group(2); // 形参
            expr = normalMatcher.group(3); // 表达式
            parameters.putIfAbsent(funcName, new ArrayList<String>());
            factors.addAll(Arrays.asList(params.split("\\s*,\\s*"))); // 真正的参数
            functions.put(funcName, expr.replaceAll("x", "u").replaceAll("y", "v")); // 储存初始定义
            for (String param : factors) { // 储存参数
                param = param.replaceAll("x", "u").replaceAll("y", "v");
                parameters.get(funcName).add(param);
            }
            expr = expandNormalFunction(funcName, factors); // 展开普通函数
            functions.put(funcName, expr.replaceAll("x", "u").replaceAll("y", "v")); // 保存展开结果
        } else if (initialMatcher.matches()) { // 处理初始定义
            funcName = "f" + "{" + initialMatcher.group(2) + "}"; // 函数名
            params = initialMatcher.group(3); // 形参
            expr = initialMatcher.group(4); // 表达式
            factors.addAll(Arrays.asList(params.split("\\s*,\\s*"))); // 真正的参数
            functions.put(funcName, expr.replaceAll("x", "u").replaceAll("y", "v")); // 储存初始定
            if (!parameters.containsKey("f")) { // 储存参数
                parameters.put("f", new ArrayList<String>());
                for (String param : factors) { // 储存参数
                    param = param.replaceAll("x", "u").replaceAll("y", "v");
                    parameters.get("f").add(param);
                }
            }
        } else if (recurrenceMatcher.matches()) { //处理递推定义
            funcName = "f"; // 递推表达式中的函数名
            params = recurrenceMatcher.group(3); // 形参
            expr = recurrenceMatcher.group(4); // 表达式
            recurrences.put(funcName, expr.replaceAll("x", "u").replaceAll("y", "v"));
            factors.addAll(Arrays.asList(params.split("\\s*,\\s*"))); // 真正的参数
        } else {
            throw new IllegalArgumentException("Invalid function definition: " + newInput);
        }
        if (functions.containsKey("f{0}") && functions.containsKey("f{1}") 
            && recurrences.containsKey("f")) { // 如果初始定义和递推定义都存在，则生成0~5的展开表达式
            for (int i = 2; i <= 5; i++) {
                expandFunction("f", i, factors);
            }
        }
    }

    // 展开递推函数
    private static String expandFunction(String funcName, int seq, ArrayList<String> factors) { 
        if (seq < 0 || seq > 5) {
            throw new IllegalArgumentException("Invalid sequence number: " + seq);
        } else if (functions.containsKey(funcName + "{" + seq + "}")) { // 如果已经存在展开的表达式，直接返回替换实参后返回
            return replaceParams(funcName, functions.get(funcName + "{" + seq + "}"), factors);
        } else if (seq == 0 || seq == 1) { // 如果是初始定义，替换实参后直接返回
            return replaceParams(funcName, functions.get(funcName + "{" + seq + "}"), factors);
        } else {
            // 获取递推表达式（注意：这里先将 u,v 转换回 x,y 供处理）
            String recurrenceExpr = recurrences.get(funcName);
            recurrenceExpr = recurrenceExpr.replaceAll("u", "x").replaceAll("v", "y");
            // 分别处理 f{n-1}(...) 和 f{n-2}(...)
            String callId1 = funcName + "{n-1}("; // 使用 funcName + "{n-1}(" 来定位调用位置
            int index1 = recurrenceExpr.indexOf(callId1);
            while (index1 != -1) { // 找到左括号的位置（callId1 后面紧跟的左括号）
                int startParen = index1 + callId1.length() - 1; // 此处应指向'('
                int endParen = findClosingParen(recurrenceExpr, startParen);
                if (endParen == -1) {
                    throw new IllegalArgumentException("Unmatched parenthesis.");
                }
                String paramsStr = recurrenceExpr.substring(startParen + 1, endParen); // 提取括号内参数字符串
                ArrayList<String> factors1 = new ArrayList<>(splitParameters(paramsStr)); // 利用栈分割参数
                String expanded1 = expandFunction(funcName, seq - 1, factors1); // 递归展开 f{n-1} 调用
                if (!isAtomic(expanded1)) {
                    expanded1 = "(" + expanded1 + ")";
                }
                String toReplace = recurrenceExpr.substring(index1, endParen + 1); // 将f{n-1}(...)替换
                recurrenceExpr = recurrenceExpr.replace(toReplace, expanded1);
                index1 = recurrenceExpr.indexOf(callId1); // 继续查找下一个 f{n-1} 调用
            }
            String callId2 = funcName + "{n-2}("; // 处理 f{n-2}(...) 调用
            int index2 = recurrenceExpr.indexOf(callId2);
            while (index2 != -1) {
                int startParen = index2 + callId2.length() - 1;
                int endParen = findClosingParen(recurrenceExpr, startParen);
                if (endParen == -1) {
                    throw new IllegalArgumentException("Unmatched parenthesis.");
                }
                String paramsStr = recurrenceExpr.substring(startParen + 1, endParen);
                ArrayList<String> factors2 = new ArrayList<>(splitParameters(paramsStr));
                String expanded2 = expandFunction(funcName, seq - 2, factors2);
                if (!isAtomic(expanded2)) {
                    expanded2 = "(" + expanded2 + ")";
                }
                String toReplace = recurrenceExpr.substring(index2, endParen + 1);
                recurrenceExpr = recurrenceExpr.replace(toReplace, expanded2);
                index2 = recurrenceExpr.indexOf(callId2);
            }
            // 将处理后的表达式中 x,y 替换回 u,v，保存展开结果
            recurrenceExpr = recurrenceExpr.replaceAll("x", "u").replaceAll("y", "v");
            functions.put(funcName + "{" + seq + "}", recurrenceExpr);
            recurrenceExpr = replaceParams(funcName, recurrenceExpr, factors); // 返回前再替换实参
            return recurrenceExpr;
        }
    }

    // 展开普通函数
    private static String expandNormalFunction(String funcName, ArrayList<String> factors) {
        String expr = replaceParams(funcName, functions.get(funcName), factors); // 获取当前函数表达式
        // 用于匹配嵌套的普通函数调用（g 或 h）
        Pattern nestedCallPattern = Pattern.compile("([gh])\\s*\\(");
        Matcher matcher = nestedCallPattern.matcher(expr);
        while (matcher.find()) { // 只要存在嵌套就逐个展开
            String nestedFunc = matcher.group(1);
            int startIndex = matcher.start();
            int openParenIndex = expr.indexOf("(", startIndex);
            int closeParenIndex = findClosingParen(expr, openParenIndex);
            if (closeParenIndex == -1) {
                throw new IllegalArgumentException("Unmatched parenthesis in nested call: " + expr);
            }
            // 提取嵌套调用中的参数字符串，并分割成各个实参
            String argsStr = expr.substring(openParenIndex + 1, closeParenIndex);
            ArrayList<String> argFactors = splitParameters(argsStr);
            String expandedNested = expandNormalFunction(nestedFunc, argFactors); // 递归展开被调用的普通函数
            if (!isAtomic(expandedNested)) { // 如果展开结果不是原子表达式，加上括号
                expandedNested = "(" + expandedNested + ")";
            }
            // 将整个嵌套调用替换为展开后的结果
            String toReplace = expr.substring(startIndex, closeParenIndex + 1);
            expr = expr.replace(toReplace, expandedNested);
            // expr已修改，重新构造matcher
            matcher = nestedCallPattern.matcher(expr);
        }
        return expr;
    }

    public static String callFunc(String name, ArrayList<String> factors) {
        // 调用展开后的函数
        if (!functions.containsKey(name)) {
            throw new IllegalArgumentException("Function " + name + " is not defined.");
        } else {
            return replaceParams(name, functions.get(name), factors);
        }
    }

    private static String replaceParams(String funcName, String expr, ArrayList<String> factors) {
        // 替换形参为实参
        String paramfuncName = String.valueOf(funcName.charAt(0));
        ArrayList<String> params = parameters.get(paramfuncName);
        if (params == null || params.size() != factors.size()) {
            throw new IllegalArgumentException("Invalid number of arguments for " + funcName);
        }
        String newExpr = new String();
        newExpr = expr;
        for (int i = 0; i < params.size(); i++) {
            String paramName = params.get(i);
            String factor = factors.get(i);
            newExpr = newExpr.replaceAll(paramName, "(" + factor + ")");
        }

        return newExpr;
    }

    private static int findClosingParen(String s, int startIndex) {
        int count = 0;
        for (int i = startIndex; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '(') {
                count++;
            } else if (c == ')') {
                count--;
                if (count == 0) {
                    return i;
                }
            }
        }
        return -1; // 没有匹配
    }

    private static ArrayList<String> splitParameters(String s) {
        ArrayList<String> params = new ArrayList<>();
        int count = 0;
        StringBuilder current = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == ',' && count == 0) {
                params.add(current.toString().trim());
                current.setLength(0);
            } else {
                if (c == '(') {
                    count++;
                } else if (c == ')') {
                    count--;
                }
                current.append(c);
            }
        }
        if (current.length() > 0) {
            params.add(current.toString().trim());
        }
        return params;
    }

    private static boolean isAtomic(String s) {
        return s.matches("\\d+") || s.matches("[a-zA-Z]+");
    }
}