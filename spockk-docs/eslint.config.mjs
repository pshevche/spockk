import eslint from '@eslint/js'
import eslintConfigPrettier from 'eslint-config-prettier'
import eslintPluginVue from 'eslint-plugin-vue'
import headerPlugin from 'eslint-plugin-yet-another-license-header'
import globals from 'globals'
import typescriptEslint from 'typescript-eslint'

export default typescriptEslint.config(
  {
    ignores: ['node_modules/**', 'docs/.vitepress/cache/**', 'docs/.vitepress/dist/**', 'build/**'],
  },
  {
    extends: [
      eslint.configs.recommended,
      ...typescriptEslint.configs.recommended,
      ...eslintPluginVue.configs['flat/recommended'],
    ],
    files: ['docs/.vitepress/**/*.{ts,mts,vue}'],
    languageOptions: {
      ecmaVersion: 'latest',
      sourceType: 'module',
      globals: globals.browser,
      parserOptions: {
        parser: typescriptEslint.parser,
      },
    },
  },
  {
    // CSS isn't JS/TS, so the license-header rule (which walks a JS AST) can't
    // check docs/.vitepress/theme/style.css; its header is kept in sync by hand.
    files: ['docs/.vitepress/theme/**/*.{ts,vue}'],
    plugins: {
      'yet-another-license-header': headerPlugin,
    },
    rules: {
      'yet-another-license-header/header': [
        'error',
        { headerFile: '../gradle/config/licenseHeader.txt' },
      ],
    },
  },
  eslintConfigPrettier,
)
