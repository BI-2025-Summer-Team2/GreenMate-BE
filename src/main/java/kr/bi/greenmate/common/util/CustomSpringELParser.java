package kr.bi.greenmate.common.util;

import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

public class CustomSpringELParser {
    private CustomSpringELParser() {
    }

    public static String[] getDynamicValue(String[] parameterNames, Object[] args, String[] keys) {
        ExpressionParser parser = new SpelExpressionParser();
        StandardEvaluationContext context = new StandardEvaluationContext();

        for (int i = 0; i < parameterNames.length; i++) {
            context.setVariable(parameterNames[i], args[i]);
        }

        String[] dynamicKeys = new String[keys.length];
        for(int i=0; i<keys.length; i++){
            dynamicKeys[i] = parser.parseExpression(keys[i]).getValue(context, String.class);
        }

        return dynamicKeys;
    }
}
