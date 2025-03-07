import java.util.HashMap;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FuncManager {
    private static HashMap<String, HashMap<Integer, String>> functions = new HashMap<>(); // 储存函数定义
    private static HashMap<String, ArrayList<String>> parameters = new HashMap<>(); // 储存函数形参列表
    private static HashMap<String, String> recurrences = new HashMap<>(); // 储存递推关系

    public static void FuncDefinition(String input) {
        // 将输入中的 x 替换为 u，y 替换为 v，防止冲突 .replaceAll("x", "u").replaceAll("y", "v")
        String newInput = input;
        
        // 正则表达式匹配初始定义和递推定义
        Pattern initialPattern = 
            Pattern.compile("([a-zA-Z]+)\\{(\\d)\\}\\s*\\(([^)]+)\\)\\s*=\\s*(.*)");
        Pattern recurrencePattern = 
            Pattern.compile("([a-zA-Z]+)\\{(n)\\}\\s*\\(([^)]+)\\)\\s*=\\s*(.*)");
        Matcher initialMatcher = initialPattern.matcher(newInput);
        Matcher recurrenceMatcher = recurrencePattern.matcher(newInput);
        String funcName = new String();
        String params = new String();
        String expr = new String(); 
        ArrayList<String> factors = new ArrayList<>();
        if (initialMatcher.matches()) { // 判断是初始定义还是递推定义
            funcName = initialMatcher.group(1); // 函数名
            final BigInteger seq = new BigInteger(initialMatcher.group(2)); // 初始定义内容
            params = initialMatcher.group(3); // 形参
            expr = initialMatcher.group(4); // 表达式
            // 处理初始定义
            functions.putIfAbsent(funcName, new HashMap<Integer, String>()); 
            parameters.putIfAbsent(funcName, new ArrayList<String>());
            factors.addAll(Arrays.asList(params.split("\\s*,\\s*"))); // 真正的参数
            // 储存初始定义
            functions.get(funcName).put(seq.intValue(), 
                expr.replaceAll("x", "u").replaceAll("y", "v"));
            if (parameters.get(funcName).isEmpty()) {
                for (String param : factors) {
                    param = param.replaceAll("x", "u").replaceAll("y", "v");
                    parameters.get(funcName).add(param);
                }
            }
        } else if (recurrenceMatcher.matches()) {
            funcName = recurrenceMatcher.group(1); // 函数名
            params = recurrenceMatcher.group(3); // 形参
            expr = recurrenceMatcher.group(4); // 表达式
            // 处理递推定义
            recurrences.put(funcName, expr.replaceAll("x", "u").replaceAll("y", "v"));
            parameters.putIfAbsent(funcName, new ArrayList<String>());
            factors.addAll(Arrays.asList(params.split("\\s*,\\s*"))); // 真正的参数
            // 储存参数
            if (parameters.get(funcName).isEmpty()) {
                for (String param : factors) {
                    param = param.replaceAll("x", "u").replaceAll("y", "v");
                    parameters.get(funcName).add(param);
                }
            }
        } else {
            throw new IllegalArgumentException("Invalid function definition: " + newInput);
        }
        // 如果初始定义和递推定义都存在，则生成0~5的展开表达式
        if (functions.get(funcName).size() == 2 && recurrences.containsKey(funcName)) {
            for (int i = 2; i <= 5; i++) {
                expandFunction(funcName, i, factors);
            }
        }
    }

    // 展开递推函数
    private static String expandFunction(String funcName, int seq, ArrayList<String> factors) { 
        if (seq < 0 || seq > 5) {
            throw new IllegalArgumentException("Invalid sequence number: " + seq);
        }    
        if (functions.get(funcName).containsKey(seq)) { // 如果已经存在展开的表达式，替换实参后返回
            return replaceParams(funcName, functions.get(funcName).get(seq), factors);
        }
        if (seq == 0 || seq == 1) { // 如果是初始定义，替换实参后直接返回
            return replaceParams(funcName, functions.get(funcName).get(seq), factors);
        }
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
            String paramsStr = recurrenceExpr.substring(startParen + 1, endParen); // 提取括号内的参数字符串
            ArrayList<String> factors1 = new ArrayList<>(splitParameters(paramsStr)); // 利用栈分割参数
            String expanded1 = expandFunction(funcName, seq - 1, factors1); // 递归展开 f{n-1} 调用
            if (!isAtomic(expanded1)) {
                expanded1 = "(" + expanded1 + ")";
            }
            String toReplace = recurrenceExpr.substring(index1, endParen + 1); // 将整个f{n-1}(...)替换
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
        functions.get(funcName).put(seq, recurrenceExpr);
        recurrenceExpr = replaceParams(funcName, recurrenceExpr, factors); // 返回前再替换实参
        return recurrenceExpr;
    }

    public static String callFunc(String name, BigInteger seq, ArrayList<String> factors) {
        // 调用展开后的函数
        if (!functions.containsKey(name)) {
            throw new IllegalArgumentException("Function " + name + " is not defined.");
        } else if (seq.compareTo(BigInteger.ZERO) < 0 || seq.compareTo(BigInteger.valueOf(5)) > 0) {
            throw new IllegalArgumentException("Invalid sequence number: " + seq);
        } else if (!functions.get(name).containsKey(seq.intValue())) {
            throw new IllegalArgumentException("Function " + name + " is not defined for " + seq);
        } else {
            return replaceParams(name, functions.get(name).get(seq.intValue()), factors);
        }
    }

    private static String replaceParams(String funcName, String expr, ArrayList<String> factors) {
        // 替换形参为实参
        ArrayList<String> params = parameters.get(funcName);
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