package bsise.server.common.hibernate;

import static org.hibernate.type.StandardBasicTypes.BOOLEAN;

import org.hibernate.boot.model.FunctionContributions;
import org.hibernate.boot.model.FunctionContributor;

public class FulltextMatchNaturalLanguageModeFunctionContributor implements FunctionContributor {

    private static final String MATCH_AGAINST_FUNCTION_NAME = "match_against_natural_language_mode";
    private static final String PATTERN = "match(?1) against(?2 in natural language mode) ";

    @Override
    public void contributeFunctions(FunctionContributions functionContributions) {
        functionContributions.getFunctionRegistry()
                .registerPattern(MATCH_AGAINST_FUNCTION_NAME, PATTERN,
                        functionContributions.getTypeConfiguration().getBasicTypeRegistry().resolve(BOOLEAN));
    }
}
