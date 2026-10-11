// Defaults shared by standalone LSP clients and the VS Code extension.
// Keep these aligned with package.json's contributes.configuration.properties.
export const defaultClientSettings: any = {
  "projectCodeGeneration": {
    "enable": false,
    "generators": []
  },
  "diagnosticsForUnrealNamingConvention": true,
  "completion": {
    "dependencyRestrictions": []
  },
  "mathCompletionShortcuts": true,
  "inlayHints": {
    "typeHintsForAutos": true,
    "parameterHintsForSingleParameterFunctions": false,
    "parameterHintsIgnoredFunctionNames": [],
    "inlayHintsEnabled": true,
    "parameterHintsIgnoredParameterNames": [
      "Object",
      "Actor",
      "FunctionName",
      "Value",
      "InValue",
      "NewValue",
      "Condition",
      "Parameters",
      "Params"
    ],
    "typeHintsForAutoIgnoredTypes": [],
    "parameterReferenceHints": true,
    "parameterHintsForComplexExpressions": true,
    "parameterHintsForConstants": true
  },
  "unrealConnectionHost": "127.0.0.1",
  "correctFloatLiteralsWhenExpectingDoublePrecision": false,
  "unrealConnectionPort": 27099,
  "insertParenthesisOnFunctionCompletion": false,
  "codeLenses": {
    "showCreateBlueprintClasses": [
      "AActor",
      "UUserWidget"
    ]
  },
  "dataBreakpoints": {
    "asBreakpoints": {
      "triggerCount": -1
    },
    "cppBreakpoints": {
      "enable": false,
      "triggerCount": 1
    }
  },
  "scriptIgnorePatterns": [
    "**/Saved/**",
    "**/.plastic/**"
  ],
  "markUnreadVariablesAsUnused": false,
  "inlineValues": {
    "showInlineValueForLocalVariables": true,
    "showInlineValueForParameters": true,
    "showInlineValueForMemberAssignment": true,
    "showInlineValueForFunctionThisObject": true
  }
};

export function mergeClientSettings(current: any, overrides: any): any {
    const merge = (base: any, extra: any): any => {
        const result = { ...base };
        if (!extra || typeof extra !== 'object' || Array.isArray(extra)) return result;
        for (const [key, value] of Object.entries(extra)) {
            if (key === '__proto__' || key === 'constructor' || key === 'prototype') continue;
            result[key] = value && typeof value === 'object' && !Array.isArray(value)
                ? merge(base?.[key], value) : value;
        }
        return result;
    };
    return merge(merge(defaultClientSettings, current), overrides);
}
