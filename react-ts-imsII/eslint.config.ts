import globals from "globals";
import eslintConfigAirbnb from "eslint-config-airbnb";
import eslintConfigAirbnbTypeScript from "eslint-config-airbnb-typescript";
import * as tseslint from "typescript-eslint";
import reactPlugin from "eslint-plugin-react";
import reactHooksPlugin from "eslint-plugin-react-hooks";
import importPlugin from "eslint-plugin-import";
import { Linter } from "eslint";

// Định nghĩa type cho cấu hình
type FlatConfigItem = {
  files: string[];
  languageOptions: {
    globals: Record<string, boolean>;
    parser: typeof tseslint.parser;
    parserOptions: {
      project: string[];
      tsconfigRootDir: string;
      ecmaVersion: "latest";
      sourceType: "module";
      ecmaFeatures: {
        jsx: boolean;
      };
    };
  };
  plugins: {
    "@typescript-eslint": typeof tseslint.plugin;
    react: typeof reactPlugin;
    "react-hooks": typeof reactHooksPlugin;
    import: typeof importPlugin;
  };
  rules: Linter.RulesRecord;
  settings: {
    react: {
      version: string;
    };
  };
};

const config: FlatConfigItem[] = [
  {
    files: ["**/*.{js,jsx,ts,tsx}"],
    languageOptions: {
      globals: {
        ...globals.browser,
        ...globals.node
      },
      parser: tseslint.parser,
      parserOptions: {
        project: ["./tsconfig.app.json", "./tsconfig.node.json"],
        tsconfigRootDir: process.cwd(),
        ecmaVersion: "latest",
        sourceType: "module",
        ecmaFeatures: {
          jsx: true
        }
      }
    },
    plugins: {
      "@typescript-eslint": tseslint.plugin,
      react: reactPlugin,
      "react-hooks": reactHooksPlugin,
      import: importPlugin
    },
    rules: {
      // Airbnb rules
      ...eslintConfigAirbnb.rules,
      ...eslintConfigAirbnbTypeScript.rules,

      // React rules
      "react/react-in-jsx-scope": "off",
      "react/jsx-filename-extension": [
        "warn",
        { extensions: [".jsx", ".tsx"] }
      ],

      // TypeScript rules
      "@typescript-eslint/explicit-function-return-type": "off",
      "@typescript-eslint/explicit-module-boundary-types": "off",

      // React Hooks rules
      "react-hooks/rules-of-hooks": "error",
      "react-hooks/exhaustive-deps": "warn"
    },
    settings: {
      react: {
        version: "detect"
      }
    }
  }
];

export default config;