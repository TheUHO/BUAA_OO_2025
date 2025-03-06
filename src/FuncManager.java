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

    private static String expandFunction(String funcName, int seq, ArrayList<String> factors) {
        // 展开递推函数
        if (seq < 0 || seq > 5) {
            throw new IllegalArgumentException("Invalid sequence number: " + seq);
        }
        // 如果已经存在展开的表达式，替换实参后返回
        if (functions.get(funcName).containsKey(seq)) {
            return replaceParams(funcName, functions.get(funcName).get(seq), factors);
        }
        // 如果是初始定义，替换实参后直接返回
        if (seq == 0 || seq == 1) {
            return replaceParams(funcName, functions.get(funcName).get(seq), factors);
        }
        // 获取递推表达式
        String recurrenceExpr = recurrences.get(funcName);
        recurrenceExpr = recurrenceExpr.replaceAll("u", "x").replaceAll("v", "y");
        // 处理递推表达式n-1
        Pattern pattern1 = Pattern
            .compile("[a-zA-Z]+\\{n-1\\}\\(((?:[^()]+|\\((?:[^()]+|\\([^()]*\\))*\\))*)\\)");

        Matcher matcher1 = pattern1.matcher(recurrenceExpr);
        if (matcher1.find()) {
            ArrayList<String> factors1 = new ArrayList<>();
            for (String param : matcher1.group(1).split("\\s*,\\s*")) {
                factors1.add(param);
            }
            String expanded1 = expandFunction(funcName, seq - 1, factors1);
            recurrenceExpr = recurrenceExpr
            .replaceAll("[a-zA-Z]+\\{n-1\\}\\(((?:[^()]+|\\((?:[^()]+|\\([^()]*\\))*\\))*)\\)",
             "(" + expanded1 + ")");
        }

        // 处理n-2
        Pattern pattern2 = Pattern
            .compile("[a-zA-Z]+\\{n-2\\}\\(((?:[^()]+|\\((?:[^()]+|\\([^()]*\\))*\\))*)\\)");

        Matcher matcher2 = pattern2.matcher(recurrenceExpr);
        if (matcher2.find()) {
            ArrayList<String> factors2 = new ArrayList<>();
            for (String param : matcher2.group(1).split("\\s*,\\s*")) {
                factors2.add(param);
            }
            String expanded2 = expandFunction(funcName, seq - 2, factors2);
            recurrenceExpr = recurrenceExpr
            .replaceAll("[a-zA-Z]+\\{n-2\\}\\(((?:[^()]+|\\((?:[^()]+|\\([^()]*\\))*\\))*)\\)",
             "(" + expanded2 + ")");
        }
        // 替换x和y便于后续调用
        recurrenceExpr = recurrenceExpr.replaceAll("x", "u").replaceAll("y", "v");
        // 保存展开结果，便于后续调用
        functions.get(funcName).put(seq, recurrenceExpr);
        // 替换实参后返回
        recurrenceExpr = replaceParams(funcName, recurrenceExpr, factors);
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
}
